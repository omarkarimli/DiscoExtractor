package com.omarkarimli.discoextractor.extractor;

import com.omarkarimli.discoextractor.extractor.services.bandcamp.BandcampService;
import com.omarkarimli.discoextractor.extractor.services.bilibili.BilibiliService;
import com.omarkarimli.discoextractor.extractor.services.media_ccc.MediaCCCService;
import com.omarkarimli.discoextractor.extractor.services.peertube.PeertubeService;
import com.omarkarimli.discoextractor.extractor.services.soundcloud.SoundcloudService;
import com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreService;
import com.omarkarimli.discoextractor.extractor.services.niconico.NiconicoService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"ConstantName", "InnerAssignment"})
public final class ServiceList {
    private ServiceList() {
        //no instance
    }

    public static final OneCoreService OneCore;
    public static final SoundcloudService SoundCloud;
    public static final MediaCCCService MediaCCC;
    public static final PeertubeService PeerTube;
    public static final BandcampService Bandcamp;
    public static final NiconicoService NicoNico;
    public static final BilibiliService BiliBili;
    /**
     * When creating a new service, put this service in the end of this list,
     * and give it the next free id.
     */
    private static final List<StreamingService> SERVICES = Collections.unmodifiableList(
            Arrays.asList(
                    OneCore = new OneCoreService(0),
                    SoundCloud = new SoundcloudService(1),
                    MediaCCC = new MediaCCCService(2),
                    PeerTube = new PeertubeService(3),
                    Bandcamp = new BandcampService(4),
                    BiliBili = new BilibiliService(5),
                    NicoNico = new NiconicoService(6)
            ));

    /**
     * Get all the supported services.
     *
     * @return a unmodifiable list of all the supported services
     */
    public static List<StreamingService> all() {
        return SERVICES;
    }
}
