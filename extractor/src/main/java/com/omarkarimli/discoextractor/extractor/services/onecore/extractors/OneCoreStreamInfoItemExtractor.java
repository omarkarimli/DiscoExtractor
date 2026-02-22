package com.omarkarimli.discoextractor.extractor.services.onecore.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.localization.DateWrapper;
import com.omarkarimli.discoextractor.extractor.localization.TimeAgoParser;
import com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper;
import com.omarkarimli.discoextractor.extractor.services.onecore.linkHandler.OneCoreStreamLinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.stream.StreamInfoItemExtractor;
import com.omarkarimli.discoextractor.extractor.stream.StreamType;
import com.omarkarimli.discoextractor.extractor.utils.JsonUtils;
import com.omarkarimli.discoextractor.extractor.utils.Utils;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getTextFromObject;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getThumbnailUrlFromInfoItem;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getUrlFromNavigationEndpoint;
import static com.omarkarimli.discoextractor.extractor.utils.Utils.isNullOrEmpty;

public class OneCoreStreamInfoItemExtractor implements StreamInfoItemExtractor {
    private final JsonObject videoInfo;
    private final TimeAgoParser timeAgoParser;
    private StreamType cachedStreamType;

    /**
     * Creates an extractor of StreamInfoItems from a YouTube page.
     *
     * @param videoInfoItem The JSON page element
     * @param timeAgoParser A parser of the textual dates or {@code null}.
     */
    public OneCoreStreamInfoItemExtractor(final JsonObject videoInfoItem,
                                          @Nullable final TimeAgoParser timeAgoParser) {
        this.videoInfo = videoInfoItem;
        this.timeAgoParser = timeAgoParser;
    }

    @Override
    public StreamType getStreamType() {
        if (cachedStreamType != null) {
            return cachedStreamType;
        }

        final JsonArray badges = videoInfo.getArray("badges");
        for (final Object badge : badges) {
            final JsonObject badgeRenderer
                    = ((JsonObject) badge).getObject("metadataBadgeRenderer");
            if (badgeRenderer.getString("style", "").equals("BADGE_STYLE_TYPE_LIVE_NOW")
                    || badgeRenderer.getString("label", "").equals("LIVE NOW")) {
                cachedStreamType = StreamType.LIVE_STREAM;
                return cachedStreamType;
            }
        }

        for (final Object overlay : videoInfo.getArray("thumbnailOverlays")) {
            final String style = ((JsonObject) overlay)
                    .getObject("thumbnailOverlayTimeStatusRenderer")
                    .getString("style", "");
            if (style.equalsIgnoreCase("LIVE")) {
                cachedStreamType = StreamType.LIVE_STREAM;
                return cachedStreamType;
            }
        }

        cachedStreamType = StreamType.VIDEO_STREAM;
        return cachedStreamType;
    }

    @Override
    public boolean isAd() throws ParsingException {
        return isPremium() || getName().equals("[Private video]")
                || getName().equals("[Deleted video]");
    }

    @Override
    public String getUrl() throws ParsingException {
        try {
            final String videoId = videoInfo.getString("videoId");
            return OneCoreStreamLinkHandlerFactory.getInstance().getUrl(videoId);
        } catch (final Exception e) {
            throw new ParsingException("Could not get url", e);
        }
    }

    @Override
    public String getName() throws ParsingException {
        String name = getTextFromObject(videoInfo.getObject("title"));
        if (!isNullOrEmpty(name)) {
            return Utils.replaceMany(name);
        }

        name = getTextFromObject(videoInfo.getObject("headline"));
        if (!isNullOrEmpty(name)) {
            return Utils.replaceMany(name);
        }

        throw new ParsingException("Could not get name");
    }

