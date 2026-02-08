// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package com.omarkarimli.discoextractor.extractor.services.bilibili.linkHandler;

import static com.omarkarimli.discoextractor.extractor.services.bilibili.utils.formatParamWithPercentSpace;

import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.SearchQueryHandlerFactory;
import com.omarkarimli.discoextractor.extractor.search.filter.Filter;
import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;
import com.omarkarimli.discoextractor.extractor.services.bilibili.search.filter.BilibiliFilters;

import java.util.List;

public class BilibiliSearchQueryHandlerFactory extends SearchQueryHandlerFactory {


    private static final String SEARCH_URL = "https://api.bilibili.com/x/web-interface/search/type?";

    private final BilibiliFilters searchFilters = new BilibiliFilters();

    @Override
    public String getUrl(final String query, final List<FilterItem> selectedContentFilter,
                         final List<FilterItem> selectedSortFilter)
            throws ParsingException {

        searchFilters.setSelectedSortFilter(selectedSortFilter);
        searchFilters.setSelectedContentFilter(selectedContentFilter);

        final String filterQuery = searchFilters.evaluateSelectedContentFilters();

        return SEARCH_URL + filterQuery + "&keyword=" + formatParamWithPercentSpace(query) + "&page=1";
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
    public FilterItem getFilterItem(final int filterId) {
        return searchFilters.getFilterItem(filterId);
    }

    @Override
    public Filter getContentFilterSortFilterVariant(final int contentFilterId) {
        return searchFilters.getContentFilterSortFilterVariant(contentFilterId);
    }
}
