package com.omarkarimli.discoextractor.extractor.services.bilibili.extractors;

import java.io.IOException;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import com.omarkarimli.discoextractor.extractor.*;
import com.omarkarimli.discoextractor.extractor.InfoItem;
import com.omarkarimli.discoextractor.extractor.MultiInfoItemsCollector;
import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.downloader.Downloader;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.linkhandler.SearchQueryHandler;
import com.omarkarimli.discoextractor.extractor.search.SearchExtractor;
import static com.omarkarimli.discoextractor.extractor.services.bilibili.BilibiliService.getDefaultCookies;
import static com.omarkarimli.discoextractor.extractor.services.bilibili.BilibiliService.getHeaders;

public class BilibiliSearchExtractor extends SearchExtractor{

    private JsonObject searchCollection;

    public BilibiliSearchExtractor(StreamingService service, SearchQueryHandler linkHandler) {
        super(service, linkHandler);
    }

    @Override
    public InfoItemsPage<InfoItem> getInitialPageInternal() throws IOException, ExtractionException {
        if(searchCollection.getObject("data").getArray("result").isEmpty()){
            return new InfoItemsPage<>(new MultiInfoItemsCollector(getServiceId()), null);
        }
        int currentPage = 1;
        String nextPage = getUrl().replace(String.format("page=%s", 1), String.format("page=%s", String.valueOf(currentPage + 1)));
        return new InfoItemsPage<>(getCommittedCollector(), new Page(nextPage, getDefaultCookies()));
    }

    private MultiInfoItemsCollector getCommittedCollector(){
        JsonArray result = searchCollection.getObject("data").getArray("result");
        final MultiInfoItemsCollector collector = new MultiInfoItemsCollector(getServiceId());
        for (int i = 0; i< result.size(); i++) {
            String type = result.getObject(i).getString("type");
            switch (type){
                case "video":
                    collector.commit(new BilibiliStreamInfoItemExtractor(result.getObject(i)));
                    break;
                case "live_room":
                    collector.commit(new BilibiliLiveInfoItemExtractor(result.getObject(i), 0));
                    break;
                case "bili_user":
                    collector.commit(new BilibiliSearchResultChannelInfoItemExtractor(result.getObject(i)));
                    break;
                case "media_bangumi":
                case "media_ft":
                    collector.commit(new BilibiliPremiumContentInfoItemExtractor(result.getObject(i)));
            }
        }
        return collector;
    }

    @Override
    public InfoItemsPage<InfoItem> getPageInternal(Page page) throws IOException, ExtractionException {
        try {
            final String html = getDownloader().get(page.getUrl(), getHeaders(getOriginalUrl())).responseBody();
            searchCollection = JsonParser.object().from(html);
        } catch (JsonParserException e) {
            e.printStackTrace();
        }

        if(searchCollection.getObject("data").getArray("result").isEmpty()){
            return new InfoItemsPage<>(new MultiInfoItemsCollector(getServiceId()), null);
        }

        String currentPageString = page.getUrl().split("page=")[page.getUrl().split("page=").length-1];
        int currentPage = Integer.parseInt(currentPageString);
        String nextPage = page.getUrl().replace(String.format("page=%s", currentPageString), String.format("page=%s", String.valueOf(currentPage + 1)));
        return new InfoItemsPage<>(getCommittedCollector(), new Page(nextPage, getDefaultCookies()));
    }

    @Override
    public void onFetchPage(Downloader downloader) throws IOException, ExtractionException {
        try {
            final String response = getDownloader().get(
                    getLinkHandler().getUrl(), getHeaders(getOriginalUrl())).responseBody();
            searchCollection = JsonParser.object().from(response);
        } catch (final JsonParserException e) {
            throw new ExtractionException("could not parse search results.");
        }
    }
}
