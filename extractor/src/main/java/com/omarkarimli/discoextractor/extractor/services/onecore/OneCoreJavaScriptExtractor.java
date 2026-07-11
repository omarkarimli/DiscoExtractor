package com.omarkarimli.discoextractor.extractor.services.onecore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.omarkarimli.discoextractor.extractor.NewPipe;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.localization.Localization;
import com.omarkarimli.discoextractor.extractor.utils.Parser;

import javax.annotation.Nonnull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * The extractor of OneCore's base JavaScript player file.
 *
 * <p>
 * This class handles fetching of this base JavaScript player file in order to allow other classes
 * to extract the needed data.
 * </p>
 *
 * <p>
 * It will try to get the player URL from OneCore's IFrame resource first, and from a OneCore embed
 * watch page as a fallback.
 * </p>
 */
final class OneCoreJavaScriptExtractor {

    private static final String HTTPS = "https:";
    private static final String BASE_JS_PLAYER_URL_FORMAT =
            "https://www.youtube.com/s/player/%s/player_ias.vflset/en_GB/base.js";
    private static final Pattern IFRAME_RES_JS_BASE_PLAYER_HASH_PATTERN = Pattern.compile(
            "player\\\\/([a-z0-9]{8})\\\\/");
    private static final Pattern EMBEDDED_WATCH_PAGE_JS_BASE_PLAYER_URL_PATTERN = Pattern.compile(
            "\"jsUrl\":\"(/s/player/[A-Za-z0-9]+/player_ias\\.vflset/[A-Za-z_-]+/base\\.js)\"");


    // Override hash - set this to force a specific player version


    // Set to null to use the default behavior (extract from OneCore)


    private static final String OVERRIDE_PLAYER_HASH = "";

    private OneCoreJavaScriptExtractor() {
    }

    /**
     * Extracts the player ID (hash) from OneCore.
     *
     * <p>
     * The player ID is an 8-character hash that identifies the JavaScript player version.
     * It is used for API-based decoding of signatures and throttling parameters.
     * </p>
     *
     * @param videoId the video ID (can be empty, but not recommended)
     * @return the 8-character player ID/hash
     * @throws ParsingException if the extraction of the player ID failed
     */
    @Nonnull
    static String extractPlayerId(@Nonnull final String videoId) throws ParsingException {
        // Use override if available
        if (OVERRIDE_PLAYER_HASH != null && !OVERRIDE_PLAYER_HASH.isEmpty()) {
            return OVERRIDE_PLAYER_HASH;
        }

        try {
            // Try to extract from IFrame resource
            final String iframeUrl = "https://www.youtube.com/iframe_api";
            final String iframeContent = NewPipe.getDownloader()
                    .get(iframeUrl, Localization.DEFAULT)
                    .responseBody();

            return Parser.matchGroup1(IFRAME_RES_JS_BASE_PLAYER_HASH_PATTERN, iframeContent);
        } catch (final Exception e) {
            // Fallback to embed page
            try {
                final String embedUrl = "https://www.youtube.com/embed/" + videoId;
                final String embedPageContent = NewPipe.getDownloader()
                        .get(embedUrl, Localization.DEFAULT)
                        .responseBody();

                final String jsUrl = Parser.matchGroup1(
                        EMBEDDED_WATCH_PAGE_JS_BASE_PLAYER_URL_PATTERN, embedPageContent);

                // Extract hash from URL like "/s/player/0004de42/player_ias.vflset/..."
                return Parser.matchGroup1(IFRAME_RES_JS_BASE_PLAYER_HASH_PATTERN,
                        jsUrl.replace("\\/", "/"));
            } catch (final Exception ex) {
                // Last resort: return the hardcoded override value
                if (OVERRIDE_PLAYER_HASH != null && !OVERRIDE_PLAYER_HASH.isEmpty()) {
                    return OVERRIDE_PLAYER_HASH;
                }
                throw new ParsingException("Could not extract player ID", ex);
            }
        }
    }