    @Override
    public long getDuration() throws ParsingException {
        if (getStreamType() == StreamType.LIVE_STREAM || isPremiere()) {
            return -1;
        }

        String duration = getTextFromObject(videoInfo.getObject("lengthText"));

        if (isNullOrEmpty(duration)) {
            for (final Object thumbnailOverlay : videoInfo.getArray("thumbnailOverlays")) {
                if (((JsonObject) thumbnailOverlay).has("thumbnailOverlayTimeStatusRenderer")) {
                    duration = getTextFromObject(((JsonObject) thumbnailOverlay)
                            .getObject("thumbnailOverlayTimeStatusRenderer").getObject("text"));
                }
            }

            if (isNullOrEmpty(duration)) {
                // Duration of short videos in channel tab
                // example: "simple is best - 49 seconds - play video"
                final String accessibilityLabel = videoInfo.getObject("accessibility")
                        .getObject("accessibilityData").getString("label");
                if (accessibilityLabel == null || timeAgoParser == null) {
                    return 0;
                }

                final String[] labelParts = accessibilityLabel.split(" \u2013 ");

                if (labelParts.length > 2) {
                    final String textualDuration = labelParts[labelParts.length - 2];
                    return timeAgoParser.parseDuration(textualDuration);
                } else {
                    return 0; // some videos don't have durations at all
                }
            }
        }

        // NewPipe#8034 - YT returns not a correct duration for "YT shorts" videos
        if ("SHORTS".equalsIgnoreCase(duration)) {
            return 0;
        }

        return OneCoreParsingHelper.parseDurationString(duration);
    }

    @Override
    public String getUploaderName() throws ParsingException {
        String name = getTextFromObject(videoInfo.getObject("longBylineText"));

        if (isNullOrEmpty(name)) {
            name = getTextFromObject(videoInfo.getObject("ownerText"));

            if (isNullOrEmpty(name)) {
                name = getTextFromObject(videoInfo.getObject("shortBylineText"));

                if (isNullOrEmpty(name)) {
                    throw new ParsingException("Could not get uploader name");
                }
            }
        }

        return Utils.replaceMany(name);
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        String url = getUrlFromNavigationEndpoint(videoInfo.getObject("longBylineText")
                .getArray("runs").getObject(0).getObject("navigationEndpoint"));

        if (isNullOrEmpty(url)) {
            url = getUrlFromNavigationEndpoint(videoInfo.getObject("ownerText")
                    .getArray("runs").getObject(0).getObject("navigationEndpoint"));

            if (isNullOrEmpty(url)) {
                url = getUrlFromNavigationEndpoint(videoInfo.getObject("shortBylineText")
                        .getArray("runs").getObject(0).getObject("navigationEndpoint"));

                if (isNullOrEmpty(url)) {
                    throw new ParsingException("Could not get uploader url");
                }
            }
        }

        return url;
    }

    @Nullable
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {

        if (videoInfo.has("channelThumbnailSupportedRenderers")) {
            return JsonUtils.getArray(videoInfo, "channelThumbnailSupportedRenderers"
                            + ".channelThumbnailWithLinkRenderer.thumbnail.thumbnails")
                    .getObject(0).getString("url");
        }

        if (videoInfo.has("channelThumbnail")) {
            return JsonUtils.getArray(videoInfo, "channelThumbnail.thumbnails")
                    .getObject(0).getString("url");
        }

        return null;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return OneCoreParsingHelper.isVerified(videoInfo.getArray("ownerBadges"));
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        if (getStreamType().equals(StreamType.LIVE_STREAM)) {
            return null;
        }

        if (isPremiere()) {
            return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(getDateFromPremiere());
        }

        final String publishedTimeText
                = getTextFromObject(videoInfo.getObject("publishedTimeText"));
        if (!isNullOrEmpty(publishedTimeText)) {
            return Utils.replaceMany(publishedTimeText);
        }

        final String shortsTimestampText = getTextFromObject(videoInfo
                .getObject("navigationEndpoint")
                .getObject("reelWatchEndpoint").getObject("overlay")
                .getObject("reelPlayerOverlayRenderer")
                .getObject("reelPlayerHeaderSupportedRenderers")
                .getObject("reelPlayerHeaderRenderer")
                .getObject("timestampText")
        );
        if (!isNullOrEmpty(shortsTimestampText)) {
            return Utils.replaceMany(shortsTimestampText);
        }

        return null;
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        if (getStreamType().equals(StreamType.LIVE_STREAM)) {
            return null;
        }

        if (isPremiere()) {
            return new DateWrapper(getDateFromPremiere());
        }

        final String textualUploadDate = getTextualUploadDate();
        if (timeAgoParser != null && !isNullOrEmpty(textualUploadDate)) {
            try {
                return timeAgoParser.parse(textualUploadDate);
            } catch (final ParsingException e) {
                throw new ParsingException("Could not get upload date", e);
            }
        }
        return null;
    }

