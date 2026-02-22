package com.omarkarimli.discoextractor.extractor.services.onecore.extractors;

import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.extractPlaylistTypeFromPlaylistUrl;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getTextFromObject;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getThumbnailUrlFromInfoItem;
import static com.omarkarimli.discoextractor.extractor.utils.Utils.isNullOrEmpty;

import com.grack.nanojson.JsonObject;

import com.omarkarimli.discoextractor.extractor.ListExtractor;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.playlist.PlaylistInfo;
import com.omarkarimli.discoextractor.extractor.playlist.PlaylistInfoItemExtractor;
import com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper;
import com.omarkarimli.discoextractor.extractor.utils.Utils;

import javax.annotation.Nonnull;

public class OneCoreMixOrPlaylistInfoItemExtractor implements PlaylistInfoItemExtractor {
    private final JsonObject mixInfoItem;

    public OneCoreMixOrPlaylistInfoItemExtractor(final JsonObject mixInfoItem) {
        this.mixInfoItem = mixInfoItem;
    }

    @Override
    public String getName() throws ParsingException {
        final String name = getTextFromObject(mixInfoItem.getObject("title"));
        if (isNullOrEmpty(name)) {
            throw new ParsingException("Could not get name");
        }
        return Utils.replaceMany(name);
    }

    @Override
    public String getUrl() throws ParsingException {
        final String url = mixInfoItem.getString("shareUrl");
        if (isNullOrEmpty(url)) {
            throw new ParsingException("Could not get url");
        }
        return url;
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return getThumbnailUrlFromInfoItem(mixInfoItem);
    }

    @Override
    public String getUploaderName() throws ParsingException {
        // this will be "YouTube" for mixes
        return Utils.replaceMany(OneCoreParsingHelper.getTextFromObject(mixInfoItem.getObject("longBylineText")));
    }

    @Override
    public long getStreamCount() throws ParsingException {
        final String countString = OneCoreParsingHelper.getTextFromObject(
                mixInfoItem.getObject("videoCountShortText"));
        if (countString == null) {
            throw new ParsingException("Could not extract item count for playlist/mix info item");
        }

        try {
            return Integer.parseInt(countString);
        } catch (final NumberFormatException ignored) {
            // un-parsable integer: this is a mix with infinite items and "50+" as count string
            // (though onecore music mixes do not necessarily have an infinite count of songs)
            return ListExtractor.ITEM_COUNT_INFINITE;
        }
    }

    @Nonnull
    @Override
    public PlaylistInfo.PlaylistType getPlaylistType() throws ParsingException {
        return extractPlaylistTypeFromPlaylistUrl(getUrl());
    }
}
