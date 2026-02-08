package com.omarkarimli.discoextractor.extractor.services.onecore.extractors;

import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;

import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.linkhandler.SearchQueryHandler;
import com.omarkarimli.discoextractor.extractor.search.SearchExtractor;

public abstract class OneCoreBaseSearchExtractor extends SearchExtractor {
    public OneCoreBaseSearchExtractor(final StreamingService service,
                                      final SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @SuppressWarnings("unchecked")
    protected  <T extends FilterItem> T getSelectedContentFilterItem() {
        final FilterItem filterItem = getLinkHandler().getContentFilters().get(0);

        if (filterItem != null) {
            return (T) filterItem;
        }
        throw new RuntimeException("no content filter set");
    }
}
