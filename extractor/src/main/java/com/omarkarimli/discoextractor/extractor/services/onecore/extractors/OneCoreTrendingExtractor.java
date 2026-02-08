package com.omarkarimli.discoextractor.extractor.services.onecore.extractors;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonWriter;

import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.ServiceList;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.downloader.Downloader;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.kiosk.KioskExtractor;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;
import com.omarkarimli.discoextractor.extractor.localization.TimeAgoParser;
import com.omarkarimli.discoextractor.extractor.stream.StreamInfoItem;
import com.omarkarimli.discoextractor.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;

import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getJsonPostResponse;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getTextAtKey;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.prepareDesktopJsonBuilder;
import static com.omarkarimli.discoextractor.extractor.utils.Utils.UTF_8;
import static com.omarkarimli.discoextractor.extractor.utils.Utils.isNullOrEmpty;

public class OneCoreTrendingExtractor extends KioskExtractor<StreamInfoItem> {
    private JsonObject initialData;

    public OneCoreTrendingExtractor(final StreamingService service,
                                    final ListLinkHandler linkHandler,
                                    final String kioskId) {
        super(service, linkHandler, kioskId);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        // @formatter:off
        final byte[] body = JsonWriter.string(prepareDesktopJsonBuilder(getExtractorLocalization(),
                        getExtractorContentCountry())
                .value("browseId", "UC4R8DWoMoI7CAwX8_LjQHig")
                .done())
                .getBytes(UTF_8);
        // @formatter:on

        initialData = getJsonPostResponse("browse", body, getExtractorLocalization());
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page) {
        return InfoItemsPage.emptyPage();
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        final JsonObject header = initialData.getObject("header");
        String name = null;
        if (header.has("feedTabbedHeaderRenderer")) {
            name = getTextAtKey(header.getObject("feedTabbedHeaderRenderer"), "title");
        } else if (header.has("c4TabbedHeaderRenderer")) {
            name = getTextAtKey(header.getObject("c4TabbedHeaderRenderer"), "title");
        } else if (header.has("carouselHeaderRenderer")) {
            name = header.getObject("carouselHeaderRenderer").getArray("contents").getObject(0)
                    .getObject("topicChannelDetailsRenderer").getObject("title").getString("simpleText");
        }

        if (isNullOrEmpty(name)) {
            name = "Unknown";
        }
        return name;
    }

    @Nonnull
    @Override
    public InfoItemsPage<StreamInfoItem> getInitialPage() throws ParsingException {
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());
        final TimeAgoParser timeAgoParser = getTimeAgoParser();
        final JsonObject tabContent = getTrendingTabRenderer().getObject("content");

        if (tabContent.has("richGridRenderer")) {
            tabContent.getObject("richGridRenderer")
                    .getArray("contents")
                    .stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    // Filter Trending shorts and Recently trending sections
                    .filter(content -> content.has("richItemRenderer"))
                    .map(content -> content.getObject("richItemRenderer")
                            .getObject("content")
                            .getObject("videoRenderer"))
                    .forEachOrdered(videoRenderer -> collector.commit(
                            new OneCoreStreamInfoItemExtractor(videoRenderer, timeAgoParser)));
        } else if (tabContent.has("sectionListRenderer")) {
            tabContent.getObject("sectionListRenderer")
                    .getArray("contents")
                    .stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .flatMap(content -> content.getObject("itemSectionRenderer")
                            .getArray("contents")
                            .stream())
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .map(content -> content.getObject("shelfRenderer"))
                    // Filter Trending shorts and Recently trending sections which have a title,
                    // contrary to normal trends
                    .filter(shelfRenderer -> !shelfRenderer.has("title"))
                    .flatMap(shelfRenderer -> shelfRenderer.getObject("content")
                            .getObject("expandedShelfContentsRenderer")
                            .getArray("items")
                            .stream())
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .map(item -> item.getObject("videoRenderer"))
                    .forEachOrdered(videoRenderer -> collector.commit(
                            new OneCoreStreamInfoItemExtractor(videoRenderer, timeAgoParser)));
        }
        // try 1
        if(collector.getItems().isEmpty()) {
            tabContent.getObject("sectionListRenderer")
                    .getArray("contents")
                    .stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .flatMap(content -> content.getObject("itemSectionRenderer")
                            .getArray("contents")
                            .stream())
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .map(content -> content.getObject("shelfRenderer"))
                    .flatMap(shelfRenderer -> shelfRenderer.getObject("content")
                            .getObject("horizontalListRenderer")
                            .getArray("items")
                            .stream())
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .map(item -> item.getObject("gridVideoRenderer"))
                    .forEachOrdered(videoRenderer -> collector.commit(
                            new OneCoreStreamInfoItemExtractor(videoRenderer, timeAgoParser)));
        }
        // try 2
        if (collector.getItems().isEmpty()) {
            tabContent.getObject("richGridRenderer")
                    .getArray("contents")
                    .stream()
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    // Filter Trending shorts and Recently trending sections
                    .filter(content -> content.has("richSectionRenderer"))
                    .map(content -> content.getObject("richSectionRenderer")
                            .getObject("content")
                            .getObject("richShelfRenderer"))
                    .flatMap(richShelfRenderer -> richShelfRenderer.getArray("contents")
                            .stream())
                    .filter(JsonObject.class::isInstance)
                    .map(JsonObject.class::cast)
                    .filter(content -> content.has("richItemRenderer"))
                    .map(content -> content.getObject("richItemRenderer")
                            .getObject("content")
                            .getObject("videoRenderer"))
                    .forEachOrdered(videoRenderer -> collector.commit(
                            new OneCoreStreamInfoItemExtractor(videoRenderer, timeAgoParser)));
        }
        if (collector.getItems().isEmpty()) {
            throw new ParsingException("Could not get trending page");
        }

        if (ServiceList.OneCore.getFilterTypes().contains("recommendations")) {
            collector.applyBlocking(ServiceList.OneCore.getFilterConfig());
        }
        return new InfoItemsPage<>(collector, null);
    }

    private JsonObject getTrendingTabRenderer() throws ParsingException {
        return initialData.getObject("contents")
                .getObject("twoColumnBrowseResultsRenderer")
                .getArray("tabs")
                .stream()
                .filter(JsonObject.class::isInstance)
                .map(JsonObject.class::cast)
                .map(tab -> tab.getObject("tabRenderer"))
                .filter(tabRenderer -> tabRenderer.getBoolean("selected"))
                // There should be at most one tab selected
                .findFirst()
                .orElseThrow(() -> new ParsingException("Could not get \"Now\" trending tab"));
    }
}
