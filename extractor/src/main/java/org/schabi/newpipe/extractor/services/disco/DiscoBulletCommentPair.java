package org.schabi.newpipe.extractor.services.disco;

import com.grack.nanojson.JsonObject;

public class DiscoBulletCommentPair {
    private final JsonObject data;
    private final long offsetDuration; // the expected offset of the comment from the start of the video
    public DiscoBulletCommentPair(JsonObject item, long offsetDuration) {
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
