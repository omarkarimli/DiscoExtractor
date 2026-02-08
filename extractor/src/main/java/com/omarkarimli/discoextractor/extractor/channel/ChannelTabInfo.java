package com.omarkarimli.discoextractor.extractor.channel;

import com.omarkarimli.discoextractor.extractor.InfoItem;
import com.omarkarimli.discoextractor.extractor.ListExtractor;
import com.omarkarimli.discoextractor.extractor.ListInfo;
import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;
import com.omarkarimli.discoextractor.extractor.utils.ExtractorHelper;

import java.io.IOException;

public class ChannelTabInfo extends ListInfo<InfoItem> {
    public ChannelTabInfo(final int serviceId, final ListLinkHandler linkHandler) {
        super(serviceId, linkHandler, linkHandler.getContentFilters().get(0).getName());
    }

    public static ChannelTabInfo getInfo(final StreamingService service,
                                         final ListLinkHandler linkHandler)
            throws ExtractionException, IOException {
        final ChannelTabExtractor extractor = service.getChannelTabExtractor(linkHandler);
        extractor.fetchPage();
        return getInfo(extractor);
    }

    public static ChannelTabInfo getInfo(final ChannelTabExtractor extractor) {
        final ChannelTabInfo info =
                new ChannelTabInfo(extractor.getServiceId(), extractor.getLinkHandler());

        try {
            info.setOriginalUrl(extractor.getOriginalUrl());
        } catch (final Exception e) {
            info.addError(e);
        }

        final ListExtractor.InfoItemsPage<InfoItem> page
                = ExtractorHelper.getItemsPageOrLogError(info, extractor);
        info.setRelatedItems(page.getItems());
        info.setNextPage(page.getNextPage());

        return info;
    }

    public static ListExtractor.InfoItemsPage<InfoItem> getMoreItems(
            final StreamingService service, final ListLinkHandler linkHandler, final Page page)
            throws ExtractionException, IOException {
        return service.getChannelTabExtractor(linkHandler).getPage(page);
    }
}
