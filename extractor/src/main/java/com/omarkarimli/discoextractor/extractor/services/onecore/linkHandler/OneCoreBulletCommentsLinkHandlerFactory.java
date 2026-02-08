package com.omarkarimli.discoextractor.extractor.services.onecore.linkHandler;

import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;

import java.util.List;

public class OneCoreBulletCommentsLinkHandlerFactory extends ListLinkHandlerFactory {
    OneCoreStreamLinkHandlerFactory factory = new OneCoreStreamLinkHandlerFactory();
    @Override
    public String getId(String url) throws ParsingException {
        return factory.getId(url);
    }

    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {
        return factory.onAcceptUrl(url);
    }

    @Override
    public String getUrl(String id, List<FilterItem> contentFilter, List<FilterItem> sortFilter) throws ParsingException {
        return factory.getUrl(id);
    }
}
