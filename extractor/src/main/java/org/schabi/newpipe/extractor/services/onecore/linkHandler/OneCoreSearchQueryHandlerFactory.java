package org.schabi.newpipe.extractor.services.onecore.linkHandler;

import org.schabi.newpipe.extractor.search.filter.Filter;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.onecore.search.filter.OneCoreFilters;

import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;

import javax.annotation.Nonnull;

import java.util.List;

public final class OneCoreSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    public static final String ALL = "all";
    public static final String VIDEOS = "videos";
    public static final String CHANNELS = "channels";
    public static final String PLAYLISTS = "playlists";

    public static final String MUSIC_SONGS = "music_songs";
    public static final String MUSIC_VIDEOS = "music_videos";
    public static final String MUSIC_ALBUMS = "music_albums";
    public static final String MUSIC_PLAYLISTS = "music_playlists";
    public static final String MUSIC_ARTISTS = "music_artists";

    private final OneCoreFilters searchFilters = new OneCoreFilters();

    private static OneCoreSearchQueryHandlerFactory instance = null;

    /**
     * Singleton to get the same objects of filters during search.
     *
     * The content filter holds a variable search parameter: (filter.getParams())
     *
     * @return
     */
    @Nonnull
    public static synchronized OneCoreSearchQueryHandlerFactory getInstance() {
        if (instance == null) {
            instance = new OneCoreSearchQueryHandlerFactory();
        }
        return instance;
    }

    @Override
    public String getUrl(final String searchString,
                         @Nonnull final List<FilterItem> selectedContentFilter,
                         final List<FilterItem> selectedSortFilter) throws ParsingException {
        searchFilters.setSelectedContentFilter(selectedContentFilter);
        searchFilters.setSelectedSortFilter(selectedSortFilter);
        return searchFilters.evaluateSelectedFilters(searchString);
    }

    @Override
    public Filter getAvailableContentFilter() {
        return searchFilters.getContentFilters();
    }

    @Override
    public Filter getAvailableSortFilter() {
        return searchFilters.getSortFilters();
    }

    @Override
    public Filter getContentFilterSortFilterVariant(final int contentFilterId) {
        return searchFilters.getContentFilterSortFilterVariant(contentFilterId);
    }

    @Override
    public FilterItem getFilterItem(final int filterId) {
        return searchFilters.getFilterItem(filterId);
    }
}
