package org.schabi.newpipe.extractor.services.onecore;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.bulletComments.BulletCommentsExtractor;
import org.schabi.newpipe.extractor.search.filter.FilterItem;
import org.schabi.newpipe.extractor.services.onecore.extractors.OneCoreBulletCommentsExtractor;
import org.schabi.newpipe.extractor.services.onecore.linkHandler.OneCoreBulletCommentsLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.onecore.search.filter.OneCoreFilters;

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
import org.schabi.newpipe.extractor.services.onecore.extractors.OneCoreChannelExtractor;
import org.schabi.newpipe.extractor.services.onecore.extractors.OneCoreChannelTabExtractor;
import org.schabi.newpipe.extractor.services.onecore.extractors.OneCoreCommentsExtractor;
import org.schabi.newpipe.extractor.services.onecore.extractors.OneCoreFeedExtractor;
import org.schabi.newpipe.extractor.services.onecore.extractors.OneCoreMixPlaylistExtractor;
import org.schabi.newpipe.extractor.services.onecore.extractors.OneCoreMusicSearchExtractor;
import org.schabi.newpipe.extractor.services.onecore.extractors.OneCorePlaylistExtractor;
import org.schabi.newpipe.extractor.services.onecore.extractors.OneCoreSearchExtractor;
import org.schabi.newpipe.extractor.services.onecore.extractors.OneCoreStreamExtractor;
import org.schabi.newpipe.extractor.services.onecore.extractors.OneCoreSubscriptionExtractor;
import org.schabi.newpipe.extractor.services.onecore.extractors.OneCoreSuggestionExtractor;
import org.schabi.newpipe.extractor.services.onecore.extractors.OneCoreTrendingExtractor;
import org.schabi.newpipe.extractor.services.onecore.linkHandler.OneCoreChannelLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.onecore.linkHandler.OneCoreChannelTabLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.onecore.linkHandler.OneCoreCommentsLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.onecore.linkHandler.OneCorePlaylistLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.onecore.linkHandler.OneCoreSearchQueryHandlerFactory;
import org.schabi.newpipe.extractor.services.onecore.linkHandler.OneCoreStreamLinkHandlerFactory;
import org.schabi.newpipe.extractor.services.onecore.linkHandler.OneCoreTrendingLinkHandlerFactory;
import org.schabi.newpipe.extractor.stream.StreamExtractor;
import org.schabi.newpipe.extractor.subscription.SubscriptionExtractor;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import javax.annotation.Nonnull;
import java.util.List;

