package com.omarkarimli.discoextractor.extractor.channel;

import com.omarkarimli.discoextractor.extractor.InfoItem;
import com.omarkarimli.discoextractor.extractor.ListExtractor;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;

import javax.annotation.Nonnull;

public abstract class ChannelTabExtractor extends ListExtractor<InfoItem> {

    public ChannelTabExtractor(final StreamingService service,
                               final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    @Nonnull
    public String getTab() {
        return getLinkHandler().getContentFilters().get(0).getName();
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return getTab();
    }

}
