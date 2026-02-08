package com.omarkarimli.discoextractor.extractor.services.onecore.extractors;

import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.DISABLE_PRETTY_PRINT_PARAMETER;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.ONECOREI_V1_URL;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getJsonPostResponse;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getTextFromObject;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getValidJsonResponseBody;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.prepareDesktopJsonBuilder;
import static com.omarkarimli.discoextractor.extractor.utils.Utils.UTF_8;
import static com.omarkarimli.discoextractor.extractor.utils.Utils.isNullOrEmpty;

import org.schabi.newpipe.extractor.*;

import com.omarkarimli.discoextractor.extractor.InfoItem;
import com.omarkarimli.discoextractor.extractor.MetaInfo;
import com.omarkarimli.discoextractor.extractor.MultiInfoItemsCollector;
import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.services.onecore.search.filter.OneCoreFilters;
import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonBuilder;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.grack.nanojson.JsonWriter;

import com.omarkarimli.discoextractor.extractor.downloader.Downloader;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.SearchQueryHandler;
import com.omarkarimli.discoextractor.extractor.localization.Localization;
import com.omarkarimli.discoextractor.extractor.localization.TimeAgoParser;
import com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper;
import com.omarkarimli.discoextractor.extractor.utils.JsonUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

public class OneCoreSearchExtractor extends OneCoreBaseSearchExtractor {
    private JsonObject initialData;

    public OneCoreSearchExtractor(final StreamingService service,
                                  final SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
        final String query = super.getSearchString();
        final Localization localization = getExtractorLocalization();

        // Get the search parameter for the request
        final OneCoreFilters.OneCoreContentFilterItem contentFilterItem =
                getSelectedContentFilterItem();
        final String params = contentFilterItem.getParams();

        final JsonBuilder<JsonObject> jsonBody = prepareDesktopJsonBuilder(localization,
                getExtractorContentCountry())
                .value("query", query);
        if (!isNullOrEmpty(params)) {
            jsonBody.value("params", params);
        }

        final byte[] body = JsonWriter.string(jsonBody.done()).getBytes(UTF_8);

        initialData = getJsonPostResponse("search", body, localization);
    }

    @Nonnull
    @Override
    public String getUrl() throws ParsingException {
        return super.getUrl() + "&gl=" + getExtractorContentCountry().getCountryCode();
    }

    @Nonnull
    @Override
    public String getSearchSuggestion() throws ParsingException {
        final JsonObject itemSectionRenderer = initialData.getObject("contents")
                .getObject("twoColumnSearchResultsRenderer").getObject("primaryContents")
                .getObject("sectionListRenderer").getArray("contents").getObject(0)
                .getObject("itemSectionRenderer");
        final JsonObject didYouMeanRenderer = itemSectionRenderer.getArray("contents").getObject(0)
                .getObject("didYouMeanRenderer");
        final JsonObject showingResultsForRenderer = itemSectionRenderer.getArray("contents")
                .getObject(0)
                .getObject("showingResultsForRenderer");

        if (!didYouMeanRenderer.isEmpty()) {
            return JsonUtils.getString(didYouMeanRenderer,
                    "correctedQueryEndpoint.searchEndpoint.query");
        } else if (showingResultsForRenderer != null) {
            return getTextFromObject(showingResultsForRenderer.getObject("correctedQuery"));
        } else {
            return "";
        }
    }

    @Override
    public boolean isCorrectedSearch() {
        final JsonObject showingResultsForRenderer = initialData.getObject("contents")
                .getObject("twoColumnSearchResultsRenderer").getObject("primaryContents")
                .getObject("sectionListRenderer").getArray("contents").getObject(0)
                .getObject("itemSectionRenderer").getArray("contents").getObject(0)
                .getObject("showingResultsForRenderer");
        return !showingResultsForRenderer.isEmpty();
    }

