package com.omarkarimli.discoextractor.extractor.services.niconico.extractors;

import com.omarkarimli.discoextractor.extractor.bulletComments.BulletCommentsInfoItemExtractor;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.services.niconico.protobuf.BulletComment;

import java.time.Duration;

public class NiconicoBulletCommentsProtoInfoItemExtractor implements BulletCommentsInfoItemExtractor {
    BulletComment.MessageItem object;
    long startAt;
    NiconicoBulletCommentsProtoInfoItemExtractor(BulletComment.MessageItem object) {
        this.object = object;
    }
    @Override
    public String getCommentText() throws ParsingException {
        return object.message.text_message.text;
    }

    @Override
    public Duration getDuration() throws ParsingException {
        return Duration.ofMillis(object.message.text_message.time_to_now - 6000);
    }
}
