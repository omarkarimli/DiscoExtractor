// Created by Fynn Godau 2019, licensed GNU GPL version 3 or later

package com.omarkarimli.discoextractor.extractor.services.bandcamp;

import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.channel.ChannelExtractor;
import com.omarkarimli.discoextractor.extractor.channel.ChannelTabExtractor;
import com.omarkarimli.discoextractor.extractor.comments.CommentsExtractor;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.kiosk.KioskList;
import com.omarkarimli.discoextractor.extractor.linkhandler.LinkHandler;
import com.omarkarimli.discoextractor.extractor.linkhandler.LinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.linkhandler.SearchQueryHandler;
import com.omarkarimli.discoextractor.extractor.linkhandler.SearchQueryHandlerFactory;
import com.omarkarimli.discoextractor.extractor.playlist.PlaylistExtractor;
import com.omarkarimli.discoextractor.extractor.search.SearchExtractor;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampChannelExtractor;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampChannelTabExtractor;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampCommentsExtractor;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampExtractorHelper;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampPlaylistExtractor;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampRadioExtractor;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampRadioStreamExtractor;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampSearchExtractor;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampStreamExtractor;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampSuggestionExtractor;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.linkHandler.BandcampChannelLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.linkHandler.BandcampChannelTabLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.linkHandler.BandcampCommentsLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.linkHandler.BandcampFeaturedLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.linkHandler.BandcampPlaylistLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.linkHandler.BandcampSearchQueryHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.linkHandler.BandcampStreamLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.stream.StreamExtractor;
import com.omarkarimli.discoextractor.extractor.subscription.SubscriptionExtractor;
import com.omarkarimli.discoextractor.extractor.suggestion.SuggestionExtractor;

import java.util.Arrays;

import static com.omarkarimli.discoextractor.extractor.StreamingService.ServiceInfo.MediaCapability.AUDIO;
import static com.omarkarimli.discoextractor.extractor.StreamingService.ServiceInfo.MediaCapability.COMMENTS;
import static com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampExtractorHelper.BASE_URL;
import static com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor.FEATURED_API_URL;
import static com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampFeaturedExtractor.KIOSK_FEATURED;
import static com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampRadioExtractor.KIOSK_RADIO;
import static com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.BandcampRadioExtractor.RADIO_API_URL;

public class BandcampService extends StreamingService {

    public BandcampService(final int id) {
        super(id, "Bandcamp", Arrays.asList(AUDIO, COMMENTS));
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() {
        return new BandcampStreamLinkHandlerFactory();
    }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() {
        return BandcampChannelLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelTabLHFactory() {
        return BandcampChannelTabLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getPlaylistLHFactory() {
        return new BandcampPlaylistLinkHandlerFactory();
    }

    @Override
    public SearchQueryHandlerFactory getSearchQHFactory() {
        return new BandcampSearchQueryHandlerFactory();
    }

    @Override
    public ListLinkHandlerFactory getCommentsLHFactory() {
        return new BandcampCommentsLinkHandlerFactory();
    }

    @Override
    public SearchExtractor getSearchExtractor(final SearchQueryHandler queryHandler) {
        return new BandcampSearchExtractor(this, queryHandler);
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return new BandcampSuggestionExtractor(this);
    }

    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        return null;
    }

    @Override
    public KioskList getKioskList() throws ExtractionException {

        final KioskList kioskList = new KioskList(this);

        try {
            kioskList.addKioskEntry(
                    (streamingService, url, kioskId) -> new BandcampFeaturedExtractor(
                            BandcampService.this,
                            new BandcampFeaturedLinkHandlerFactory().fromUrl(FEATURED_API_URL),
                            kioskId
                    ),
                    new BandcampFeaturedLinkHandlerFactory(),
                    KIOSK_FEATURED
            );

            kioskList.addKioskEntry(
                    (streamingService, url, kioskId) -> new BandcampRadioExtractor(
                            BandcampService.this,
                            new BandcampFeaturedLinkHandlerFactory().fromUrl(RADIO_API_URL),
                            kioskId
                    ),
                    new BandcampFeaturedLinkHandlerFactory(),
                    KIOSK_RADIO
            );

            kioskList.setDefaultKiosk(KIOSK_FEATURED);

        } catch (final Exception e) {
            throw new ExtractionException(e);
        }

        return kioskList;
    }

    @Override
    public ChannelExtractor getChannelExtractor(final ListLinkHandler linkHandler) {
        return new BandcampChannelExtractor(this, linkHandler);
    }

    @Override
    public ChannelTabExtractor getChannelTabExtractor(final ListLinkHandler linkHandler) {
        return new BandcampChannelTabExtractor(this, linkHandler);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(final ListLinkHandler linkHandler) {
        return new BandcampPlaylistExtractor(this, linkHandler);
    }

    @Override
    public StreamExtractor getStreamExtractor(final LinkHandler linkHandler) {
        if (BandcampExtractorHelper.isRadioUrl(linkHandler.getUrl())) {
            return new BandcampRadioStreamExtractor(this, linkHandler);
        }
        return new BandcampStreamExtractor(this, linkHandler);
    }

    @Override
    public CommentsExtractor getCommentsExtractor(final ListLinkHandler linkHandler) {
        return new BandcampCommentsExtractor(this, linkHandler);
    }
}
