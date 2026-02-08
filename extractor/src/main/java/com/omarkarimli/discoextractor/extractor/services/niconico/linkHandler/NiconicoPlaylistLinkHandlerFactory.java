package com.omarkarimli.discoextractor.extractor.services.niconico.linkHandler;

import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;

import java.util.List;

public class NiconicoPlaylistLinkHandlerFactory extends ListLinkHandlerFactory {
    @Override
    public String getId(String url) throws ParsingException {
        return url;
    }

    @Override
    public String getUrl(String id, List<FilterItem> contentFilter, List<FilterItem> sortFilter) throws ParsingException {
        return id;
    }

    @Override
    public boolean onAcceptUrl(String url) throws ParsingException {
        return url.contains("/mylist/") || url.contains("/series/");
    }
}
