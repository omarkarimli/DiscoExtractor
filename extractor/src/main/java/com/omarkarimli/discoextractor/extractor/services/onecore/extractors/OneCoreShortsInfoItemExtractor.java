package com.omarkarimli.discoextractor.extractor.services.onecore.extractors;

import com.grack.nanojson.JsonObject;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.localization.DateWrapper;
import com.omarkarimli.discoextractor.extractor.stream.StreamInfoItemExtractor;
import com.omarkarimli.discoextractor.extractor.stream.StreamType;
import com.omarkarimli.discoextractor.extractor.utils.Utils;

import javax.annotation.Nullable;

public class OneCoreShortsInfoItemExtractor implements StreamInfoItemExtractor {
    public JsonObject item;
    public OneCoreShortsInfoItemExtractor(JsonObject item) {
        this.item = item;
    }
    @Override
    public String getName() throws ParsingException {
        return item.getObject("overlayMetadata").getObject("primaryText").getString("content");
    }

    @Override
    public String getUrl() throws ParsingException {
        return "https://youtube.com" + item.getObject("onTap").getObject("innertubeCommand")
                .getObject("commandMetadata").getObject("webCommandMetadata").getString("url");
    }

    @Override
    public String getThumbnailUrl() throws ParsingException {
        return item.getObject("thumbnail").getArray("sources").getObject(0).getString("url");
    }

    @Override
    public StreamType getStreamType() throws ParsingException {
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public long getDuration() throws ParsingException {
        return 0;
    }

    @Override
    public long getViewCount() throws ParsingException {
        try {
            return Utils.mixedNumberWordToLong(item.getObject("overlayMetadata").getObject("secondaryText").getString("content").split(" view")[0]);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public String getUploaderName() throws ParsingException {
        return null;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() throws ParsingException {
        return null;
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        return null;
    }
}
