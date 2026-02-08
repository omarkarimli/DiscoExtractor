package com.omarkarimli.discoextractor.extractor.services.niconico.linkHandler;

import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.LinkHandlerFactory;
import com.omarkarimli.discoextractor.extractor.services.niconico.NiconicoService;
import com.omarkarimli.discoextractor.extractor.utils.Parser;

import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.NonNull;

public class NiconicoStreamLinkHandlerFactory extends LinkHandlerFactory {
    @NonNull
    @Override
    public String getId(final String url) throws ParsingException {
        if(url.contains("live.nicovideo.jp")){
            return url;
        }
        return Parser.matchGroup(NiconicoService.SMILEVIDEO, url, 2).split(Pattern.quote("?"))[0];
    }

    @Override
    public String getUrl(final String id) throws ParsingException {
        if(id.contains("live.nicovideo.jp")){
            return id;
        }
        return NiconicoService.WATCH_URL + id;
    }

    @Override
    public boolean onAcceptUrl(final String url) throws ParsingException {
        try {
            getId(url);
            return true;
        } catch (final ParsingException e) {
            return false;
        }
    }
}
