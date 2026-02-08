package com.omarkarimli.discoextractor.extractor.services.niconico.extractors;

import com.grack.nanojson.JsonObject;

import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.comments.CommentsExtractor;
import com.omarkarimli.discoextractor.extractor.comments.CommentsInfoItem;
import com.omarkarimli.discoextractor.extractor.comments.CommentsInfoItemsCollector;
import com.omarkarimli.discoextractor.extractor.downloader.Downloader;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;

import java.io.IOException;

import javax.annotation.Nonnull;

public class NiconicoCommentsExtractor extends CommentsExtractor {

    private JsonObject watch;
    private final NiconicoWatchDataCache watchDataCache;
    private final NiconicoCommentsCache commentsCache;

    public NiconicoCommentsExtractor(
            final StreamingService service,
            final ListLinkHandler uiHandler,
            final NiconicoWatchDataCache watchDataCache,
            final NiconicoCommentsCache commentsCache) {
        super(service, uiHandler);
        this.watchDataCache = watchDataCache;
        this.commentsCache = commentsCache;
    }

    @Override
    public void onFetchPage(final @Nonnull Downloader downloader)
            throws IOException, ExtractionException {
        watch = watchDataCache.refreshAndGetWatchData(downloader, getId());
    }

    @Nonnull
    @Override
    public InfoItemsPage<CommentsInfoItem> getInitialPage()
            throws IOException, ExtractionException {
        final CommentsInfoItemsCollector collector = new CommentsInfoItemsCollector(getServiceId());
        for (final JsonObject comment : commentsCache.getComments(watch,
                getDownloader(), getId())) {
            collector.commit(new NiconicoCommentsInfoItemExtractor(comment, getUrl()));
        }
        this.getId();
        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public InfoItemsPage<CommentsInfoItem> getPage(final Page page)
            throws IOException, ExtractionException {
        return null;
    }
}
