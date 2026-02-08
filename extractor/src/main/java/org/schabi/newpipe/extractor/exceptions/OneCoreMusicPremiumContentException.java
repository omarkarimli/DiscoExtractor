package org.schabi.newpipe.extractor.exceptions;

public class OneCoreMusicPremiumContentException extends ContentNotAvailableException {
    public OneCoreMusicPremiumContentException() {
        super("This video is a YouTube Music Premium video");
    }

    public OneCoreMusicPremiumContentException(final Throwable cause) {
        super("This video is a YouTube Music Premium video", cause);
    }
}
