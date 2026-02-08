package com.omarkarimli.discoextractor.extractor.services.onecore.extractors;

import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.getJsonPostResponse;
import static com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreParsingHelper.prepareDesktopJsonBuilder;
import static com.omarkarimli.discoextractor.extractor.utils.Utils.UTF_8;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonWriter;

import com.omarkarimli.discoextractor.extractor.Page;
import com.omarkarimli.discoextractor.extractor.StreamingService;
import com.omarkarimli.discoextractor.extractor.bulletComments.BulletCommentsExtractor;
import com.omarkarimli.discoextractor.extractor.bulletComments.BulletCommentsInfoItem;
import com.omarkarimli.discoextractor.extractor.bulletComments.BulletCommentsInfoItemsCollector;
import com.omarkarimli.discoextractor.extractor.downloader.Downloader;
import com.omarkarimli.discoextractor.extractor.exceptions.ExtractionException;
import com.omarkarimli.discoextractor.extractor.exceptions.ParsingException;
import com.omarkarimli.discoextractor.extractor.linkhandler.ListLinkHandler;
import com.omarkarimli.discoextractor.extractor.localization.ContentCountry;
import com.omarkarimli.discoextractor.extractor.localization.Localization;
import com.omarkarimli.discoextractor.extractor.services.onecore.WatchDataCache;
import com.omarkarimli.discoextractor.extractor.services.onecore.OneCoreBulletCommentPair;
import com.omarkarimli.discoextractor.extractor.stream.StreamType;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public class OneCoreBulletCommentsExtractor extends BulletCommentsExtractor {
    private final boolean shoudldBeLive;
    private JsonObject data;
    private String key;
    private StreamType streamType;
    private ScheduledExecutorService executor;
    private final CopyOnWriteArrayList<OneCoreBulletCommentPair> messages = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<OneCoreBulletCommentPair> SuperChatMessages = new CopyOnWriteArrayList<>();
    private String lastContinuation;
    private ScheduledFuture<?> future;
    private boolean disabled = false;
    private long currentPlayPosition = 0;
    private long lastPlayPosition = 0;
    private final boolean isLiveStream;
    private final long startTime;
    private final String[] continuationKeyTexts = new String[]{
            "timedContinuationData", "invalidationContinuationData"
//           , "playerSeekContinuationData" , "liveChatReplayContinuationData"
    };
    private final CopyOnWriteArrayList<String> IDList= new CopyOnWriteArrayList<>();
    private boolean shouldSkipFetch = false;

    public OneCoreBulletCommentsExtractor(StreamingService service, ListLinkHandler uiHandler, WatchDataCache watchDataCache) throws ExtractionException {
        super(service, uiHandler);
        if(watchDataCache.currentUrl.equals(uiHandler.getUrl())){
            isLiveStream = watchDataCache.streamType.equals(StreamType.LIVE_STREAM);
            startTime = watchDataCache.startAt;
            shoudldBeLive = watchDataCache.shouldBeLive;
        } else if (watchDataCache.lastCurrentUrl.equals(uiHandler.getUrl())){
            isLiveStream = watchDataCache.lastStreamType.equals(StreamType.LIVE_STREAM);
            startTime = watchDataCache.lastStartAt;
            shoudldBeLive = watchDataCache.lastShouldBeLive;
        } else {
            throw new ExtractionException("WatchDataCache of current url is not initialized");
        }
    }

    @Override
    public void onFetchPage(@Nonnull Downloader downloader) throws IOException, ExtractionException {
        String response = downloader.get(getUrl()).responseBody();
        if(response.contains("Live chat replay is not available") || response.contains("is disabled") || (!shoudldBeLive && !isLiveStream) ) {
            disabled = true;
            return ;
        }
        try {
            lastContinuation = JsonParser.object().from(response.split(Pattern.quote("var ytInitialData = "))[1]
                            .split(Pattern.quote(";</script>"))[0]).getObject("contents")
                    .getObject("twoColumnWatchNextResults").getObject("conversationBar")
                    .getObject("liveChatRenderer").getArray("continuations").getObject(0)
                    .getObject("reloadContinuationData").getString("continuation");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchMessage(){
        if(shouldSkipFetch){
            shouldSkipFetch = false;
            return ;
        }
        if(lastPlayPosition == currentPlayPosition){
            return ; // should only happen when watching replay and user pauses
            // we do not want to fetch the same message twice
        }
        try {
            final byte[] json = JsonWriter.string(prepareDesktopJsonBuilder(Localization.DEFAULT,
                            ContentCountry.DEFAULT)
                            .value("continuation", lastContinuation)
                            .object("currentPlayerState")
                            .value("playerOffsetMs", String.valueOf(currentPlayPosition))
                            .end()
                            .done())
                    .getBytes(UTF_8);
            JsonObject result;
            try{
                result = getJsonPostResponse("live_chat/" +
                        (isLiveStream? "get_live_chat":
                                "get_live_chat_replay"), json, Localization.DEFAULT);
            } catch (Exception e){
                return;
            }

            JsonObject liveChatContinuation = result.getObject("continuationContents").getObject("liveChatContinuation");
            JsonArray temp1 = liveChatContinuation
                    .getArray("continuations");
            JsonObject lastContinuationParent = temp1.getObject((!isLiveStream && temp1.size() == 2)?1:0);
            if(isLiveStream){
                for(String i: continuationKeyTexts){
                    if(lastContinuationParent.has(i)){
                        lastContinuation = lastContinuationParent.getObject(i).getString("continuation");
                        break;
                    }
                    if(i.equals(continuationKeyTexts[1])){
                        throw new ParsingException("Failed to get continuation data");
                    }
                }
            } else {
                lastContinuation = lastContinuationParent.getObject("playerSeekContinuationData")
                        .getString("continuation");
                if(lastContinuation == null){
                    throw new ParsingException("Failed to get continuation data");
                }
            }

            lastPlayPosition = currentPlayPosition;

            JsonArray actions = liveChatContinuation.getArray("actions");
            for(int i = 0; i < actions.size(); i++){
                JsonObject item = isLiveStream?
                        actions.getObject(i).getObject("addChatItemAction").getObject("item"):
                        actions.getObject(i).getObject("replayChatItemAction")
                                .getArray("actions").getObject(0)
                                .getObject("addChatItemAction").getObject("item");
                if(item.has("liveChatTextMessageRenderer")){
                    JsonObject temp = item.getObject("liveChatTextMessageRenderer");
                    String id = temp.getString("id");
                    if(!IDList.contains(id)){
                        messages.add(new OneCoreBulletCommentPair(temp, isLiveStream?
                                -1 : Long.parseLong(actions.getObject(i).getObject("replayChatItemAction")
                                .getString("videoOffsetTimeMsec"))));
                        IDList.add(id);
                    }
                } else if (item.has("liveChatPaidMessageRenderer")) {
                    JsonObject temp = item.getObject("liveChatPaidMessageRenderer");
                    String id = temp.getString("id");
                    if(!IDList.contains(id)){
                        SuperChatMessages.add(new OneCoreBulletCommentPair(temp, isLiveStream?
                                -1 : Long.parseLong(actions.getObject(i).getObject("replayChatItemAction")
                                .getString("videoOffsetTimeMsec"))));
                        IDList.add(id);
                    }
                }
            }
        } catch (Exception e) {
            // should never throw any exception as that will stop fetching and unable to reconnect
        }
    }

    @Nonnull
    @Override
    public InfoItemsPage<BulletCommentsInfoItem> getInitialPage() throws IOException, ExtractionException {
        if(isDisabled()){
            return null;
        }
        executor = Executors.newSingleThreadScheduledExecutor();
        future = executor.scheduleAtFixedRate(this::fetchMessage, 1000, 1000, TimeUnit.MILLISECONDS);
        return null;
    }

    @Override
    public InfoItemsPage<BulletCommentsInfoItem> getPage(Page page) throws IOException, ExtractionException {
        return null;
    }

    @Override
    public boolean isLive() {
        return true;
    }

    @Override
    public List<BulletCommentsInfoItem> getLiveMessages() throws ParsingException {
        final BulletCommentsInfoItemsCollector collector =
                new BulletCommentsInfoItemsCollector(getServiceId());
        for(OneCoreBulletCommentPair item: messages){
            collector.commit(new OneCoreBulletCommentsInfoItemExtractor(item.getData(), startTime, item.getOffsetDuration()));
        }
        for(OneCoreBulletCommentPair item: SuperChatMessages){
            collector.commit(new OneCoreSuperChatInfoItemExtractor(item.getData(), startTime, item.getOffsetDuration()));
        }
        messages.clear();
        SuperChatMessages.clear();
        return collector.getItems();
    }

    @Override
    public void disconnect() {
        if(future != null && !future.isCancelled()){
            future.cancel(true);
        }
    }

    @Override
    public void reconnect() {
        if(!isDisabled() && future != null && future.isCancelled()){
            future = executor.scheduleAtFixedRate(this::fetchMessage, 1000, 1000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public void setCurrentPlayPosition(long currentPlayPosition) {
        if(!this.isLiveStream && currentPlayPosition == 49) {  // 49 is -1 + 50, invalid and shouldn't set position or it will causing duplicate messages
            return;
        }
        if(this.currentPlayPosition > currentPlayPosition){
            IDList.clear();
            shouldSkipFetch = true;
        }
        this.currentPlayPosition = currentPlayPosition;
    }

    @Override
    public void clearMappingState() {
        IDList.clear();
    }
}
