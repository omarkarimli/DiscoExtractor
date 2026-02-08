package com.omarkarimli.discoextractor.extractor.services.peertube;

import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;

import static com.omarkarimli.discoextractor.extractor.StreamingService.ServiceInfo.MediaCapability.COMMENTS;
import static com.omarkarimli.discoextractor.extractor.StreamingService.ServiceInfo.MediaCapability.VIDEO;
import static java.util.Arrays.asList;

import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.channel.ChannelExtractor;
import com.omarkarimli.discoextractor.extractor.channel.ChannelTabExtractor;
import com.omarkarimli.discoextractor.extractor.comments.CommentsExtractor;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.kiosk.KioskList;
import com.omarkarimli.discoextractor.extractor.linkhandler.ChannelTabs;
import com.omarkarimli.discoextractor.extractor.linkhandler.LinkHandler;
import com.omarkarimli.discoextractor.extractor.linkhandler.LinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.linkhandler.SearchQueryHandler;
import com.omarkarimli.discoextractor.extractor.linkhandler.SearchQueryHandlerFactory;
import com.omarkarimli.discoextractor.extractor.playlist.PlaylistExtractor;
import com.omarkarimli.discoextractor.extractor.search.SearchExtractor;
import com.omarkarimli.discoextractor.extractor.services.peertube.extractors.PeertubeAccountExtractor;
import com.omarkarimli.discoextractor.extractor.services.peertube.extractors.PeertubeAccountTabExtractor;
import com.omarkarimli.discoextractor.extractor.services.peertube.extractors.PeertubeChannelExtractor;
import com.omarkarimli.discoextractor.extractor.services.peertube.extractors.PeertubeChannelTabExtractor;
import com.omarkarimli.discoextractor.extractor.services.peertube.extractors.PeertubeCommentsExtractor;
import com.omarkarimli.discoextractor.extractor.services.peertube.extractors.PeertubePlaylistExtractor;
import com.omarkarimli.discoextractor.extractor.services.peertube.extractors.PeertubeSearchExtractor;
import com.omarkarimli.discoextractor.extractor.services.peertube.extractors.PeertubeStreamExtractor;
import com.omarkarimli.discoextractor.extractor.services.peertube.extractors.PeertubeSuggestionExtractor;
import com.omarkarimli.discoextractor.extractor.services.peertube.extractors.PeertubeTrendingExtractor;
import com.omarkarimli.discoextractor.extractor.services.peertube.linkHandler.PeertubeChannelLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.peertube.linkHandler.PeertubeChannelTabLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.peertube.linkHandler.PeertubeCommentsLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.peertube.linkHandler.PeertubePlaylistLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.peertube.linkHandler.PeertubeSearchQueryHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.peertube.linkHandler.PeertubeStreamLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.peertube.linkHandler.PeertubeTrendingLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.stream.StreamExtractor;
import com.omarkarimli.discoextractor.extractor.subscription.SubscriptionExtractor;
import com.omarkarimli.discoextractor.extractor.suggestion.SuggestionExtractor;

import java.util.List;
import java.util.Optional;

public class PeertubeService extends StreamingService {

    private PeertubeInstance instance;

    public PeertubeService(final int id) {
        this(id, PeertubeInstance.DEFAULT_INSTANCE);
    }

    public PeertubeService(final int id, final PeertubeInstance instance) {
        super(id, "PeerTube", asList(VIDEO, COMMENTS));
        this.instance = instance;
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() {
        return PeertubeStreamLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() {
        return PeertubeChannelLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelTabLHFactory() {
        return PeertubeChannelTabLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getPlaylistLHFactory() {
        return PeertubePlaylistLinkHandlerFactory.getInstance();
    }

    @Override
    public SearchQueryHandlerFactory getSearchQHFactory() {
        return PeertubeSearchQueryHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getCommentsLHFactory() {
        return PeertubeCommentsLinkHandlerFactory.getInstance();
    }

    @Override
    public SearchExtractor getSearchExtractor(final SearchQueryHandler queryHandler) {
        final List<FilterItem> selectedSortFilter = queryHandler.getSortFilter();

        final Optional<FilterItem> sepiaFilter = PeertubeHelpers.getSepiaFilter(selectedSortFilter);
        return new PeertubeSearchExtractor(this, queryHandler, sepiaFilter.isPresent());
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return new PeertubeSuggestionExtractor(this);
    }

    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        return null;
    }

    @Override
    public ChannelExtractor getChannelExtractor(final ListLinkHandler linkHandler)
            throws ExtractionException {

        if (linkHandler.getUrl().contains("/video-channels/")) {
            return new PeertubeChannelExtractor(this, linkHandler);
        } else {
            return new PeertubeAccountExtractor(this, linkHandler);
        }
    }

    @Override
    public ChannelTabExtractor getChannelTabExtractor(final ListLinkHandler linkHandler)
            throws ExtractionException {
        final String tab = linkHandler.getContentFilters().get(0).getName();
        switch (tab) {
            case ChannelTabs.CHANNELS:
                return new PeertubeAccountTabExtractor(this, linkHandler);
            case ChannelTabs.PLAYLISTS:
                return new PeertubeChannelTabExtractor(this, linkHandler);
        }
        throw new ParsingException("tab " + tab + " not supported");
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(final ListLinkHandler linkHandler)
            throws ExtractionException {
        return new PeertubePlaylistExtractor(this, linkHandler);
    }

    @Override
    public StreamExtractor getStreamExtractor(final LinkHandler linkHandler)
            throws ExtractionException {
        return new PeertubeStreamExtractor(this, linkHandler);
    }

    @Override
    public CommentsExtractor getCommentsExtractor(final ListLinkHandler linkHandler)
            throws ExtractionException {
        return new PeertubeCommentsExtractor(this, linkHandler);
    }

    @Override
    public String getBaseUrl() {
        return instance.getUrl();
    }

    public PeertubeInstance getInstance() {
        return this.instance;
    }

    public void setInstance(final PeertubeInstance instance) {
        this.instance = instance;
    }

    @Override
    public KioskList getKioskList() throws ExtractionException {
        final KioskList.KioskExtractorFactory kioskFactory = (streamingService, url, id) ->
                new PeertubeTrendingExtractor(
                        PeertubeService.this,
                        new PeertubeTrendingLinkHandlerFactory().fromId(id),
                        id
                );

        final KioskList list = new KioskList(this);

        // add kiosks here e.g.:
        final PeertubeTrendingLinkHandlerFactory h = new PeertubeTrendingLinkHandlerFactory();
        try {
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_TRENDING);
            list.addKioskEntry(kioskFactory, h,
                    PeertubeTrendingLinkHandlerFactory.KIOSK_MOST_LIKED);
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_RECENT);
            list.addKioskEntry(kioskFactory, h, PeertubeTrendingLinkHandlerFactory.KIOSK_LOCAL);
            list.setDefaultKiosk(PeertubeTrendingLinkHandlerFactory.KIOSK_TRENDING);
        } catch (final Exception e) {
            throw new ExtractionException(e);
        }

        return list;
    }


}
