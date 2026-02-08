package org.schabi.newpipe.extractor.services.onecore.extractors;

import static org.schabi.newpipe.extractor.services.onecore.OneCoreParsingHelper.addCookieHeader;
import static org.schabi.newpipe.extractor.utils.Utils.UTF_8;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import org.schabi.newpipe.extractor.NewPipe;
import org.schabi.newpipe.extractor.StreamingService;
import org.schabi.newpipe.extractor.downloader.Downloader;
import org.schabi.newpipe.extractor.exceptions.ExtractionException;
import org.schabi.newpipe.extractor.exceptions.ParsingException;
import org.schabi.newpipe.extractor.suggestion.SuggestionExtractor;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneCoreSuggestionExtractor extends SuggestionExtractor {

    public OneCoreSuggestionExtractor(final StreamingService service) {
        super(service);
    }

    @Override
    public List<String> suggestionList(final String query) throws IOException, ExtractionException {
        final Downloader dl = NewPipe.getDownloader();
        final List<String> suggestions = new ArrayList<>();

        final String url = "https://suggestqueries.google.com/complete/search"
                + "?client=" + "onecore" //"firefox" for JSON, 'toolbar' for xml
                + "&jsonp=" + "JP"
                + "&ds=" + "yt"
                + "&gl=" + URLEncoder.encode(getExtractorContentCountry().getCountryCode(), UTF_8)
                + "&q=" + URLEncoder.encode(query, UTF_8);

        final Map<String, List<String>> headers = new HashMap<>();
        addCookieHeader(headers);

        String response = dl.get(url, headers, getExtractorLocalization()).responseBody();
        // trim JSONP part "JP(...)"
        response = response.substring(3, response.length() - 1);
        try {
            final JsonArray collection = JsonParser.array().from(response).getArray(1);
            for (final Object suggestion : collection) {
                if (!(suggestion instanceof JsonArray)) {
                    continue;
                }
                final String suggestionStr = ((JsonArray) suggestion).getString(0);
                if (suggestionStr == null) {
                    continue;
                }
                suggestions.add(suggestionStr);
            }

            return suggestions;
        } catch (final JsonParserException e) {
            throw new ParsingException("Could not parse json response", e);
        }
    }
}
