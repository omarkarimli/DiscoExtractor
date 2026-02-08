package com.omarkarimli.discoextractor.extractor.services.onecore.extractors;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;

import com.omarkarimli.discoextractor.extractor.bulletComments.BulletCommentsInfoItem;
import com.omarkarimli.discoextractor.extractor.bulletComments.BulletCommentsInfoItemExtractor;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;

import java.time.Duration;

public class OneCoreBulletCommentsInfoItemExtractor implements BulletCommentsInfoItemExtractor {
    private final JsonObject data;
    private long startTime;
    private long offsetDuration; // the expected offset of the comment from the start of the video
    public OneCoreBulletCommentsInfoItemExtractor(JsonObject item, long startTime, long offsetDuration) {
        data = item;
        this.startTime = startTime;
        this.offsetDuration = offsetDuration;
    }

    @Override
    public String getCommentText() throws ParsingException {
        JsonArray array = data.getObject("message").getArray("runs");
        StringBuilder result = new StringBuilder();
        for(int i = 0; i< array.size(); i++){
            if(array.getObject(i).has("text")){
                result.append(array.getObject(i).getString("text"));
            }
        }
        return result.toString().replaceAll("□", "");
    }

    @Override
    public int getArgbColor() throws ParsingException {
        return BulletCommentsInfoItemExtractor.super.getArgbColor();
    }

    @Override
    public BulletCommentsInfoItem.Position getPosition() throws ParsingException {
        return BulletCommentsInfoItem.Position.REGULAR;
    }

    @Override
    public double getRelativeFontSize() throws ParsingException {
        return BulletCommentsInfoItemExtractor.super.getRelativeFontSize();
    }

    @Override
    public Duration getDuration() throws ParsingException {
       // return Duration.ofMillis(Long.parseLong(data.getString("timestampUsec"))/1000 - startTime);
        return offsetDuration == -1 ? Duration.ZERO : Duration.ofMillis(offsetDuration);
    }

    @Override
    public boolean isLive() throws ParsingException {
        return true;
    }
}