/*
 * Created by Christian Schabesberger on 23.08.15.
 *
 * Copyright (C) Christian Schabesberger 2018 <chris.schabesberger@mailbox.org>
 * YoutubeService.java is part of NewPipe.
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

public class OneCoreService extends StreamingService {
    public WatchDataCache watchDataCache = new WatchDataCache();

    public OneCoreService(final int id) {
        super(id, "YouTube", asList(AUDIO, VIDEO, LIVE, COMMENTS, BULLET_COMMENTS, SPONSORBLOCK));
    }

    @Override
    public String getBaseUrl() {
        return "https://youtube.com";
    }

    @Override
    public LinkHandlerFactory getStreamLHFactory() {
        return OneCoreStreamLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelLHFactory() {
        return OneCoreChannelLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getChannelTabLHFactory() {
        return OneCoreChannelTabLinkHandlerFactory.getInstance();
    }

    @Override
    public ListLinkHandlerFactory getPlaylistLHFactory() {
        return OneCorePlaylistLinkHandlerFactory.getInstance();
    }

    @Override
    public SearchQueryHandlerFactory getSearchQHFactory() {
        return OneCoreSearchQueryHandlerFactory.getInstance();
    }

    @Override
    public StreamExtractor getStreamExtractor(final LinkHandler linkHandler) {
        return new OneCoreStreamExtractor(this, linkHandler, watchDataCache);
    }

    @Override
    public ChannelExtractor getChannelExtractor(final ListLinkHandler linkHandler) {
        return new OneCoreChannelExtractor(this, linkHandler);
    }

    @Override
    public ChannelTabExtractor getChannelTabExtractor(final ListLinkHandler linkHandler) {
        return new OneCoreChannelTabExtractor(this, linkHandler);
    }

    @Override
    public PlaylistExtractor getPlaylistExtractor(final ListLinkHandler linkHandler) {
        if (OneCoreParsingHelper.isOneCoreMixId(linkHandler.getId())) {
            return new OneCoreMixPlaylistExtractor(this, linkHandler);
        } else {
            return new OneCorePlaylistExtractor(this, linkHandler);
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
        if (filterItem instanceof OneCoreFilters.MusicOneCoreContentFilterItem) {
            return new OneCoreMusicSearchExtractor(this, query);
        } else {
            return new OneCoreSearchExtractor(this, query);
        }
    }

    @Override
    public SuggestionExtractor getSuggestionExtractor() {
        return new OneCoreSuggestionExtractor(this);
    }

    @Override
    public KioskList getKioskList() throws ExtractionException {
        final KioskList list = new KioskList(this);

        // add kiosks here e.g.:
        try {
            list.addKioskEntry(
                    (streamingService, url, id) -> new OneCoreTrendingExtractor(
                            OneCoreService.this,
                            new OneCoreTrendingLinkHandlerFactory().fromUrl(url),
                            id
                    ),
                    new OneCoreTrendingLinkHandlerFactory(),
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
        return new OneCoreSubscriptionExtractor(this);
    }

    @Nonnull
    @Override
    public FeedExtractor getFeedExtractor(final String channelUrl) throws ExtractionException {
        return new OneCoreFeedExtractor(this, getChannelLHFactory().fromUrl(channelUrl));
    }

    @Override
    public ListLinkHandlerFactory getCommentsLHFactory() {
        return OneCoreCommentsLinkHandlerFactory.getInstance();
    }

    @Override
    public CommentsExtractor getCommentsExtractor(final ListLinkHandler urlIdHandler)
            throws ExtractionException {
        return new OneCoreCommentsExtractor(this, urlIdHandler);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Localization
    //////////////////////////////////////////////////////////////////////////*/

    // https://www.youtube.com/picker_ajax?action_language_json=1
    private static final List<Localization> SUPPORTED_LANGUAGES = Localization.listFrom(
            "en-GB"
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

    private static final List<Localization> SUPPORTED_LANGUAGES_FULL = Localization.listFrom(
            "af", "am", "ar", "az", "be", "bg", "bn", "bs", "ca", "cs", "da", "de",
            "el", "en", "en-GB", "es", "es-419", "es-US", "et", "eu", "fa", "fi", "fil", "fr",
            "fr-CA", "gl", "gu", "hi", "hr", "hu", "hy", "id", "is", "it", "iw", "ja",
            "ka", "kk", "km", "kn", "ko", "ky", "lo", "lt", "lv", "mk", "ml", "mn",
            "mr", "ms", "my", "ne", "nl", "no", "pa", "pl", "pt", "pt-PT", "ro", "ru",
            "si", "sk", "sl", "sq", "sr", "sr-Latn", "sv", "sw", "ta", "te", "th", "tr",
            "uk", "ur", "uz", "vi", "zh-CN", "zh-HK", "zh-TW", "zu"
    );

    public static Localization getTempLocalization() {
        final Localization preferredLocalization = NewPipe.getPreferredLocalization();

        // Check the localization's language and country
        if (SUPPORTED_LANGUAGES_FULL.contains(preferredLocalization)) {
            return preferredLocalization;
        }

        // Fallback to the first supported language that matches the preferred language
        for (final Localization supportedLanguage : SUPPORTED_LANGUAGES_FULL) {
            if (supportedLanguage.getLanguageCode()
                    .equals(preferredLocalization.getLanguageCode())) {
                return supportedLanguage;
            }
        }

        return Localization.DEFAULT;
    }

    @Override
    public List<Localization> getSupportedLocalizations() {
        return SUPPORTED_LANGUAGES;
    }

    @Override
    public List<ContentCountry> getSupportedCountries() {
        return SUPPORTED_COUNTRIES;
    }

    @Override
    public BulletCommentsExtractor getBulletCommentsExtractor(ListLinkHandler linkHandler) throws ExtractionException {
        return new OneCoreBulletCommentsExtractor(this, linkHandler, watchDataCache);
    }

    @Override
    public ListLinkHandlerFactory getBulletCommentsLHFactory() {
        return new OneCoreBulletCommentsLinkHandlerFactory();
    }
}
