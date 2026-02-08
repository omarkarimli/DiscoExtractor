package com.omarkarimli.discoextractor.extractor.services.onecore;

import com.grack.nanojson.JsonObject;

public class OneCoreBulletCommentPair {
    private final JsonObject data;
    private final long offsetDuration; // the expected offset of the comment from the start of the video
    public OneCoreBulletCommentPair(JsonObject item, long offsetDuration) {
        this.offsetDuration = offsetDuration;
        this.data = item;
    }

    public JsonObject getData() {
        return data;
    }

    public long getOffsetDuration() {
        return offsetDuration;
    }
}
