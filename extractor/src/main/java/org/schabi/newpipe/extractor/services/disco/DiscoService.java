package org.schabi.newpipe.extractor.services.disco;

import org.schabi.newpipe.extractor.bulletComments.BulletCommentsExtractor;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.disco.extractors.DiscoBulletCommentsExtractor;
import org.schabi.newpipe.extractor.services.disco.linkHandler.DiscoBulletCommentsLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.disco.search.filter.DiscoFilters;

import static java.util.Arrays.asList;
import static org.schabi.newpipe.extractor.StreamingService.ServiceInfo.MediaCapability.*;

import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.channel.ChannelExtractor;
import org.schabi.newpipe.extractor.channel.ChannelTabExtractor;
import org.schabi.newpipe.extractor.comments.CommentsExtractor;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.feed.FeedExtractor;
import org.schabi.newpipe.extractor.kiosk.KioskList;
import org.schabi.newpipe.extractor.linkhandler.LinkHandler;
import org.schabi.newpipe.extractor.linkhandler.LinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandler;
import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandler;
import org.schabi.newpipe.extractor.linkhandler.SearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.localization.ContentCountry;
import org.schabi.newpipe.extractor.localization.Localization;
import org.schabi.newpipe.extractor.playlist.PlaylistExtractor;
import org.schabi.newpipe.extractor.search.SearchExtractor;
import org.schabi.newpipe.extractor.services.disco.extractors.DiscoChannelExtractor;
import org.schabi.newpipe.extractor.services.disco.extractors.DiscoChannelTabExtractor;
import org.schabi.newpipe.extractor.services.disco.extractors.DiscoCommentsExtractor;
import org.schabi.newpipe.extractor.services.disco.extractors.DiscoFeedExtractor;
import org.schabi.newpipe.extractor.services.disco.extractors.DiscoMixPlaylistExtractor;
import org.schabi.newpipe.extractor.services.disco.extractors.DiscoMusicSearchExtractor;
import org.schabi.newpipe.extractor.services.disco.extractors.DiscoPlaylistExtractor;
import org.schabi.newpipe.extractor.services.disco.extractors.DiscoSearchExtractor;
import org.schabi.newpipe.extractor.services.disco.extractors.DiscoStreamExtractor;
import org.schabi.newpipe.extractor.services.disco.extractors.DiscoSubscriptionExtractor;
import org.schabi.newpipe.extractor.services.disco.extractors.DiscoSuggestionExtractor;
import org.schabi.newpipe.extractor.services.disco.extractors.DiscoTrendingExtractor;
import org.schabi.newpipe.extractor.services.disco.linkHandler.DiscoChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.disco.linkHandler.DiscoChannelTabLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.disco.linkHandler.DiscoCommentsLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.disco.linkHandler.DiscoPlaylistLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.disco.linkHandler.DiscoSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.services.disco.linkHandler.DiscoStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.disco.linkHandler.DiscoTrendingLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import javax.annotation.Nonnull;
import java.util.List;

