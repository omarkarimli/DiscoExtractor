package com.omarkarimli.discoextractor.extractor.services.niconico.extractors;

import com.grack.nanojson.JsonObject;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.localization.DateWrapper;
import com.omarkarimli.discoextractor.extractor.stream.StreamInfoItemExtractor;
import com.omarkarimli.discoextractor.extractor.stream.StreamType;

import javax.annotation.Nullable;

import java.time.OffsetDateTime;

import static com.omarkarimli.discoextractor.extractor.services.niconico.NiconicoService.*;

public class NiconicoSeriesContentItemExtractor implements StreamInfoItemExtractor {

    private final JsonObject data;

    public NiconicoSeriesContentItemExtractor(JsonObject data) {
        this.data = data;
    }

    @Override
    public String getName() throws ParsingException {
        return data.getString("title");
    }

    @Override
    public String getUrl() throws ParsingException {
        return WATCH_URL + data.getString("id");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return data.getObject("thumbnail").getString("largeUrl");
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public long getDuration() throws ParsingException {
        return data.getLong("duration");
    }

    @Override
    public long getViewCount() throws ParsingException {
        return data.getObject("count").getLong("view");
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return data.getObject("owner").getString("name");
    }

    @Override
    public String getUploaderUrl() throws ParsingException {
        JsonObject owner = data.getObject("owner");
        if(owner.getString("ownerType").equals("user")){
            return USER_URL + owner.getString("id");
        } else {
            return CHANNEL_URL + owner.getString("id");
        }
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return data.getString("registeredAt");
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        try {
            return new DateWrapper(OffsetDateTime.parse(getTextualUploadDate()));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean requiresMembership() throws ParsingException {
        return data.getBoolean("isPaymentRequired");
    }
}
