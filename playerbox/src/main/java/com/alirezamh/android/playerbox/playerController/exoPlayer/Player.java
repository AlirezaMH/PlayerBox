package com.alirezamh.android.playerbox.playerController.exoPlayer;

import android.content.Context;
import android.net.Uri;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.alirezamh.android.playerbox.PlayerStatus;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataRenderer;
import com.google.android.exoplayer2.source.AdaptiveMediaSourceEventListener;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;


/**
 * Author:      Alireza Mahmoodi
 * Created:     4/29/2017
 * Email:       mahmoodi.dev@gmail.com
 * Website:     alirezamh.com
 */

public class Player implements ExoPlayer.EventListener,
        AudioRendererEventListener,
        VideoRendererEventListener,
        AdaptiveMediaSourceEventListener,
        ExtractorMediaSource.EventListener,
        DefaultDrmSessionManager.EventListener,
        MetadataRenderer.Output,
        SurfaceHolder.Callback {
    private static final String TAG = "EXO_PLAYER";
    private final Context context;
    private final Handler mainHandler;
    private SimpleExoPlayer player;
    private DataSource.Factory dataSourceFactory;
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private boolean autoPlay = false;
    private final PlayerStatus playerStatus;
    private Timer bitTimer;
    private long bitPeriod = 1000; //ms
    private static final int MAX_TIMELINE_ITEM_LINES = 3;
    private final MappingTrackSelector trackSelector;
    private final Timeline.Window window;
    private final Timeline.Period period;
    private Handler tickHandler;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean isSurfaceCreated = false;


    public Player(Context context, PlayerStatus ps){
        this.context = context;
        playerStatus = ps;
        window = new Timeline.Window();
        period = new Timeline.Period();
        // 1. Create a default TrackSelector
        mainHandler = new Handler();
//        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // 3. Create the player
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);

        tickHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(player == null) return;
                playerStatus.sendTask(PlayerStatus.STATUS_BUFFER, (int) player.getBufferedPosition());
                playerStatus.sendTask(PlayerStatus.STATUS_PROGRESS, (int) player.getCurrentPosition());
            }
        };
    }



    public void prepare(String url){
        prepare(Uri.parse(url));
    }

    public void prepare(Uri uri){
        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        // Produces DataSource instances through which media data is loaded.
        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "yourApplicationName"), bandwidthMeter);

        // This is the MediaSource representing the media to be played.