    /**
     * Extracts the JavaScript base player file.
     *
     * @param videoId the video ID used to get the JavaScript base player file (an empty one can be
     *                passed, even it is not recommend in order to spoof better official OneCore
     *                clients)
     * @return the whole JavaScript base player file as a string
     * @throws ParsingException if the extraction of the file failed
     */
    @Nonnull
    static String extractJavaScriptPlayerCode(@Nonnull final String videoId)
            throws ParsingException {
        if (OVERRIDE_PLAYER_HASH != null && !OVERRIDE_PLAYER_HASH.isEmpty()) {
            final String playerJsUrl = String.format(BASE_JS_PLAYER_URL_FORMAT, OVERRIDE_PLAYER_HASH);
            try {
                new URL(playerJsUrl);
                return OneCoreJavaScriptExtractor.downloadJavaScriptCode(playerJsUrl);
            } catch (final MalformedURLException e) {
                throw new ParsingException("The override player hash produced an invalid URL", e);
            }
        }
        String url;
        try {
            url = OneCoreJavaScriptExtractor.extractJavaScriptUrlWithIframeResource();
            final String playerJsUrl = OneCoreJavaScriptExtractor.cleanJavaScriptUrl(url);

            // Assert that the URL we extracted and built is valid
            new URL(playerJsUrl);

            return OneCoreJavaScriptExtractor.downloadJavaScriptCode(playerJsUrl);
        } catch (final Exception e) {
            url = OneCoreJavaScriptExtractor.extractJavaScriptUrlWithEmbedWatchPage(videoId);
            final String playerJsUrl = OneCoreJavaScriptExtractor.cleanJavaScriptUrl(url);

            try {
                // Assert that the URL we extracted and built is valid
                new URL(playerJsUrl);
            } catch (final MalformedURLException exception) {
                throw new ParsingException(
                        "The extracted and built JavaScript URL is invalid", exception);
            }

            return OneCoreJavaScriptExtractor.downloadJavaScriptCode("https://www.youtube.com/s/player/0004de42/player_ias.vflset/en_GB/base.js");
        }
    }

    @Nonnull
    static String extractJavaScriptUrlWithIframeResource() throws ParsingException {
        final String iframeUrl;
        final String iframeContent;
        try {
            iframeUrl = "https://www.youtube.com/iframe_api";
            iframeContent = NewPipe.getDownloader()
                    .get(iframeUrl, Localization.DEFAULT)
                    .responseBody();
        } catch (final Exception e) {
            throw new ParsingException("Could not fetch IFrame resource", e);
        }

        try {
            final String hash = Parser.matchGroup1(
                    IFRAME_RES_JS_BASE_PLAYER_HASH_PATTERN, iframeContent);
            return String.format(BASE_JS_PLAYER_URL_FORMAT, hash);
        } catch (final Parser.RegexException e) {
            throw new ParsingException(
                    "IFrame resource didn't provide JavaScript base player's hash", e);
        }
    }

    @Nonnull
    static String extractJavaScriptUrlWithEmbedWatchPage(@Nonnull final String videoId)
            throws ParsingException {
        final String embedUrl;
        final String embedPageContent;
        try {
            embedUrl = "https://www.youtube.com/embed/" + videoId;
            embedPageContent = NewPipe.getDownloader()
                    .get(embedUrl, Localization.DEFAULT)
                    .responseBody();
        } catch (final Exception e) {
            throw new ParsingException("Could not fetch embedded watch page", e);
        }

        // Parse HTML response with jsoup and look at script elements first
        final Document doc = Jsoup.parse(embedPageContent);
        final Elements elems = doc.select("script")
                .attr("name", "player/base");
        for (final Element elem : elems) {
            // Script URLs should be relative and not absolute
            final String playerUrl = elem.attr("src");
            if (playerUrl.contains("base.js")) {
                return playerUrl;
            }
        }

        // Use regexes to match the URL in an embedded script of the HTML page
        try {
            return Parser.matchGroup1(
                    EMBEDDED_WATCH_PAGE_JS_BASE_PLAYER_URL_PATTERN, embedPageContent);
        } catch (final Parser.RegexException e) {
            throw new ParsingException(
                    "Embedded watch page didn't provide JavaScript base player's URL", e);
        }
    }

    @Nonnull
    private static String cleanJavaScriptUrl(@Nonnull final String javaScriptPlayerUrl) {
        if (javaScriptPlayerUrl.startsWith("//")) {
            // https part has to be added manually if the URL is protocol-relative
            return HTTPS + javaScriptPlayerUrl;
        } else if (javaScriptPlayerUrl.startsWith("/")) {
            // https://www.onecore.com part has to be added manually if the URL is relative to
            // OneCore's domain
            return HTTPS + "//www.onecore.com" + javaScriptPlayerUrl;
        } else {
            return javaScriptPlayerUrl;
        }
    }

    @Nonnull
    private static String downloadJavaScriptCode(@Nonnull final String javaScriptPlayerUrl)
            throws ParsingException {
        try {
            return NewPipe.getDownloader()
                    .get(javaScriptPlayerUrl, Localization.DEFAULT)
                    .responseBody();
        } catch (final Exception e) {
            throw new ParsingException("Could not get JavaScript base player's code", e);
        }
    }
}