/*
 * Created by Christian Schabesberger on 23.08.15.
 *
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * DiscoService.java is part of NewPipe.
 *
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

public class DiscoService extends StreamingService {
    public WatchDataCache watchDataCache = new WatchDataCache();

    public DiscoService(final int id) {
        super(id, "YouTube", asList(AUDIO, VIDEO, LIVE, COMMENTS, BULLET_COMMENTS, SPONSORBLOCK));
    }

    @Override
    public String getBaseUrl() {
        return "https://youtube.com";
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() {
        return DiscoStreamLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() {
        return DiscoChannelLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelTabLHFactory() {
        return DiscoChannelTabLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getPlaylistLHFactory() {
        return DiscoPlaylistLinkHandlerFactory.getInstance();
    }

    @Override
    public SearchQueryHandlerFactory getSearchQHFactory() {
        return DiscoSearchQueryHandlerFactory.getInstance();
    }

    @Override
    public StreamExtractor getStreamExtractor(final LinkHandler linkHandler) {
        return new DiscoStreamExtractor(this, linkHandler, watchDataCache);
    }

    @Override
    public ChannelExtractor getChannelExtractor(final ListLinkHandler linkHandler) {
        return new DiscoChannelExtractor(this, linkHandler);
    }

    @Override
    public ChannelTabExtractor getChannelTabExtractor(final ListLinkHandler linkHandler) {
        return new DiscoChannelTabExtractor(this, linkHandler);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(final ListLinkHandler linkHandler) {
        if (DiscoParsingHelper.isDiscoMixId(linkHandler.getId())) {
            return new DiscoMixPlaylistExtractor(this, linkHandler);
        } else {
            return new DiscoPlaylistExtractor(this, linkHandler);
        }
    }

    @Override
    public SearchExtractor getSearchExtractor(final SearchQueryHandler query) {
        final List<FilterItem> contentFilters = query.getContentFilters();

        if (contentFilters.isEmpty()) {
            // something is odd
            throw new RuntimeException("contentFilters is empty. WHY?");
        }

        final FilterItem filterItem = contentFilters.get(0);
        if (filterItem instanceof DiscoFilters.MusicDiscoContentFilterItem) {
            return new DiscoMusicSearchExtractor(this, query);
        } else {
            return new DiscoSearchExtractor(this, query);
        }
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return new DiscoSuggestionExtractor(this);
    }

    @Override
    public KioskList getKioskList() throws ExtractionException {
        final KioskList list = new KioskList(this);

        // add kiosks here e.g.:
        try {
            list.addKioskEntry(
                    (streamingService, url, id) -> new DiscoTrendingExtractor(
                            DiscoService.this,
                            new DiscoTrendingLinkHandlerFactory().fromUrl(url),
                            id
                    ),
                    new DiscoTrendingLinkHandlerFactory(),
                    "Recommended Lives"
            );
            list.setDefaultKiosk("Recommended Lives");
        } catch (final Exception e) {
            throw new ExtractionException(e);
        }

        return list;
    }

    @Override
    public SubscriptionExtractor getSubscriptionExtractor() {
        return new DiscoSubscriptionExtractor(this);
    }

    @Nonnull
    @Override
    public FeedExtractor getFeedExtractor(final String channelUrl) throws ExtractionException {
        return new DiscoFeedExtractor(this, getChannelLHFactory().fromUrl(channelUrl));
    }

    @Override
    public ListLinkHandlerFactory getCommentsLHFactory() {
        return DiscoCommentsLinkHandlerFactory.getInstance();
    }

    @Override
    public CommentsExtractor getCommentsExtractor(final ListLinkHandler urlIdHandler)
            throws ExtractionException {
        return new DiscoCommentsExtractor(this, urlIdHandler);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Localization
    //////////////////////////////////////////////////////////////////////////*/

    // https://www.youtube.com/picker_ajax?action_language_json=1
    // Using "zu" (Zulu) to prevent Disco from auto-translating video titles
    private static final List<Localization> SUPPORTED_LANGUAGES = Localization.listFrom(
            "zu"
    );

    // https://www.youtube.com/picker_ajax?action_country_json=1
    private static final List<ContentCountry> SUPPORTED_COUNTRIES = ContentCountry.listFrom(
            "DZ", "AR", "AU", "AT", "AZ", "BH", "BD", "BY", "BE", "BO", "BA", "BR", "BG", "CA",
            "CL", "CO", "CR", "HR", "CY", "CZ", "DK", "DO", "EC", "EG", "SV", "EE", "FI", "FR",
            "GE", "DE", "GH", "GR", "GT", "HN", "HK", "HU", "IS", "IN", "ID", "IQ", "IE", "IL",
            "IT", "JM", "JP", "JO", "KZ", "KE", "KW", "LV", "LB", "LY", "LI", "LT", "LU", "MY",
            "MT", "MX", "ME", "MA", "NP", "NL", "NZ", "NI", "NG", "MK", "NO", "OM", "PK", "PA",
            "PG", "PY", "PE", "PH", "PL", "PT", "PR", "QA", "RO", "RU", "SA", "SN", "RS", "SG",
            "SK", "SI", "ZA", "KR", "ES", "LK", "SE", "CH", "TW", "TZ", "TH", "TN", "TR", "UG",
            "UA", "AE", "GB", "US", "UY", "VE", "VN", "YE", "ZW"
    );

    @Override
    public List<Localization> getSupportedLocalizations() {
        return SUPPORTED_LANGUAGES;
    }

    @Override
    public Localization getLocalization() {
        return new Localization("zu");
    }

    @Override
    public List<ContentCountry> getSupportedCountries() {
        return SUPPORTED_COUNTRIES;
    }

    @Override
    public BulletCommentsExtractor getBulletCommentsExtractor(ListLinkHandler linkHandler) throws ExtractionException {
        return new DiscoBulletCommentsExtractor(this, linkHandler, watchDataCache);
    }

    @Override
    public ListLinkHandlerFactory getBulletCommentsLHFactory() {
        return new DiscoBulletCommentsLinkHandlerFactory();
    }
}
