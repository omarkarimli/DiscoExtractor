package com.omarkarimli.discoextractor.extractor.channel;

import com.omarkarimli.discoextractor.extractor.Image;
import com.omarkarimli.discoextractor.extractor.ListExtractor.InfoItemsPage;
import com.omarkarimli.discoextractor.extractor.ListInfo;
import com.omarkarimli.discoextractor.extractor.NewPipe;
import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;
import com.omarkarimli.discoextractor.extractor.stream.StreamInfoItem;
import com.omarkarimli.discoextractor.extractor.utils.ExtractorHelper;
import com.omarkarimli.discoextractor.extractor.utils.Utils;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ChannelInfo extends ListInfo<StreamInfoItem> {

    public ChannelInfo(final int serviceId,
                       final String id,
                       final String url,
                       final String originalUrl,
                       final String name,
                       final ListLinkHandler listLinkHandler) {
        super(serviceId, id, url, originalUrl, name, listLinkHandler.getContentFilters(),
                listLinkHandler.getSortFilter());
    }

    public static ChannelInfo getInfo(final String url) throws IOException, ExtractionException {
        return getInfo(NewPipe.getServiceByUrl(url), url);
    }

    public static ChannelInfo getInfo(final StreamingService service, final String url)
            throws IOException, ExtractionException {
        final ChannelExtractor extractor = service.getChannelExtractor(url);
        extractor.fetchPage();
        return getInfo(extractor);
    }

    public static InfoItemsPage<StreamInfoItem> getMoreItems(final StreamingService service,
                                                             final String url,
                                                             final Page page)
            throws IOException, ExtractionException {
        return service.getChannelExtractor(url).getPage(page);
    }

    public static ChannelInfo getInfo(final ChannelExtractor extractor)
            throws IOException, ExtractionException {

        final int serviceId = extractor.getServiceId();
        final String id = extractor.getId();
        final String url = extractor.getUrl();
        final String originalUrl = extractor.getOriginalUrl();
        final String name = extractor.getName();

        final ChannelInfo info =
                new ChannelInfo(serviceId, id, url, originalUrl, name, extractor.getLinkHandler());

        try {
            info.setAvatarUrl(extractor.getAvatarUrl());
        } catch (final Exception e) {
            info.addError(e);
        }
        try {
            info.setBannerUrl(extractor.getBannerUrl());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setAvatars(extractor.getAvatars());
        } catch (final Exception e) {
            info.addError(e);
        }
        if (info.getAvatarUrl() == null && !info.getAvatars().isEmpty()) {
            info.setAvatarUrl(info.getAvatars().get(info.getAvatars().size() - 1).getUrl());
        }

        try {
            info.setBanners(extractor.getBanners());
        } catch (final Exception e) {
            info.addError(e);
        }
        if (info.getBannerUrl() == null && !info.getBanners().isEmpty()) {
            info.setBannerUrl(info.getBanners().get(info.getBanners().size() - 1).getUrl());
        }

        try {
            info.setFeedUrl(extractor.getFeedUrl());
        } catch (final Exception e) {
            info.addError(e);
        }

        final InfoItemsPage<StreamInfoItem> itemsPage =
                ExtractorHelper.getItemsPageOrLogError(info, extractor);
        info.setRelatedItems(itemsPage.getItems());
        info.setNextPage(itemsPage.getNextPage());

        try {
            info.setSubscriberCount(extractor.getSubscriberCount());
        } catch (final Exception e) {
            info.addError(e);
        }
        try {
            info.setDescription(Utils.replaceMany(extractor.getDescription()));
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setParentChannelName(extractor.getParentChannelName());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setParentChannelUrl(extractor.getParentChannelUrl());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setParentChannelAvatarUrl(extractor.getParentChannelAvatarUrl());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setVerified(extractor.isVerified());
        } catch (final Exception e) {
            info.addError(e);
        }

        try {
            info.setTabs(extractor.getTabs());
        } catch (final Exception e) {
            info.addError(e);
        }
        try {
            info.setTags(extractor.getTags());
        } catch (final Exception e) {
            info.addError(e);
        }

        return info;
    }


    @Nonnull
    private List<Image> avatars = Collections.emptyList();
    @Nonnull
    private List<Image> banners = Collections.emptyList();

    private String avatarUrl;
    private String parentChannelName;
    private String parentChannelUrl;
    private String parentChannelAvatarUrl;
    private String bannerUrl;
    private String feedUrl;
    private long subscriberCount = -1;
    private String description;
    private String[] donationLinks;
    private boolean verified;

    private List<ListLinkHandler> tabs = Collections.emptyList();

    private List<String> tags = Collections.emptyList();

    public String getParentChannelName() {
        return parentChannelName;
    }

    public void setParentChannelName(final String parentChannelName) {
        this.parentChannelName = parentChannelName;
    }

    public String getParentChannelUrl() {
        return parentChannelUrl;
    }

    public void setParentChannelUrl(final String parentChannelUrl) {
        this.parentChannelUrl = parentChannelUrl;
    }

    public String getParentChannelAvatarUrl() {
        return parentChannelAvatarUrl;
    }

    public void setParentChannelAvatarUrl(final String parentChannelAvatarUrl) {
        this.parentChannelAvatarUrl = parentChannelAvatarUrl;
    }

    @Nonnull
    public List<Image> getAvatars() {
        return avatars;
    }

    public void setAvatars(@Nonnull final List<Image> avatars) {
        this.avatars = avatars;
    }

    @Nonnull
    public List<Image> getBanners() {
        return banners;
    }

    public void setBanners(@Nonnull final List<Image> banners) {
        this.banners = banners;
    }


    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(final String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(final String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(final String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public long getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(final long subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String[] getDonationLinks() {
        return donationLinks;
    }

    public void setDonationLinks(final String[] donationLinks) {
        this.donationLinks = donationLinks;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(final boolean verified) {
        this.verified = verified;
    }

    @Nonnull
    public List<ListLinkHandler> getTabs() {
        return tabs;
    }

    public void setTabs(@Nonnull final List<ListLinkHandler> tabs) {
        this.tabs = tabs;
    }

    @Nonnull
    public List<String> getTags() {
        return tags;
    }

    public void setTags(@Nonnull final List<String> tags) {
        this.tags = tags;
    }
}
