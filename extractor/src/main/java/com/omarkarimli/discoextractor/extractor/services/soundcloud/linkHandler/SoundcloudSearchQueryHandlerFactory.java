package com.omarkarimli.discoextractor.extractor.services.soundcloud.linkHandler;

import com.omarkarimli.discoextractor.extractor.search.filter.Filter;
import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.search.filter.SoundcloudFilters;

import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.exceptions.ReCaptchaException;
import com.omarkarimli.discoextractor.extractor.linkhandler.SearchQueryHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.SoundcloudParsingHelper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import static com.omarkarimli.discoextractor.extractor.services.soundcloud.SoundcloudParsingHelper.SOUNDCLOUD_API_V2_URL;
import static com.omarkarimli.discoextractor.extractor.utils.Utils.UTF_8;

public class SoundcloudSearchQueryHandlerFactory extends SearchQueryHandlerFactory {

    public static final int ITEMS_PER_PAGE = 10;
    private static SoundcloudSearchQueryHandlerFactory instance = null;

    private final SoundcloudFilters searchFilters = new SoundcloudFilters();

    public static synchronized SoundcloudSearchQueryHandlerFactory getInstance() {
        if (instance == null) {
            instance = new SoundcloudSearchQueryHandlerFactory();
        }
        return instance;
    }
    @Override
    public String getUrl(final String id,
                         final List<FilterItem> selectedContentFilter,
                         final List<FilterItem> selectedSortFilter)
            throws ParsingException {

        String url = SOUNDCLOUD_API_V2_URL + "search";
        String sortQuery = "";

        searchFilters.setSelectedContentFilter(selectedContentFilter);
        searchFilters.setSelectedSortFilter(selectedSortFilter);
        url += searchFilters.evaluateSelectedContentFilters();
        sortQuery = searchFilters.evaluateSelectedSortFilters();

        try {
            return url + "?q=" + URLEncoder.encode(id, UTF_8) + sortQuery + "&client_id="
                    + SoundcloudParsingHelper.clientId() + "&limit=" + ITEMS_PER_PAGE
                    + "&offset=0";

        } catch (final UnsupportedEncodingException e) {
            throw new ParsingException("Could not encode query", e);
        } catch (final ReCaptchaException e) {
            throw new ParsingException("ReCaptcha required", e);
        } catch (final IOException | ExtractionException e) {
            throw new ParsingException("Could not get client id", e);
        }
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
