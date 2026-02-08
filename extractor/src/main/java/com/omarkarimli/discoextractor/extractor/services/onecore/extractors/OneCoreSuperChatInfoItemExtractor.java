package com.omarkarimli.discoextractor.extractor.services.onecore.extractors;

import com.grack.nanojson.JsonObject;

import com.omarkarimli.discoextractor.extractor.bulletComments.BulletCommentsInfoItem;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;

public class OneCoreSuperChatInfoItemExtractor extends OneCoreBulletCommentsInfoItemExtractor {
    private JsonObject data;
    public OneCoreSuperChatInfoItemExtractor(JsonObject item, long startTime, long offsetDuration) {
        super(item, startTime, offsetDuration);
        data = item;
    }

    @Override
    public String getCommentText() throws ParsingException {
        String superResult = super.getCommentText();
        if(superResult.length() == 0){
            return "";
        }
        return String.format("(%s) ", data.getObject("purchaseAmountText")
                .getString("simpleText")) + super.getCommentText();
    }

    @Override
    public int getArgbColor() throws ParsingException {
        return (int) data.getLong("bodyBackgroundColor");
    }

    @Override
    public BulletCommentsInfoItem.Position getPosition() throws ParsingException {
        return BulletCommentsInfoItem.Position.SUPERCHAT;
    }
}
