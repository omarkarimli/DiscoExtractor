package com.omarkarimli.discoextractor.extractor.services.media_ccc.extractors;

import com.omarkarimli.discoextractor.extractor.search.filter.FilterItem;
import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import com.omarkarimli.discoextractor.extractor.InfoItem;
import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.channel.ChannelInfoItem;
import com.omarkarimli.discoextractor.extractor.channel.ChannelInfoItemExtractor;
import com.omarkarimli.discoextractor.extractor.downloader.Downloader;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.linkhandler.SearchQueryHandler;
import com.omarkarimli.discoextractor.extractor.MultiInfoItemsCollector;
import com.omarkarimli.discoextractor.extractor.search.SearchExtractor;
import com.omarkarimli.discoextractor.extractor.services.media_ccc.extractors.infoItems.MediaCCCStreamInfoItemExtractor;
import com.omarkarimli.discoextractor.extractor.services.media_ccc.linkHandler.MediaCCCConferencesListLinkHandlerFactory;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nonnull;

import static com.omarkarimli.discoextractor.extractor.services.media_ccc.search.filter.MediaCCCFilters.ALL;
import static com.omarkarimli.discoextractor.extractor.services.media_ccc.search.filter.MediaCCCFilters.CONFERENCES;
import static com.omarkarimli.discoextractor.extractor.services.media_ccc.search.filter.MediaCCCFilters.EVENTS;

public class MediaCCCSearchExtractor extends SearchExtractor {
    private JsonObject doc;
    private MediaCCCConferenceKiosk conferenceKiosk;

    public MediaCCCSearchExtractor(final StreamingService service,
                                   final SearchQueryHandler linkHandler) {
        super(service, linkHandler);
        try {
            conferenceKiosk = new MediaCCCConferenceKiosk(service,
                    new MediaCCCConferencesListLinkHandlerFactory().fromId("conferences"),
                    "conferences");
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private FilterItem getContentFilter() {
        // TODO check if getLinkHandler().getContentFilters() not empty
        final FilterItem filterItem = getLinkHandler().getContentFilters().get(0);

        return filterItem;
    }
    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPageInternal() {
        final MultiInfoItemsCollector searchItems = new MultiInfoItemsCollector(getServiceId());


        final FilterItem filterItem = getContentFilter();

        if (filterItem.getName().contains(CONFERENCES) || filterItem.getName().equals(ALL)) {
            searchConferences(getSearchString(),
                    conferenceKiosk.getInitialPage().getItems(),
                    searchItems);
        }

        if (filterItem.getName().equals(EVENTS) || filterItem.getName().equals(ALL)) {
            final JsonArray events = doc.getArray("events");
            for (int i = 0; i < events.size(); i++) {
                // Ensure only uploaded talks are shown in the search results.
                // If the release date is null, the talk has not been held or uploaded yet
                // and no streams are going to be available anyway.
                if (events.getObject(i).getString("release_date") != null) {
                    searchItems.commit(new MediaCCCStreamInfoItemExtractor(
                            events.getObject(i)));
                }
            }
        }
        return new InfoItemsPage<>(searchItems, null);
    }

    @Override
    public InfoItemsPage<InfoItem> getPageInternal(final Page page) {
        return InfoItemsPage.emptyPage();
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {

        final FilterItem filterItem = getContentFilter();

        if (filterItem.getName().contains(EVENTS) || filterItem.getName().equals(ALL)) {
            final String site;
            final String url = getUrl();
            site = downloader.get(url, getExtractorLocalization()).responseBody();
            try {
                doc = JsonParser.object().from(site);
            } catch (final JsonParserException jpe) {
                throw new ExtractionException("Could not parse JSON.", jpe);
            }
        }

        if (filterItem.getName().contains(CONFERENCES) || filterItem.getName().equals(ALL)) {
            conferenceKiosk.fetchPage();
        }
    }

    private void searchConferences(final String searchString,
                                   final List<ChannelInfoItem> channelItems,
                                   final MultiInfoItemsCollector collector) {
        for (final ChannelInfoItem item : channelItems) {
            if (item.getName().toUpperCase().contains(
                    searchString.toUpperCase())) {
                collector.commit(new ChannelInfoItemExtractor() {
                    @Override
                    public String getDescription() {
                        return item.getDescription();
                    }

                    @Override
                    public long getSubscriberCount() {
                        return item.getSubscriberCount();
                    }

                    @Override
                    public long getStreamCount() {
                        return item.getStreamCount();
                    }

                    @Override
                    public boolean isVerified() {
                        return false;
                    }

                    @Override
                    public String getName() {
                        return item.getName();
                    }

                    @Override
                    public String getUrl() {
                        return item.getUrl();
                    }

                    @Override
                    public String getThumbnailUrl() {
                        return item.getThumbnailUrl();
                    }
                });
            }
        }
    }
}
