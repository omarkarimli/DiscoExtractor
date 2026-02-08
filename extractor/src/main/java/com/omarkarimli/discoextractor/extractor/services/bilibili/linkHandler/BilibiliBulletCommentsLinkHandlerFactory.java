package com.omarkarimli.discoextractor.extractor.services.bilibili.linkHandler;

import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;

import java.util.List;

public class BilibiliBulletCommentsLinkHandlerFactory extends ListLinkHandlerFactory {

    @Override
    public String getId(String url) throws ParsingException {
        return new BilibiliStreamLinkHandlerFactory().getId(url);
    }

    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {
        try {
            getId(url);
            return true;
        } catch (final ParsingException e) {
            return false;
        }
    }

    @Override
    public String getUrl(String id, final List<FilterItem> contentFilter,
                         final List<FilterItem> sortFilter) throws ParsingException {
        return new BilibiliStreamLinkHandlerFactory().getUrl(id);
    }
}
