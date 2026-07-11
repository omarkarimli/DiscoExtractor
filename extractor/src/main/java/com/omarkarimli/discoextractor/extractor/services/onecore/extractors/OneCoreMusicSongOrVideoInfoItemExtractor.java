package com.omarkarimli.discoextractor.extractor.services.onecore.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.localization.DateWrapper;
import com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper;
import com.omarkarimli.discoextractor.extractor.stream.StreamInfoItemExtractor;
import com.omarkarimli.discoextractor.extractor.stream.StreamType;
import com.omarkarimli.discoextractor.extractor.utils.Parser;
import com.omarkarimli.discoextractor.extractor.utils.Utils;

import javax.annotation.Nonnull;

import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getTextFromObject;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getImagesFromThumbnailsArray;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getUrlFromNavigationEndpoint;
import static com.omarkarimli.discoextractor.extractor.services.onecore.linkHandler.OneCoreSearchQueryHandlerFactory.MUSIC_SONGS;
import static com.omarkarimli.discoextractor.extractor.services.onecore.linkHandler.OneCoreSearchQueryHandlerFactory.MUSIC_VIDEOS;
import static com.omarkarimli.discoextractor.extractor.utils.Utils.isNullOrEmpty;

public class OneCoreMusicSongOrVideoInfoItemExtractor implements StreamInfoItemExtractor {
    private final JsonObject songOrVideoInfoItem;
    private final JsonArray descriptionElements;
    private final String searchType;

    public OneCoreMusicSongOrVideoInfoItemExtractor(final JsonObject songOrVideoInfoItem,
                                                    final JsonArray descriptionElements,
                                                    final String searchType) {
        this.songOrVideoInfoItem = songOrVideoInfoItem;
        this.descriptionElements = descriptionElements;
        this.searchType = searchType;
    }


    @Override
    public String getUrl() throws ParsingException {
        final String id = songOrVideoInfoItem.getObject("playlistItemData").getString("videoId");
        if (!isNullOrEmpty(id)) {
            return "https://music.youtube.com/watch?v=" + id;
        }
        throw new ParsingException("Could not get URL");
    }

    @Override
    public String getName() throws ParsingException {
        final String name = getTextFromObject(songOrVideoInfoItem.getArray("flexColumns")
                .getObject(0)
                .getObject("musicResponsiveListItemFlexColumnRenderer")
                .getObject("text"));
        if (!isNullOrEmpty(name)) {
            return Utils.replaceAllCustom(name);
        }
        throw new ParsingException("Could not get name");
    }

    @Override
    public StreamType getStreamType() {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() {
        return false;
    }

    @Override
    public long getDuration() throws ParsingException {
        final String duration = descriptionElements.getObject(descriptionElements.size() - 1)
                .getString("text");
        if (!isNullOrEmpty(duration)) {
            return OneCoreParsingHelper.parseDurationString(duration);
        }
        throw new ParsingException("Could not get duration");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        final String name = descriptionElements.getObject(0).getString("text");
        if (!isNullOrEmpty(name)) {
            return Utils.replaceAllCustom(name);
        }
        throw new ParsingException("Could not get uploader name");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        if (searchType.equals(MUSIC_VIDEOS)) {
            final JsonArray items = songOrVideoInfoItem.getObject("menu")
                    .getObject("menuRenderer")
                    .getArray("items");
            for (final Object item : items) {
                final JsonObject menuNavigationItemRenderer =
                        ((JsonObject) item).getObject("menuNavigationItemRenderer");
                if (menuNavigationItemRenderer.getObject("icon")
                        .getString("iconType", "")
                        .equals("ARTIST")) {
                    return getUrlFromNavigationEndpoint(
                            menuNavigationItemRenderer.getObject("navigationEndpoint"));
                }
            }

            return null;
        } else {
            final JsonObject navigationEndpointHolder = songOrVideoInfoItem.getArray("flexColumns")
                    .getObject(1)
                    .getObject("musicResponsiveListItemFlexColumnRenderer")
                    .getObject("text")
                    .getArray("runs")
                    .getObject(0);

            if (!navigationEndpointHolder.has("navigationEndpoint")) {
                return null;
            }

            final String url = getUrlFromNavigationEndpoint(
                    navigationEndpointHolder.getObject("navigationEndpoint"));

            if (!isNullOrEmpty(url)) {
                return url;
            }

            throw new ParsingException("Could not get uploader URL");
        }
    }

    @Override
    public boolean isUploaderVerified() {
        // We don't have the ability to know this information on OneCore Music
        return false;
    }

    @Override
    public String getTextualUploadDate() {
        return null;
    }

    @Override
    public DateWrapper getUploadDate() {
        return null;
    }

    @Override
    public long getViewCount() throws ParsingException {
        if (searchType.equals(MUSIC_SONGS)) {
            return -1;
        }
        final String viewCount = descriptionElements
                .getObject(descriptionElements.size() - 3)
                .getString("text");
        if (!isNullOrEmpty(viewCount)) {
            try {
                return Utils.mixedNumberWordToLong(viewCount);
            } catch (final Parser.RegexException e) {
                // probably viewCount == "No views" or similar
                return 0;
            }
        }
        throw new ParsingException("Could not get view count");
    }

    @Nonnull
    @Override
    public String getThumbnailUrl() throws ParsingException {
        try {
            return getImagesFromThumbnailsArray(
                    songOrVideoInfoItem.getObject("thumbnail")
                            .getObject("musicThumbnailRenderer")
                            .getObject("thumbnail")
                            .getArray("thumbnails")).get(0).getUrl();
        } catch (final Exception e) {
            throw new ParsingException("Could not get thumbnails", e);
        }
    }
}
