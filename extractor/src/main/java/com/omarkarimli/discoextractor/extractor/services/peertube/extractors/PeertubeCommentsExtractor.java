package com.omarkarimli.discoextractor.extractor.services.peertube.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.comments.CommentsExtractor;
import com.omarkarimli.discoextractor.extractor.comments.CommentsInfoItem;
import com.omarkarimli.discoextractor.extractor.comments.CommentsInfoItemsCollector;
import com.omarkarimli.discoextractor.extractor.downloader.Downloader;
import com.omarkarimli.discoextractor.extractor.downloader.Response;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;
import com.omarkarimli.discoextractor.extractor.services.peertube.PeertubeParsingHelper;
import com.omarkarimli.discoextractor.extractor.utils.Utils;

import java.io.IOException;

import static com.omarkarimli.discoextractor.extractor.services.peertube.PeertubeParsingHelper.COUNT_KEY;
import static com.omarkarimli.discoextractor.extractor.services.peertube.PeertubeParsingHelper.ITEMS_PER_PAGE;
import static com.omarkarimli.discoextractor.extractor.services.peertube.PeertubeParsingHelper.START_KEY;
import static com.omarkarimli.discoextractor.extractor.utils.Utils.isNullOrEmpty;

import javax.annotation.Nonnull;

public class PeertubeCommentsExtractor extends CommentsExtractor {
    public PeertubeCommentsExtractor(final StreamingService service,
                                     final ListLinkHandler uiHandler) {
        super(service, uiHandler);
    }

    @Nonnull
    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage()
            throws IOException, ExtractionException {
        return getPage(new Page(getUrl() + "?" + START_KEY + "=0&"
                + COUNT_KEY + "=" + ITEMS_PER_PAGE));
    }

    private void collectCommentsFrom(final CommentsInfoItemsCollector collector,
                                     final JsonObject json) throws ParsingException {
        final JsonArray contents = json.getArray("data");

        for (final Object c : contents) {
            if (c instanceof JsonObject) {
                final JsonObject item = (JsonObject) c;
                if (!item.getBoolean("isDeleted")) {
                    collector.commit(new PeertubeCommentsInfoItemExtractor(item, this));
                }
            }
        }
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        if (page == null || isNullOrEmpty(page.getUrl())) {
            throw new IllegalArgumentException("Page doesn't contain an URL");
        }

        final Response response = getDownloader().get(page.getUrl());

        JsonObject json = null;
        if (response != null && !Utils.isBlank(response.responseBody())) {
            try {
                json = JsonParser.object().from(response.responseBody());
            } catch (final Exception e) {
                throw new ParsingException("Could not parse json data for comments info", e);
            }
        }

        if (json != null) {
            PeertubeParsingHelper.validate(json);
            final long total = json.getLong("total");

            final CommentsInfoItemsCollector collector
                    = new CommentsInfoItemsCollector(getServiceId());
            collectCommentsFrom(collector, json);

            return new InfoItemsPage<>(collector,
                    PeertubeParsingHelper.getNextPage(page.getUrl(), total));
        } else {
            throw new ExtractionException("Unable to get PeerTube kiosk info");
        }
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) {
    }
}
