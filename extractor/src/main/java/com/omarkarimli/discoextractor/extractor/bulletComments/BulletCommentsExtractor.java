package com.omarkarimli.discoextractor.extractor.bulletComments;

import javax.annotation.Nonnull;

import com.omarkarimli.discoextractor.extractor.ListExtractor;
import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;

import java.io.IOException;
import java.util.List;

public abstract class BulletCommentsExtractor extends ListExtractor<BulletCommentsInfoItem> {
    public BulletCommentsExtractor(final StreamingService service, final ListLinkHandler uiHandler) {
        super(service, uiHandler);
    }

    @Nonnull
    @Override
    public String getName() throws ParsingException {
        return "BulletComments";
    }

    // No need to be overridden normally as it is enough for bullet comments to be handled in getInitialPage
    @Override
    public InfoItemsPage<BulletCommentsInfoItem> getPage(Page page) throws IOException, ExtractionException {
        return null;
    }

    public List<BulletCommentsInfoItem> getLiveMessages() throws ParsingException {
        return null;
    }

    // return false if all the bullet comments can be fetched in one go, true if they need to be fetched continuously
    public boolean isLive() {
        return false;
    }

    public boolean isDisabled() {
        return false;
    }

    // Must be overriden if your BC extractor needs to fetch bullet comments continuously
    public void disconnect() {

    }

    // Must be overriden if your BC extractor needs to fetch bullet comments continuously
    public void reconnect() {

    }

    // May be useful if your BC extractor needs to fetch bullet comments continuously and requires the current play position
    public void setCurrentPlayPosition(long currentPlayPosition) {
    }

    public void clearMappingState() {
    }
}
