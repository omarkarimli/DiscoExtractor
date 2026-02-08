package com.omarkarimli.discoextractor.extractor.services.soundcloud;

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
import com.omarkarimli.discoextractor.extractor.localization.ContentCountry;
import com.omarkarimli.discoextractor.extractor.playlist.PlaylistExtractor;
import com.omarkarimli.discoextractor.extractor.search.SearchExtractor;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.extractors.SoundcloudChannelExtractor;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.extractors.SoundcloudChannelTabExtractor;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.extractors.SoundcloudChartsExtractor;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.extractors.SoundcloudCommentsExtractor;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.extractors.SoundcloudPlaylistExtractor;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.extractors.SoundcloudSearchExtractor;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.extractors.SoundcloudStreamExtractor;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.extractors.SoundcloudSubscriptionExtractor;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.extractors.SoundcloudSuggestionExtractor;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.linkHandler.SoundcloudChannelLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.linkHandler.SoundcloudChannelTabLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.linkHandler.SoundcloudChartsLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.linkHandler.SoundcloudCommentsLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.linkHandler.SoundcloudPlaylistLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.linkHandler.SoundcloudSearchQueryHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.linkHandler.SoundcloudStreamLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.stream.StreamExtractor;
import com.omarkarimli.discoextractor.extractor.subscription.SubscriptionExtractor;

import java.util.List;

import static java.util.Arrays.asList;
import static com.omarkarimli.discoextractor.extractor.StreamingService.ServiceInfo.MediaCapability.AUDIO;
import static com.omarkarimli.discoextractor.extractor.StreamingService.ServiceInfo.MediaCapability.COMMENTS;

public class SoundcloudService extends StreamingService {

    public SoundcloudService(final int id) {
        super(id, "SoundCloud", asList(AUDIO, COMMENTS));
    }

    @Override
    public String getBaseUrl() {
        return "https://soundcloud.com";
    }

    @Override
    public SearchQueryHandlerFactory getSearchQHFactory() {
        return SoundcloudSearchQueryHandlerFactory.getInstance();
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() {
        return SoundcloudStreamLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() {
        return SoundcloudChannelLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelTabLHFactory() {
        return SoundcloudChannelTabLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getPlaylistLHFactory() {
        return SoundcloudPlaylistLinkHandlerFactory.getInstance();
    }

    @Override
    public List<ContentCountry> getSupportedCountries() {
        // Country selector here: https://soundcloud.com/charts/top?genre=all-music
        return ContentCountry.listFrom(
                "AU", "CA", "DE", "FR", "GB", "IE", "NL", "NZ", "US"
        );
    }

    @Override
    public StreamExtractor getStreamExtractor(final LinkHandler linkHandler) {
        return new SoundcloudStreamExtractor(this, linkHandler);
    }

    @Override
    public ChannelExtractor getChannelExtractor(final ListLinkHandler linkHandler) {
        return new SoundcloudChannelExtractor(this, linkHandler);
    }

    @Override
    public ChannelTabExtractor getChannelTabExtractor(final ListLinkHandler linkHandler) {
        return new SoundcloudChannelTabExtractor(this, linkHandler);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(final ListLinkHandler linkHandler) {
        return new SoundcloudPlaylistExtractor(this, linkHandler);
    }

    @Override
    public SearchExtractor getSearchExtractor(final SearchQueryHandler queryHandler) {
        return new SoundcloudSearchExtractor(this, queryHandler);
    }

    @Override
    public SoundcloudSuggestionExtractor getSuggestionExtractor() {
        return new SoundcloudSuggestionExtractor(this);
    }
    @Override
    public KioskList getKioskList() throws ExtractionException {
        final KioskList.KioskExtractorFactory chartsFactory = (streamingService, url, id) ->
                new SoundcloudChartsExtractor(SoundcloudService.this,
                        new SoundcloudChartsLinkHandlerFactory().fromUrl(url), id);

        final KioskList list = new KioskList(this);

        // add kiosks here e.g.:
        final SoundcloudChartsLinkHandlerFactory h = new SoundcloudChartsLinkHandlerFactory();
        try {
            list.addKioskEntry(chartsFactory, h, "Top 50");
            list.addKioskEntry(chartsFactory, h, "New & hot");
            list.setDefaultKiosk("New & hot");
        } catch (final Exception e) {
            throw new ExtractionException(e);
        }

        return list;
    }

    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        return new SoundcloudSubscriptionExtractor(this);
    }

    @Override
    public ListLinkHandlerFactory getCommentsLHFactory() {
        return SoundcloudCommentsLinkHandlerFactory.getInstance();
    }

    @Override
    public CommentsExtractor getCommentsExtractor(final ListLinkHandler linkHandler)
            throws ExtractionException {
        return new SoundcloudCommentsExtractor(this, linkHandler);
    }
}