//        MediaSource videoSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory, null, null);
        MediaSource videoSource = buildMediaSource(uri);


        player.addListener(this);
        player.setAudioDebugListener(this);
        player.setVideoDebugListener(this);
        player.setMetadataOutput(this);

        // Prepare the player with the source.
        player.prepare(videoSource);
        clearSurface(surfaceView);
        if(autoPlay) player.setPlayWhenReady(true);

    }

    private MediaSource buildMediaSource(Uri uri) {
        return buildMediaSource(uri, null);
    }

    private MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        int type = Util.inferContentType(!TextUtils.isEmpty(overrideExtension) ? "." + overrideExtension
                : uri.getLastPathSegment());
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(dataSourceFactory), mainHandler, this);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(dataSourceFactory), mainHandler, this);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, dataSourceFactory, mainHandler, this);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, dataSourceFactory, new DefaultExtractorsFactory(),
                        mainHandler, this);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *     DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        return new DefaultDataSourceFactory(context, Util.getUserAgent(context, "yourApplicationName"), BANDWIDTH_METER);
    }

    public void play(){
        if(player.getCurrentPosition() >= player.getDuration()){
            player.seekToDefaultPosition();
            // Fixed bug with delay on playing
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    player.setPlayWhenReady(true);
                }
            }, 100);

        }else{
            player.setPlayWhenReady(true);

        }

    }

    public void setPlayWhenReady(boolean status){
        player.setPlayWhenReady(status);
    }

    public void pause(){
        player.setPlayWhenReady(false);
    }

    public void stop(){
        pause();
        player.seekToDefaultPosition();
    }

    public void reset(){
        player.stop();
        clearSurface(surfaceView);
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public void seekTo(long ms){
        player.seekTo(ms);
    }


    public int getCurrentPosition() {
        return player.getDuration() == C.TIME_UNSET ? 0
                : (int) player.getCurrentPosition();
    }

    public int getDuration() {
        return player.getDuration() == C.TIME_UNSET ? 0
                : (int) player.getDuration();
    }

    public boolean isPlaying() {
        return player.getPlayWhenReady();
    }

    public void seekTo(int timeMillis) {
        long seekPosition = player.getDuration() == C.TIME_UNSET ? 0
                : Math.min(Math.max(0, timeMillis), getDuration());
        player.seekTo(seekPosition);
    }

    private void setTimer(boolean status){

        if(!status && bitTimer != null){
            // Destroy Timer
            bitTimer.cancel();
            bitTimer.purge();
            bitTimer = null;

        }else if(bitTimer == null){
            bitTimer = new Timer();
            bitTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    tickHandler.obtainMessage().sendToTarget();
                }
            }, 0, bitPeriod);

        }

    }

    public void destroy(){
        setTimer(false);

        // Destroy Player
        player.release();
        player = null;
        isSurfaceCreated = false;

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        if (timeline == null) {
            return;
        }

        int periodCount = timeline.getPeriodCount();
        int windowCount = timeline.getWindowCount();
        for (int i = 0; i < Math.min(periodCount, MAX_TIMELINE_ITEM_LINES); i++) {
            timeline.getPeriod(i, period);
        }

        for (int i = 0; i < Math.min(windowCount, MAX_TIMELINE_ITEM_LINES); i++) {
            timeline.getWindow(i, window);
        }

        long duration = period.getDurationMs() > 0 ? period.getDurationMs() : window.getDurationMs();
        playerStatus.sendTask(PlayerStatus.STATUS_PREPARED, (int) duration );
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if(isLoading){
            playerStatus.sendTask(PlayerStatus.STATUS_BUFFERING);
        }else {
            playerStatus.sendTask(PlayerStatus.STATUS_BUFFERED);
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int state) {
        if(playWhenReady){
            playerStatus.sendTask(PlayerStatus.STATUS_PLAYING);
            setTimer(true);
        }else {
            playerStatus.sendTask(player.getCurrentPosition() > 0 ? PlayerStatus.STATUS_PAUSED : PlayerStatus.STATUS_STOPPED);
        }

        switch (state) {
            case ExoPlayer.STATE_BUFFERING:
                playerStatus.sendTask(PlayerStatus.STATUS_BUFFERING);
                setTimer(true);
                break;
            case ExoPlayer.STATE_ENDED:
                playerStatus.sendTask(PlayerStatus.STATUS_ENDED);
                player.setPlayWhenReady(false);
//                player.seekToDefaultPosition();
                setTimer(false);
                break;
            case ExoPlayer.STATE_IDLE:
//        playerStatus.sendTask(PlayerStatus.STATUS_PREPARED);
                break;
            case ExoPlayer.STATE_READY:
                playerStatus.sendTask(PlayerStatus.STATUS_BUFFERED);
                setTimer(true);
                break;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        playerStatus.sendTask(PlayerStatus.STATUS_ERROR, PlayerStatus.ERROR_CONNECTION);
        setTimer(false);
        Log.e(TAG, "onPlayerError: ", error);
    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onAudioEnabled(DecoderCounters counters) {

    }

    @Override
    public void onAudioSessionId(int audioSessionId) {

    }

    @Override
    public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

    }

    @Override
    public void onAudioInputFormatChanged(Format format) {

    }

    @Override
    public void onAudioTrackUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

    }

    @Override
    public void onAudioDisabled(DecoderCounters counters) {

    }

    @Override
    public void onDrmKeysLoaded() {

    }

    @Override
    public void onDrmSessionManagerError(Exception e) {
        Log.e(TAG, "onDrmSessionManagerError: ", e);
    }

    @Override
    public void onDrmKeysRestored() {

    }

    @Override
    public void onDrmKeysRemoved() {

    }

    @Override
    public void onMetadata(Metadata metadata) {

    }

    @Override
    public void onLoadStarted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs) {

    }

    @Override
    public void onLoadCompleted(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {
        playerStatus.sendTask(PlayerStatus.STATUS_BUFFERED);
    }

    @Override
    public void onLoadCanceled(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded) {

    }

    @Override
    public void onLoadError(DataSpec dataSpec, int dataType, int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs, long bytesLoaded, IOException error, boolean wasCanceled) {
        playerStatus.sendTask(PlayerStatus.STATUS_ERROR, PlayerStatus.ERROR_FILE_NOT_FOUND);
        setTimer(false);
        Log.e(TAG, "onLoadError: ", error);
    }

    @Override
    public void onUpstreamDiscarded(int trackType, long mediaStartTimeMs, long mediaEndTimeMs) {

    }

    @Override
    public void onDownstreamFormatChanged(int trackType, Format trackFormat, int trackSelectionReason, Object trackSelectionData, long mediaTimeMs) {

    }

    @Override
    public void onLoadError(IOException error) {
        playerStatus.sendTask(PlayerStatus.STATUS_ERROR, PlayerStatus.ERROR_FILE_NOT_FOUND);
        setTimer(false);
        Log.e(TAG, "onLoadError: ", error);
    }

    @Override
    public void onVideoEnabled(DecoderCounters counters) {

    }

    @Override
    public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

    }

    @Override
    public void onVideoInputFormatChanged(Format format) {

    }

    @Override
    public void onDroppedFrames(int count, long elapsedMs) {

    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        if(playerStatus.getOnVideoSizeChangedListener() != null) playerStatus.getOnVideoSizeChangedListener().onVideoSizeChanged(width, height);
    }

    @Override
    public void onRenderedFirstFrame(Surface surface) {

    }

    @Override
    public void onVideoDisabled(DecoderCounters counters) {

    }

    public void setVideoSurfaceView(SurfaceView surfaceView){
        this.surfaceView = surfaceView;
        surfaceView.getHolder().addCallback(this);
        player.setVideoSurfaceView(surfaceView);
    }

    public void setVideoSurfaceHolder(SurfaceHolder surfaceHolder){
        this.surfaceHolder = surfaceHolder;
        this.surfaceHolder.addCallback(this);
        player.setVideoSurfaceHolder(surfaceHolder);
    }

    public void setVideoTextureView(TextureView textureView){
        player.setVideoTextureView(textureView);
    }

    public void removeVideoSurfaceView() {
        if(surfaceView != null){
            clearSurface(surfaceView);
            player.clearVideoSurface();
            surfaceView.getHolder().removeCallback(this);
            surfaceView = null;
            isSurfaceCreated = false; //?
        }

        if(surfaceHolder != null){
            surfaceHolder.removeCallback(this);
            surfaceHolder = null;
            isSurfaceCreated = false;
        }

    }

    private void clearSurface(SurfaceView surfaceView){
        if(surfaceView == null || !isSurfaceCreated) return;

        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        egl.eglInitialize(display, null);

        int[] attribList = {
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL10.EGL_NONE, 0,      // placeholder for recordable [@-3]
                EGL10.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfigs = new int[1];
        egl.eglChooseConfig(display, attribList, configs, configs.length, numConfigs);
        EGLConfig config = configs[0];
        EGLContext context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, new int[]{
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        });
        EGLSurface eglSurface = egl.eglCreateWindowSurface(display, config, surfaceView,
                new int[]{
                        EGL14.EGL_NONE
                });

        egl.eglMakeCurrent(display, eglSurface, eglSurface, context);
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        egl.eglSwapBuffers(display, eglSurface);
        egl.eglDestroySurface(display, eglSurface);
        egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_CONTEXT);
        egl.eglDestroyContext(display, context);
        egl.eglTerminate(display);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceCreated = false;
    }

    public void setVideoScalingMode(int videoScalingMode){
        player.setVideoScalingMode(videoScalingMode);
    }

    public SimpleExoPlayer getPlayer(){
        return player;
    }
}
