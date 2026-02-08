package com.omarkarimli.discoextractor.extractor.services.onecore.linkHandler;

import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;

import com.omarkarimli.discoextractor.extractor.exceptions.FoundAdException;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandlerFactory;

import java.util.List;

public final class OneCoreCommentsLinkHandlerFactory extends ListLinkHandlerFactory {

    private static final OneCoreCommentsLinkHandlerFactory INSTANCE
            = new OneCoreCommentsLinkHandlerFactory();

    private OneCoreCommentsLinkHandlerFactory() {
    }

    public static OneCoreCommentsLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public String getUrl(final String id) {
        return "https://www.youtube.com/watch?v=" + id;
    }

    @Override
    public String getId(final String urlString) throws ParsingException, IllegalArgumentException {
        // we need the same id, avoids duplicate code
        return OneCoreStreamLinkHandlerFactory.getInstance().getId(urlString);
    }

    @Override
    public boolean onAcceptUrl(final String url) throws FoundAdException {
        try {
            getId(url);
            return true;
        } catch (final FoundAdException fe) {
            throw fe;
        } catch (final ParsingException e) {
            return false;
        }
    }

    @Override
    public String getUrl(final String id,
                         final List<FilterItem> contentFilter,
                         final List<FilterItem> sortFilter) throws ParsingException {
        return getUrl(id);
    }
}
