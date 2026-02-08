package com.omarkarimli.discoextractor.extractor.services.niconico.extractors;

import com.grack.nanojson.JsonObject;

import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.localization.DateWrapper;
import com.omarkarimli.discoextractor.extractor.services.niconico.NiconicoService;
import com.omarkarimli.discoextractor.extractor.services.niconico.NiconicoServiceParsingHelper;
import com.omarkarimli.discoextractor.extractor.stream.StreamInfoItemExtractor;
import com.omarkarimli.discoextractor.extractor.stream.StreamType;

import javax.annotation.Nullable;

public class NiconicoStreamInfoItemExtractor implements StreamInfoItemExtractor {
    protected final JsonObject item;
    public NiconicoStreamInfoItemExtractor(final JsonObject json) {
        item = json;
    }

    @Override
    public String getName() throws ParsingException {
        return item.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return NiconicoService.WATCH_URL + item.getString("contentId");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return item.getString("thumbnailUrl");
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() throws ParsingException {
        return false;
    }

    @Override
    public long getDuration() throws ParsingException {
        return item.getLong("lengthSeconds");
    }

    @Override
    public long getViewCount() throws ParsingException {
        return item.getLong("viewCounter");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        // Snapshot search API could not get uploader name.
        if (isChannel()) {
            return "ch" + item.getLong("channelId");
        }
        return "user/" + item.getLong("userId");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        if (isChannel()) {
            return NiconicoService.CHANNEL_URL + "ch" + item.getLong("channelId");
        }
        return NiconicoService.USER_URL + item.getLong("userId");
    }

    @Nullable
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return null;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return item.getString("startTime");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return NiconicoServiceParsingHelper.parseSnapshotDateTime(item.getString("startTime"));
    }

    private boolean isChannel() {
        return item.has("channelId") && item.getLong("channelId") != 0;
    }
}
