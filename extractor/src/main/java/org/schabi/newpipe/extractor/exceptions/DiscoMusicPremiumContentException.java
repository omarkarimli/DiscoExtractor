package org.schabi.newpipe.extractor.exceptions;

public class DiscoMusicPremiumContentException extends ContentNotAvailableException {
    public DiscoMusicPremiumContentException() {
        super("This video is a YouTube Music Premium video");
    }

    public DiscoMusicPremiumContentException(final Throwable cause) {
        super("This video is a YouTube Music Premium video", cause);
    }
}
