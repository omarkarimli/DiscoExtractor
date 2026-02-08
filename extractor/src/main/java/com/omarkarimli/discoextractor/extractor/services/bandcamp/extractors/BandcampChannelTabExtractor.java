package com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.omarkarimli.discoextractor.extractor.InfoItem;
import com.omarkarimli.discoextractor.extractor.MultiInfoItemsCollector;
import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.channel.ChannelTabExtractor;
import com.omarkarimli.discoextractor.extractor.downloader.Downloader;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.ChannelTabs;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.extractors.streaminfoitem.BandcampDiscographStreamInfoItemExtractor;
import com.omarkarimli.discoextractor.extractor.services.bandcamp.linkHandler.BandcampChannelTabHandler;

import javax.annotation.Nonnull;
import java.io.IOException;

public class BandcampChannelTabExtractor extends ChannelTabExtractor {
    private JsonArray discography;
    private final String filter;
    public BandcampChannelTabExtractor(final StreamingService service,
                                       final ListLinkHandler linkHandler) {
        super(service, linkHandler);

        final String tab = linkHandler.getContentFilters().get(0).getName();
        switch (tab) {
            case ChannelTabs.TRACKS:
                filter = "track";
                break;
            case ChannelTabs.ALBUMS:
                filter = "album";
                break;
            default:
                throw new IllegalArgumentException("Unsupported channel tab: " + tab);
        }
    }

    @Nonnull
    private JsonArray getDiscographs() throws ExtractionException {
        final ListLinkHandler tabHandler = getLinkHandler();
        if (tabHandler instanceof BandcampChannelTabHandler) {
            return ((BandcampChannelTabHandler) tabHandler).getDiscographs();
        } else {
            final JsonObject artistDetails = BandcampExtractorHelper.getArtistDetails(getId());
            return artistDetails.getArray("discography");
        }
    }

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader) throws ParsingException {
        if (discography == null) {
            discography = BandcampExtractorHelper.getArtistDetails(getId())
                    .getArray("discography");
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<InfoItem> getInitialPage() throws IOException, ExtractionException {
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());

        for (final Object discograph : discography) {
            // A discograph is as an item appears in a discography
            if (!(discograph instanceof JsonObject)) {
                continue;
            }

            final JsonObject discographJsonObject = (JsonObject) discograph;
            final String itemType = discographJsonObject.getString("item_type", "");

            if (!itemType.equals(filter)) {
                continue;
            }

            switch (itemType) {
                case "track":
                    collector.commit(new BandcampDiscographStreamInfoItemExtractor(
                            discographJsonObject, getUrl()));
                    break;
                case "album":
                    collector.commit(new BandcampAlbumInfoItemExtractor(
                            discographJsonObject, getUrl()));
                    break;
            }
        }

        return new InfoItemsPage<>(collector, null);
    }

    @Override
    public InfoItemsPage<InfoItem> getPage(final Page page) {
        return null;
    }
}
