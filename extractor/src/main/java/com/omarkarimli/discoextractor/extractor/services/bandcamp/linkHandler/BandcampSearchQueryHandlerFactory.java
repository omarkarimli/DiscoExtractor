// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package com.omarkarimli.discoextractor.extractor.services.bandcamp.linkHandler;

import com.omarkarimli.discoextractor.extractor.services.bandcamp.search.filter.BandcampFilters;
import com.omarkarimli.discoextractor.extractor.search.filter.Filter;
import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;

import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.SearchQueryHandlerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_URL;

public class BandcampSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    private final BandcampFilters searchFilters = new BandcampFilters();

    @Override
    public String getUrl(final String query,
                         final List<FilterItem> selectedContentFilter,
                         final List<FilterItem> selectedSortFilter) throws ParsingException {


        searchFilters.setSelectedSortFilter(selectedSortFilter);
        searchFilters.setSelectedContentFilter(selectedContentFilter);

        final String filterQuery = searchFilters.evaluateSelectedContentFilters();

        try {
            return BASE_URL + "/search?q=" + URLEncoder.encode(query, "UTF-8")
                    + filterQuery + "&page=1";
        } catch (final UnsupportedEncodingException e) {
            throw new ParsingException("query \"" + query + "\" could not be encoded", e);
        }
    }

    @Override
    public Filter getAvailableContentFilter() {
        return searchFilters.getContentFilters();
    }

    @Override
    public FilterItem getFilterItem(final int filterId) {
        return searchFilters.getFilterItem(filterId);
    }
}
