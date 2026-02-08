package com.omarkarimli.discoextractor.extractor.services.onecore;

import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.ANDROID_CLIENT_ID;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.ANDROID_CLIENT_NAME;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.ANDROID_CLIENT_VERSION;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.DESKTOP_CLIENT_PLATFORM;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.EMBED_CLIENT_SCREEN;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.IOS_CLIENT_ID;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.IOS_CLIENT_NAME;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.IOS_CLIENT_VERSION;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.IOS_DEVICE_MODEL;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.IOS_OS_VERSION;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.MOBILE_CLIENT_PLATFORM;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.WATCH_CLIENT_SCREEN;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.WEB_CLIENT_ID;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.WEB_CLIENT_NAME;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.WEB_EMBEDDED_CLIENT_ID;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.WEB_EMBEDDED_CLIENT_NAME;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.WEB_HARDCODED_CLIENT_VERSION;
import static com.omarkarimli.discoextractor.extractor.services.onecore.ClientsConstants.WEB_REMIX_HARDCODED_CLIENT_VERSION;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TODO: add docs

public final class DiscoCoreClientRequestInfo {

    @Nonnull
    public ClientInfo clientInfo;
    @Nonnull
    public DeviceInfo deviceInfo;

    public static final class ClientInfo {

        @Nonnull
        public String clientName;
        @Nonnull
        public String clientVersion;
        @Nonnull
        public String clientScreen;
        @Nullable
        public String clientId;
        @Nullable
        public String visitorData;

        private ClientInfo(@Nonnull final String clientName,
                           @Nonnull final String clientVersion,
                           @Nonnull final String clientScreen,
                           @Nullable final String clientId,
                           @Nullable final String visitorData) {
            this.clientName = clientName;
            this.clientVersion = clientVersion;
            this.clientScreen = clientScreen;
            this.clientId = clientId;
            this.visitorData = visitorData;
        }
    }

    public static final class DeviceInfo {

        @Nonnull
        public String platform;
        @Nullable
        public String deviceMake;
        @Nullable
        public String deviceModel;
        @Nullable
        public String osName;
        @Nullable
        public String osVersion;
        public int androidSdkVersion;

        private DeviceInfo(@Nonnull final String platform,
                           @Nullable final String deviceMake,
                           @Nullable final String deviceModel,
                           @Nullable final String osName,
                           @Nullable final String osVersion,
                           final int androidSdkVersion) {
            this.platform = platform;
            this.deviceMake = deviceMake;
            this.deviceModel = deviceModel;
            this.osName = osName;
            this.osVersion = osVersion;
            this.androidSdkVersion = androidSdkVersion;
        }
    }

    private DiscoCoreClientRequestInfo(@Nonnull final ClientInfo clientInfo,
                                       @Nonnull final DeviceInfo deviceInfo) {
        this.clientInfo = clientInfo;
        this.deviceInfo = deviceInfo;
    }

    @Nonnull
    public static DiscoCoreClientRequestInfo ofWebClient() {
        return new DiscoCoreClientRequestInfo(
                new DiscoCoreClientRequestInfo.ClientInfo(
                        WEB_CLIENT_NAME, WEB_HARDCODED_CLIENT_VERSION, WATCH_CLIENT_SCREEN,
                        WEB_CLIENT_ID, null),
                new DiscoCoreClientRequestInfo.DeviceInfo(DESKTOP_CLIENT_PLATFORM, null, null,
                        null, null, -1));
    }

    @Nonnull
    public static DiscoCoreClientRequestInfo ofWebEmbeddedPlayerClient() {
        return new DiscoCoreClientRequestInfo(
                new DiscoCoreClientRequestInfo.ClientInfo(WEB_EMBEDDED_CLIENT_NAME,
                        WEB_REMIX_HARDCODED_CLIENT_VERSION, EMBED_CLIENT_SCREEN,
                        WEB_EMBEDDED_CLIENT_ID, null),
                new DiscoCoreClientRequestInfo.DeviceInfo(DESKTOP_CLIENT_PLATFORM, null, null,
                        null, null, -1));
    }

    @Nonnull
    public static DiscoCoreClientRequestInfo ofAndroidClient() {
        return new DiscoCoreClientRequestInfo(
                new DiscoCoreClientRequestInfo.ClientInfo(ANDROID_CLIENT_NAME,
                        ANDROID_CLIENT_VERSION, WATCH_CLIENT_SCREEN, ANDROID_CLIENT_ID, null),
                new DiscoCoreClientRequestInfo.DeviceInfo(MOBILE_CLIENT_PLATFORM, null, null,
                        "Android", "15", 35));
    }

    @Nonnull
    public static DiscoCoreClientRequestInfo ofIosClient() {
        return new DiscoCoreClientRequestInfo(
                new DiscoCoreClientRequestInfo.ClientInfo(IOS_CLIENT_NAME, IOS_CLIENT_VERSION,
                        WATCH_CLIENT_SCREEN, IOS_CLIENT_ID, null),
                new DiscoCoreClientRequestInfo.DeviceInfo(MOBILE_CLIENT_PLATFORM, "Apple",
                        IOS_DEVICE_MODEL, "iOS", IOS_OS_VERSION, -1));
    }
}
