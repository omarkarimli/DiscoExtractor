package com.omarkarimli.discoextractor.extractor;

import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;

public interface InfoItemExtractor {
    String getName() throws ParsingException;
    String getUrl() throws ParsingException;
    String getThumbnailUrl() throws ParsingException;
}
