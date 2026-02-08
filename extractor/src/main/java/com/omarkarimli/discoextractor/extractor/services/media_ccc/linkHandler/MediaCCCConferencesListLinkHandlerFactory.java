package com.omarkarimli.discoextractor.extractor.services.media_ccc.linkHandler;

import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;

import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

public class MediaCCCConferencesListLinkHandlerFactory extends ListLinkHandlerFactory {
    @Override
    public String getId(final String url) throws ParsingException {
        return "conferences";
    }

    @Override
    public String getUrl(final String id, final List<FilterItem> contentFilter,
                         final List<FilterItem> sortFilter) throws ParsingException {
        return "https://media.ccc.de/public/conferences";
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        return url.equals("https://media.ccc.de/b/conferences")
                || url.equals("https://media.ccc.de/public/conferences")
                || url.equals("https://api.media.ccc.de/public/conferences");
    }
}
