package com.omarkarimli.discoextractor.extractor.services.soundcloud.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import com.omarkarimli.discoextractor.extractor.InfoItem;
import com.omarkarimli.discoextractor.extractor.InfoItemExtractor;
import com.omarkarimli.discoextractor.extractor.InfoItemsCollector;
import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.downloader.Downloader;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.SearchQueryHandler;
import com.omarkarimli.discoextractor.extractor.MultiInfoItemsCollector;
import com.omarkarimli.discoextractor.extractor.search.SearchExtractor;
import com.omarkarimli.discoextractor.extractor.utils.Parser;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.IntUnaryOperator;

import static com.omarkarimli.discoextractor.extractor.services.soundcloud.linkHandler.SoundcloudSearchQueryHandlerFactory.ITEMS_PER_PAGE;
import static com.omarkarimli.discoextractor.extractor.utils.Utils.EMPTY_STRING;
import static com.omarkarimli.discoextractor.extractor.utils.Utils.isNullOrEmpty;

public class SoundcloudSearchExtractor extends SearchExtractor {
    private JsonObject initialSearchObject;
    private static final String COLLECTION = "collection";
    private static final String TOTAL_RESULTS = "total_results";

    public SoundcloudSearchExtractor(final StreamingService service,
                                     final SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPageInternal() throws IOException, ExtractionException {
        if (initialSearchObject.getInt(TOTAL_RESULTS) > ITEMS_PER_PAGE) {
            return new InfoItemsPage<>(
                    collectItems(initialSearchObject.getArray(COLLECTION)),
                    getNextPageFromCurrentUrl(getUrl(), currentOffset -> ITEMS_PER_PAGE));
        } else {
            return new InfoItemsPage<>(
                    collectItems(initialSearchObject.getArray(COLLECTION)), null);
        }
    }

    @Override
    public InfoItemsPage<InfoItem> getPageInternal(final Page page) throws IOException,
            ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final Downloader dl = getDownloader();
        final JsonArray searchCollection;
        final int totalResults;
        try {
            final String response = dl.get(page.getUrl(), getExtractorLocalization())
                    .responseBody();
            final JsonObject result = JsonParser.object().from(response);
            searchCollection = result.getArray(COLLECTION);
            totalResults = result.getInt(TOTAL_RESULTS);
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }
        if (searchCollection.size() == 0) {
            return InfoItemsPage.emptyPage(); // no more search results
        }

        if (getOffsetFromUrl(page.getUrl()) + ITEMS_PER_PAGE < totalResults) {
            return new InfoItemsPage<>(collectItems(searchCollection),
                    getNextPageFromCurrentUrl(page.getUrl(),
                            currentOffset -> currentOffset + ITEMS_PER_PAGE));
        }
        return new InfoItemsPage<>(collectItems(searchCollection), null);
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws IOException,
            ExtractionException {
        final Downloader dl = getDownloader();
        final String url = getUrl();
        try {
            final String response = dl.get(url, getExtractorLocalization()).responseBody();
            initialSearchObject = JsonParser.object().from(response);
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }

        if (initialSearchObject.getArray(COLLECTION).isEmpty()) {
            throw new SearchExtractor.NothingFoundException("Nothing found");
        }
    }

    private InfoItemsCollector<InfoItem, InfoItemExtractor> collectItems(
            final JsonArray searchCollection) {
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        for (final Object result : searchCollection) {
            if (!(result instanceof JsonObject)) {
                continue;
            }

            final JsonObject searchResult = (JsonObject) result;
            final String kind = searchResult.getString("kind", EMPTY_STRING);
            switch (kind) {
                case "user":
                    collector.commit(new SoundcloudChannelInfoItemExtractor(searchResult));
                    break;
                case "track":
                    collector.commit(new SoundcloudStreamInfoItemExtractor(searchResult));
                    break;
                case "playlist":
                    collector.commit(new SoundcloudPlaylistInfoItemExtractor(searchResult));
                    break;
            }
        }

        return collector;
    }

    private Page getNextPageFromCurrentUrl(final String currentUrl,
                                           final IntUnaryOperator newPageOffsetCalculator)
            throws ParsingException {
        final int currentPageOffset = getOffsetFromUrl(currentUrl);

        return new Page(
                currentUrl.replace(
                        "&offset=" + currentPageOffset,
                        "&offset=" + newPageOffsetCalculator.applyAsInt(currentPageOffset)));
    }

    private int getOffsetFromUrl(final String url) throws ParsingException {
        try {
            return Integer.parseInt(Parser.compatParseMap(new URL(url).getQuery()).get("offset"));
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new ParsingException("Could not get offset from page URL", e);
        }
    }
}