    @Override
    public long getViewCount() throws ParsingException {
        try {
            if (videoInfo.has("topStandaloneBadge") || isPremium()) {
                return -1;
            }

            if (!videoInfo.has("viewCountText")) {
                // This object is null when a video has its views hidden.
                return -1;
            }

            final String viewCount = getTextFromObject(videoInfo.getObject("viewCountText"));

            if (viewCount.toLowerCase().contains("no views")) {
                return 0;
            } else if (viewCount.toLowerCase().contains("recommended")) {
                return -1;
            }

            return Long.parseLong(Utils.removeNonDigitCharacters(viewCount));
        } catch (final Exception e) {
            throw new ParsingException("Could not get view count", e);
        }
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return getThumbnailUrlFromInfoItem(videoInfo);
    }

    private boolean isPremium() {
        final JsonArray badges = videoInfo.getArray("badges");
        for (final Object badge : badges) {
            if (((JsonObject) badge).getObject("metadataBadgeRenderer")
                    .getString("label", "").equals("Premium")) {
                return true;
            }
        }
        return false;
    }

    private boolean isPremiere() {
        return videoInfo.has("upcomingEventData");
    }

    private OffsetDateTime getDateFromPremiere() throws ParsingException {
        final JsonObject upcomingEventData = videoInfo.getObject("upcomingEventData");
        final String startTime = upcomingEventData.getString("startTime");

        try {
            return OffsetDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(startTime)),
                    ZoneOffset.UTC);
        } catch (final Exception e) {
            throw new ParsingException("Could not parse date from premiere: \"" + startTime + "\"");
        }
    }

    @Nullable
    @Override
    public String getShortDescription() throws ParsingException {

        if (videoInfo.has("detailedMetadataSnippets")) {
            return Utils.replaceMany(getTextFromObject(videoInfo.getArray("detailedMetadataSnippets")
                    .getObject(0).getObject("snippetText")));
        }

        if (videoInfo.has("descriptionSnippet")) {
            return Utils.replaceMany(getTextFromObject(videoInfo.getObject("descriptionSnippet")));
        }

        return null;
    }
    @Override
    public boolean isShortFormContent() throws ParsingException {
        try {
            final String webPageType = videoInfo.getObject("navigationEndpoint")
                    .getObject("commandMetadata").getObject("webCommandMetadata")
                    .getString("webPageType");

            boolean isShort = !isNullOrEmpty(webPageType)
                    && webPageType.equals("WEB_PAGE_TYPE_SHORTS");

            if (!isShort) {
                isShort = videoInfo.getObject("navigationEndpoint").has("reelWatchEndpoint");
            }

            if (!isShort) {
                final JsonObject thumbnailTimeOverlay = videoInfo.getArray("thumbnailOverlays")
                        .stream()
                        .filter(JsonObject.class::isInstance)
                        .map(JsonObject.class::cast)
                        .filter(thumbnailOverlay -> thumbnailOverlay.has(
                                "thumbnailOverlayTimeStatusRenderer"))
                        .map(thumbnailOverlay -> thumbnailOverlay.getObject(
                                "thumbnailOverlayTimeStatusRenderer"))
                        .findFirst()
                        .orElse(null);

                if (!isNullOrEmpty(thumbnailTimeOverlay)) {
                    isShort = thumbnailTimeOverlay.getString("style", "")
                            .equalsIgnoreCase("SHORTS")
                            || thumbnailTimeOverlay.getObject("icon")
                            .getString("iconType", "")
                            .toLowerCase()
                            .contains("shorts");
                }
            }

            return isShort;
        } catch (final Exception e) {
            throw new ParsingException("Could not determine if this is short-form content", e);
        }
    }

    @Override
    public boolean requiresMembership() throws ParsingException {
        final JsonArray badges = videoInfo.getArray("badges");
        for (final Object badge : badges) {
            if (((JsonObject) badge).getObject("metadataBadgeRenderer")
                    .getString("style", "").equals("BADGE_STYLE_TYPE_MEMBERS_ONLY")) {
                return true;
            }
        }
        return false;
    }

}
