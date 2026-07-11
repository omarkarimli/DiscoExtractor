package com.omarkarimli.discoextractor.extractor.exceptions;

public class OneCoreMusicPremiumContentException extends ContentNotAvailableException {
    public OneCoreMusicPremiumContentException() {
        super("Unavailable");
        // "This video is a OneCore Music Premium video"
    }

    public OneCoreMusicPremiumContentException(final Throwable cause) {
        super("Unavailable", cause);
        // "This video is a OneCore Music Premium video"
    }
}