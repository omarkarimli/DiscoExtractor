package com.omarkarimli.discoextractor.extractor.bulletComments;

import java.io.IOException;

import com.omarkarimli.discoextractor.extractor.ListInfo;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;
import com.omarkarimli.discoextractor.extractor.ListExtractor.InfoItemsPage;
import com.omarkarimli.discoextractor.extractor.NewPipe;
import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.utils.ExtractorHelper;

public final class BulletCommentsInfo extends ListInfo<BulletCommentsInfoItem> {
    private BulletCommentsInfo(
            final int serviceId,
            final ListLinkHandler listUrlIdHandler,
            final String name) {
        super(serviceId, listUrlIdHandler, name);
    }

    public static BulletCommentsInfo getInfo(final String url)
            throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static BulletCommentsInfo getInfo(final StreamingService service, final String url)
            throws ExtractionException, IOException {
        return getInfo(service.getBulletCommentsExtractor(url));
    }

    public static BulletCommentsInfo getInfo(final BulletCommentsExtractor commentsExtractor)
            throws IOException, ExtractionException {
        // for services which do not have a comments extractor
        if (commentsExtractor == null) {
            return null;
        }

        commentsExtractor.fetchPage();

        final String name = commentsExtractor.getName();
        final int serviceId = commentsExtractor.getServiceId();
        final ListLinkHandler listUrlIdHandler = commentsExtractor.getLinkHandler();

        final BulletCommentsInfo commentsInfo = new BulletCommentsInfo(
                serviceId, listUrlIdHandler, name);
        commentsInfo.setBulletCommentsExtractor(commentsExtractor);
        final InfoItemsPage<BulletCommentsInfoItem> initialCommentsPage = ExtractorHelper
                .getItemsPageOrLogError(commentsInfo, commentsExtractor);
        commentsInfo.setRelatedItems(initialCommentsPage.getItems());
        commentsInfo.setNextPage(initialCommentsPage.getNextPage());

        return commentsInfo;
    }

    public static InfoItemsPage<BulletCommentsInfoItem> getMoreItems(
            final BulletCommentsInfo commentsInfo,
            final Page page) throws ExtractionException, IOException {
        return getMoreItems(NewPipe.getService(commentsInfo.getServiceId()), commentsInfo.getUrl(),
                page);
    }

    public static InfoItemsPage<BulletCommentsInfoItem> getMoreItems(
            final StreamingService service,
            final BulletCommentsInfo commentsInfo,
            final Page page) throws IOException, ExtractionException {
        return getMoreItems(service, commentsInfo.getUrl(), page);
    }

    public static InfoItemsPage<BulletCommentsInfoItem> getMoreItems(
            final StreamingService service,
            final String url,
            final Page page) throws IOException, ExtractionException {
        return service.getBulletCommentsExtractor(url).getPage(page);
    }

    private transient BulletCommentsExtractor commentsExtractor;

    public BulletCommentsExtractor getBulletCommentsExtractor() {
        return commentsExtractor;
    }

    public void setBulletCommentsExtractor(final BulletCommentsExtractor commentsExtractor) {
        this.commentsExtractor = commentsExtractor;
    }
}
