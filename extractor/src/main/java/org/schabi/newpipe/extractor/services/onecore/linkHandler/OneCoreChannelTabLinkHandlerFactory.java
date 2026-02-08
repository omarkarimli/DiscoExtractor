package org.schabi.newpipe.extractor.services.onecore.linkHandler;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.ChannelTabs;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.onecore.search.filter.OneCoreFilters;

import java.util.List;

import javax.annotation.Nonnull;

public final class OneCoreChannelTabLinkHandlerFactory extends ListLinkHandlerFactory {
    private static final OneCoreChannelTabLinkHandlerFactory INSTANCE =
            new OneCoreChannelTabLinkHandlerFactory();
    private final OneCoreFilters searchFilters = new OneCoreFilters();

    private OneCoreChannelTabLinkHandlerFactory() {
    }

    public static OneCoreChannelTabLinkHandlerFactory getInstance() {
        return INSTANCE;
    }

    public static String getUrlSuffix(final String tab) throws ParsingException {
        switch (tab) {
            case ChannelTabs.VIDEOS:
                return "/videos";
            case ChannelTabs.PLAYLISTS:
                return "/playlists";
            case ChannelTabs.LIVESTREAMS:
                return "/streams";
            case ChannelTabs.SHORTS:
                return "/shorts";
            case ChannelTabs.CHANNELS:
                return "/channels";
        }
        throw new ParsingException("tab " + tab + " not supported");
    }

    @Override
    public String getUrl(final String id,@Nonnull final List<FilterItem> selectedContentFilter,
                         final List<FilterItem> selectedSortFilter)
            throws ParsingException {
        return "https://www.youtube.com/" + id + getUrlSuffix(selectedContentFilter.get(0).getName());
    }

    @Override
    public String getId(final String url) throws ParsingException {
        return OneCoreChannelLinkHandlerFactory.getInstance().getId(url);
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        try {
            getId(url);
        } catch (final ParsingException e) {
            return false;
        }
        return true;
    }


}
