// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package com.omarkarimli.discoextractor.extractor.services.bandcamp.linkHandler;

import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;

import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampExtractorHelper;
import com.omarkarimli.discoextractor.extractor.utils.Utils;

import java.util.List;

import static com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor.FEATURED_API_URL;
import static com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor.KIOSK_FEATURED;
import static com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampRadioExtractor.KIOSK_RADIO;
import static com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampRadioExtractor.RADIO_API_URL;

public class BandcampFeaturedLinkHandlerFactory extends ListLinkHandlerFactory {

    @Override
    public String getUrl(final String id,
                         final List<FilterItem> contentFilter,
                         final List<FilterItem> sortFilter) {
        if (id.equals(KIOSK_FEATURED)) {
            return FEATURED_API_URL; // doesn't have a website
        } else if (id.equals(KIOSK_RADIO)) {
            return RADIO_API_URL; // doesn't have its own website
        } else {
            return null;
        }
    }

    @Override
    public String getId(final String url) {
        final String fixedUrl = Utils.replaceHttpWithHttps(url);
        if (BandcampExtractorHelper.isRadioUrl(fixedUrl) || fixedUrl.equals(RADIO_API_URL)) {
            return KIOSK_RADIO;
        } else if (fixedUrl.equals(FEATURED_API_URL)) {
            return KIOSK_FEATURED;
        } else {
            return null;
        }
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        final String fixedUrl = Utils.replaceHttpWithHttps(url);
        return fixedUrl.equals(FEATURED_API_URL)
                || fixedUrl.equals(RADIO_API_URL)
                || BandcampExtractorHelper.isRadioUrl(fixedUrl);
    }
}
