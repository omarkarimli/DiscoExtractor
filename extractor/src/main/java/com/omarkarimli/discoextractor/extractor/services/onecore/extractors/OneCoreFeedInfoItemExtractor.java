package com.omarkarimli.discoextractor.extractor.services.onecore.extractors;

import org.jsoup.nodes.Element;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.localization.DateWrapper;
import com.omarkarimli.discoextractor.extractor.stream.StreamInfoItemExtractor;
import com.omarkarimli.discoextractor.extractor.stream.StreamType;
import com.omarkarimli.discoextractor.extractor.utils.Utils;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

public class OneCoreFeedInfoItemExtractor implements StreamInfoItemExtractor {
    private final Element entryElement;

    public OneCoreFeedInfoItemExtractor(final Element entryElement) {
        this.entryElement = entryElement;
    }

    @Override
    public StreamType getStreamType() {
        // It is not possible to determine the stream type using the feed endpoint.
        // All entries are considered a video stream.
        return StreamType.VIDEO_STREAM;
    }

    @Override
    public boolean isAd() {
        return false;
    }

    @Override
    public long getDuration() {
        // Not available when fetching through the feed endpoint.
        return -1;
    }

    @Override
    public long getViewCount() {
        return Long.parseLong(entryElement.getElementsByTag("media:statistics").first()
                .attr("views"));
    }

    @Override
    public String getUploaderName() {
        return Utils.replaceAllCustom(entryElement.select("author > name").first().text());
    }

    @Override
    public String getUploaderUrl() {
        return entryElement.select("author > uri").first().text();
    }

    @Nullable
    @Override
    public String getUploaderAvatarUrl() throws ParsingException {
        return null;
    }

    @Override
    public boolean isUploaderVerified() throws ParsingException {
        return false;
    }

    @Nullable
    @Override
    public String getTextualUploadDate() {
        return Utils.replaceAllCustom(entryElement.getElementsByTag("published").first().text());
    }

    @Nullable
    @Override
    public DateWrapper getUploadDate() throws ParsingException {
        try {
            return new DateWrapper(OffsetDateTime.parse(entryElement.getElementsByTag("published").first().text()));
        } catch (final DateTimeParseException e) {
            throw new ParsingException("Could not parse date (\"" + getTextualUploadDate() + "\")",
                    e);
        }
    }

    @Override
    public String getName() {
        return Utils.replaceAllCustom(entryElement.getElementsByTag("title").first().text());
    }

    @Override
    public String getUrl() {
        return entryElement.getElementsByTag("link").first().attr("href");
    }

    @Override
    public String getThumbnailUrl() {
        // The hqdefault thumbnail has some black bars at the top and at the bottom, while the
        // mqdefault doesn't, so return the mqdefault one. It should always exist, according to
        // https://stackoverflow.com/a/20542029/9481500.
        return entryElement.getElementsByTag("media:thumbnail").first().attr("url")
                .replace("hqdefault", "mqdefault");
    }

    @Override
    public boolean isShortFormContent() throws ParsingException {
        return getUrl().contains("shorts");
    }
}