    @Nonnull
    @Override
    public List<MetaInfo> getMetaInfo() throws ParsingException {
        return OneCoreParsingHelper.getMetaInfo(
                initialData.getObject("contents").getObject("twoColumnSearchResultsRenderer")
                        .getObject("primaryContents").getObject("sectionListRenderer")
                        .getArray("contents"));
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPageInternal() throws IOException, ExtractionException {
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        final JsonArray sections = initialData.getObject("contents")
                .getObject("twoColumnSearchResultsRenderer").getObject("primaryContents")
                .getObject("sectionListRenderer").getArray("contents");

        Page nextPage = null;

        for (final Object section : sections) {
            if (((JsonObject) section).has("itemSectionRenderer")) {
                final JsonObject itemSectionRenderer = ((JsonObject) section)
                        .getObject("itemSectionRenderer");

                collectStreamsFrom(collector, itemSectionRenderer.getArray("contents"));
            } else if (((JsonObject) section).has("continuationItemRenderer")) {
                nextPage = getNextPageFrom(((JsonObject) section)
                        .getObject("continuationItemRenderer"));
            }
        }
        return new InfoItemsPage<>(collector, nextPage);
    }

    @Override
    public InfoItemsPage<InfoItem> getPageInternal(final Page page) throws IOException,
            ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final Localization localization = getExtractorLocalization();
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        // @formatter:off
        final byte[] json = JsonWriter.string(prepareDesktopJsonBuilder(localization,
                getExtractorContentCountry())
                .value("continuation", page.getId())
                .done())
                .getBytes(UTF_8);
        // @formatter:on

        final String responseBody = getValidJsonResponseBody(getDownloader().post(
                page.getUrl(), new HashMap<>(), json));

        final JsonObject ajaxJson;
        try {
            ajaxJson = JsonParser.object().from(responseBody);
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse JSON", e);
        }

        final JsonArray continuationItems = ajaxJson.getArray("onResponseReceivedCommands")
                .getObject(0).getObject("appendContinuationItemsAction")
                .getArray("continuationItems");

        final JsonArray contents = continuationItems.getObject(0)
                .getObject("itemSectionRenderer").getArray("contents");
        collectStreamsFrom(collector, contents);
        return new InfoItemsPage<>(collector, getNextPageFrom(continuationItems.getObject(1)
                .getObject("continuationItemRenderer")));
    }

    private void collectStreamsFrom(final MultiInfoItemsCollector collector,
                                    final JsonArray contents) throws NothingFoundException,
            ParsingException {
        final TimeAgoParser timeAgoParser = getTimeAgoParser();

        for (final Object content : contents) {
            final JsonObject item = (JsonObject) content;
            if (item.has("backgroundPromoRenderer")) {
                throw new NothingFoundException(getTextFromObject(
                        item.getObject("backgroundPromoRenderer").getObject("bodyText")));
            } else if (item.has("videoRenderer")) {
                collector.commit(new OneCoreStreamInfoItemExtractor(item
                        .getObject("videoRenderer"), timeAgoParser));
            } else if (item.has("channelRenderer")) {
                collector.commit(new OneCoreChannelInfoItemExtractor(item
                        .getObject("channelRenderer")));
            } else if (item.has("playlistRenderer")) {
                collector.commit(new OneCorePlaylistInfoItemExtractor(item
                        .getObject("playlistRenderer")));
            } else if (item.has("lockupViewModel")) {
                final JsonObject lockupViewModel = item.getObject("lockupViewModel");
                if ("LOCKUP_CONTENT_TYPE_PLAYLIST".equals(
                        lockupViewModel.getString("contentType"))) {
                    collector.commit(
                            new OneCoreMixOrPlaylistLockupInfoItemExtractor(lockupViewModel));
                }
            }
        }
    }

    private Page getNextPageFrom(final JsonObject continuationItemRenderer) throws IOException,
            ExtractionException {
        if (isNullOrEmpty(continuationItemRenderer)) {
            return null;
        }

        final String token = continuationItemRenderer.getObject("continuationEndpoint")
                .getObject("continuationCommand").getString("token");

        final String url = ONECOREI_V1_URL + "search?"
                + DISABLE_PRETTY_PRINT_PARAMETER;

        return new Page(url, token);
    }
}
