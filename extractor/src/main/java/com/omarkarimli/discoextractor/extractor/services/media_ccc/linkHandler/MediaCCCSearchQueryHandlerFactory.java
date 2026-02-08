package com.omarkarimli.discoextractor.extractor.services.media_ccc.linkHandler;

import com.omarkarimli.discoextractor.extractor.search.filter.Filter;
import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;

import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.SearchQueryHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.media_ccc.search.filter.MediaCCCFilters;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static com.omarkarimli.discoextractor.extractor.utils.Utils.UTF_8;

public class MediaCCCSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    public final MediaCCCFilters searchFilters = new MediaCCCFilters();

    @Override
    public Filter getAvailableContentFilter() {
        return searchFilters.getContentFilters();
    }

    @Override
    public FilterItem getFilterItem(final int filterId) {
        return searchFilters.getFilterItem(filterId);
    }

    @Override
    public String getUrl(final String query, final List<FilterItem> contentFilter,
                         final List<FilterItem> sortFilter) throws ParsingException {
        try {
            return "https://media.ccc.de/public/events/search?q="
                    + URLEncoder.encode(query, UTF_8);
        } catch (final UnsupportedEncodingException e) {
            throw new ParsingException("Could not create search string with query: " + query, e);
        }
    }
}
