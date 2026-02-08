package com.omarkarimli.discoextractor.extractor.services.peertube;

import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;
import com.omarkarimli.discoextractor.extractor.services.peertube.search.filter.PeertubeFilters;

import java.util.List;
import java.util.Optional;

public final class PeertubeHelpers {
    private PeertubeHelpers() { }
    public static Optional<FilterItem> getSepiaFilter(final List<FilterItem> selectedFilters) {
        final Optional<FilterItem> sepiaFilter = selectedFilters.stream()
                .filter(filterItem -> filterItem instanceof PeertubeFilters.PeertubeSepiaFilterItem)
                .findFirst();

        return sepiaFilter;
    }

    public static Optional<FilterItem> getSpecificFilter(final List<FilterItem> selectedFilters, final Class clazz) {
        final Optional<FilterItem> sepiaFilter = selectedFilters.stream()
                .filter(filterItem -> filterItem.getClass().isInstance(clazz))
                .findFirst();

        return sepiaFilter;
    }
}
