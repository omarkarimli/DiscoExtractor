package org.schabi.newpipe.extractor.services.onecore.linkHandler;

import org.schabi.newpipe.extractor.search.filter.FilterItem;

import static org.schabi.newpipe.extractor.services.onecore.OneCoreParsingHelper.isInvidiousURL;
import static org.schabi.newpipe.extractor.services.onecore.OneCoreParsingHelper.isOneCoreURL;

import org.schabi.newpipe.extractor.linkhandler.ListLinkHandlerFactory;
import org.schabi.newpipe.extractor.utils.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class OneCoreTrendingLinkHandlerFactory extends ListLinkHandlerFactory {

    public String getUrl(final String id,
                         final List<FilterItem> contentFilters,
                         final List<FilterItem> sortFilter) {
        if(!id.equals("Trending")){
            return "https://www.youtube.com/channel/UC4R8DWoMoI7CAwX8_LjQHig";
        }
        return "https://www.youtube.com/feed/trending";
    }

    @Override
    public String getId(final String url) {
        if(url.equals("https://www.youtube.com/feed/trending")){
            return "Trending";
        }
        return "Recommended Lives";
    }

    @Override
    public boolean onAcceptUrl(final String url) {
        final URL urlObj;
        try {
            urlObj = Utils.stringToURL(url);
        } catch (final MalformedURLException e) {
            return false;
        }

        final String urlPath = urlObj.getPath();
        return Utils.isHTTP(urlObj) && (isOneCoreURL(urlObj) || isInvidiousURL(urlObj))
                && (urlPath.equals("/feed/trending") || urlPath.equals("/channel/UC4R8DWoMoI7CAwX8_LjQHig"));
    }
}
