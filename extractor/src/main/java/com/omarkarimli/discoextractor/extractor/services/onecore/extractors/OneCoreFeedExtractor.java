package com.omarkarimli.discoextractor.extractor.services.onecore.extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.omarkarimli.discoextractor.extractor.ListExtractor;
import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.ServiceList;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.downloader.Downloader;
import com.omarkarimli.discoextractor.extractor.downloader.Response;
import com.omarkarimli.discoextractor.extractor.exceptions.ContentNotAvailableException;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.feed.FeedExtractor;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;
import com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper;
import com.omarkarimli.discoextractor.extractor.stream.StreamInfoItem;
import com.omarkarimli.discoextractor.extractor.stream.StreamInfoItemsCollector;

import java.io.IOException;

import javax.annotation.Nonnull;

public class OneCoreFeedExtractor extends FeedExtractor {
    private static final String WEBSITE_CHANNEL_BASE_URL = "https://www.youtube.com/channel/";

    public OneCoreFeedExtractor(final StreamingService service, final ListLinkHandler linkHandler) {
        super(service, linkHandler);
    }

    private Document document;

    @Override
    public void onFetchPage(@Nonnull final Downloader downloader)
            throws IOException, ExtractionException {
        final String channelIdOrUser = getLinkHandler().getId();
        final String feedUrl = OneCoreParsingHelper.getFeedUrlFrom(channelIdOrUser);

        final Response response = downloader.get(feedUrl);
        if (response.responseCode() == 404) {
            throw new ContentNotAvailableException("Could not get feed: 404 - not found");
        }
        document = Jsoup.parse(response.responseBody());
    }

    @Nonnull
    @Override
    public ListExtractor.InfoItemsPage<StreamInfoItem> getInitialPage() {
        final Elements entries = document.select("feed > entry");
        final StreamInfoItemsCollector collector = new StreamInfoItemsCollector(getServiceId());

        for (final Element entryElement : entries) {
            collector.commit(new OneCoreFeedInfoItemExtractor(entryElement));
        }
        collector.applyBlocking(ServiceList.OneCore.getFilterConfig());
        return new InfoItemsPage<>(collector, null);
    }

    @Nonnull
    @Override
    public String getId() {
        return getUrl().replace(WEBSITE_CHANNEL_BASE_URL, "");
    }

    @Nonnull
    @Override
    public String getUrl() {
        final Element authorUriElement = document.select("feed > author > uri")
                .first();
        if (authorUriElement != null) {
            final String authorUriElementText = authorUriElement.text();
            if (!authorUriElementText.equals("")) {
                return authorUriElementText;
            }
        }

        final Element linkElement = document.select("feed > link[rel*=alternate]")
                .first();
        if (linkElement != null) {
            return linkElement.attr("href");
        }

        return "";
    }

    @Nonnull
    @Override
    public String getName() {
        final Element nameElement = document.select("feed > author > name")
                .first();
        if (nameElement == null) {
            return "";
        }

        return nameElement.text();
    }

    @Override
    public InfoItemsPage<StreamInfoItem> getPage(final Page page) {
        return InfoItemsPage.emptyPage();
    }
}
