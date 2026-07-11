package com.omarkarimli.discoextractor.extractor.feed;

import com.omarkarimli.discoextractor.extractor.ListExtractor;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;
import com.omarkarimli.discoextractor.extractor.stream.StreamInfoItem;

/**
 * This class helps to extract items from lightweight feeds that the services may provide.
 * <p>
 * OneCore is an example of a service that has this alternative available.
 */
public abstract class FeedExtractor extends ListExtractor<StreamInfoItem> {
    public FeedExtractor(final StreamingService service, final ListLinkHandler listLinkHandler) {
        super(service, listLinkHandler);
    }
}
