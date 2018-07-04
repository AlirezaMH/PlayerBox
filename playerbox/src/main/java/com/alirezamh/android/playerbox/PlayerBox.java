package com.alirezamh.android.playerbox;

import android.content.Context;
import android.support.annotation.IntDef;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.alirezamh.android.playerbox.interfaces.OnPlayerStateListener;
import com.alirezamh.android.playerbox.models.PlaylistModel;
import com.alirezamh.android.playerbox.playerController.PlayerController;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;


/**
 * Author:      Alireza Mahmoudi
 * Last Update: 8/10/2016
 * Email:       mahmoodi.dev@gmail.com
 * Website:     alirezamh.com
 */
public class PlayerBox<PC extends PlayerController> {
    private final static String TAG = "PLAYER_BOX";
    public final static int REPEAT_MODE_OFF = 0;
    public final static int REPEAT_MODE_ONE = 1;
    public final static int REPEAT_MODE_ALL = 2;
    public final static int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    public final static int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;
    private PC player = null;
    private PlaylistModel playlist = null;
    private boolean looping = false;
    private OnPlayerStateListener userStateListener;
    private OnPlayerStateListener stateListener;
    private PlayerViewManager playerViewManager;
    private int durationSlop = 3000;
    private final PlayerBoxPhoneStateListener phoneStateListener;
    private final TelephonyManager telephonyManager;
    private List<OnMediaChangedListener> onMediaChangedListeners = new ArrayList<>();
    private int repeatMode = REPEAT_MODE_ALL;

    @IntDef({REPEAT_MODE_OFF, REPEAT_MODE_ONE, REPEAT_MODE_ALL})
    @Retention(RetentionPolicy.SOURCE)
    protected  @interface RepeatMode {}

    @IntDef({VIDEO_SCALING_MODE_SCALE_TO_FIT, VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING})
    @Retention(RetentionPolicy.SOURCE)
    protected  @interface VideoScalingMode {}

    /**
     *
     * @param context
     * @param player An instance of {MediaPlayerController} or {ExoPlayerController}
     */
    public PlayerBox(Context context, PC player){
        this.player = player;

        //Phone State Listener
        phoneStateListener = new PlayerBoxPhoneStateListener(this);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     *
     * @param context
     * @param player    An instance of {MediaPlayerController} or {ExoPlayerController}
     * @param playlist  Playlist object
     */
    public PlayerBox(Context context, PC player, PlaylistModel playlist){
        this(context, player);
        setPlayList(playlist);
    }

    public void setPlayList(PlaylistModel playlist){
        this.playlist = playlist;
        player.release();
        player.setOnPlayerStateListener(getPlayerStateListener());
        player.prepare(playlist.getMedia());
        onMediaChanged(playlist);
    }

    public PlaylistModel getPlaylist() {
        return playlist;
    }

    /**
     * Show playing status
     * @return
     */
    public boolean isPlaying() {
        return player.isPlaying();
    }

    public boolean isReady(){
        if(playlist == null || playlist.size() == 0) return false;
        return true;
    }

    public void play(int location){
        if (goTo(location)){
            play();
        }
    }
    public void play(){
        if (!isReady()) return;
        if(playlist.getMedia().equals(player.getMedia())){
            if(player.isPlaying()) return;
            player.play();
            return;
        }
        restart();
        player.play();
    }

    public void pause(){
        if (!isReady()) return;
        player.pause();
    }
    public void stop(){
        if (!isReady()) return;
        player.stop();
    }
    public boolean goTo(int location){
        if (!isReady()) return false;
        if(playlist.goTo(location)){
            onMediaChanged(playlist);
            return true;
        }else {
            return false;
        }
    }
    public boolean next(){
        if (!isReady()) return false;
        boolean isPlaying = player.isPlaying();
        if(playlist.next()){
            onMediaChanged(playlist);
        }else if(isLooping()){
            playlist.first();
            onMediaChanged(playlist);
        }else{
            if(isPlaying) stop();
            return false;
        }

        restart();
        if(isPlaying){
            if(playlist.getMedia().isStream()){
                player.playAfterBuffer(true);
            }else{
                player.play();
            }
        }
        return true;
    }

    public boolean previous(){
        if (!isReady()) return false;
        boolean isPlaying = player.isPlaying();
        if(playlist.previous()){
            onMediaChanged(playlist);
        }else if(isLooping()){
            playlist.last();
            onMediaChanged(playlist);
        }else{
           if(isPlaying) stop(); // or play again first track
            return false;

        }
        restart();
        if(isPlaying){
            if(playlist.getMedia().isStream()){
                player.playAfterBuffer(true);
            }else{
                player.play();
            }
        }

        return true;
    }

    public void destroy(){
        if(player != null){
            player.destroy();
            player = null;
            if(playlist != null) playlist.resetFlags();
        }
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    public void seekTo(int sec){
        if (!isReady()) return;
        player.seekTo(sec);
    }

    public boolean isLooping(){
        return looping;
    }

    public void setLooping(boolean looping){
        this.looping = looping;
    }

    private void restart() {
        player.prepare(playlist.getMedia());
    }

    public void release(){
        if (!isReady()) return;
        player.release();
        playlist.clear();
    }

    public void setOnPlayerStateListener(OnPlayerStateListener stateListener){
        this.userStateListener = stateListener;
    }

    public void setOnVideoSizeChangedListener(PC.OnVideoSizeChangedListener onVideoSizeChangedListener){
        player.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
    }

    public void addOnMediaChangedListener(OnMediaChangedListener onMediaChangedListener){
        if(onMediaChangedListener != null) onMediaChangedListeners.add(onMediaChangedListener);
    }

    @Deprecated
    public PC getPlayer(){
        return getPlayerController();
    }

    public PC getPlayerController(){
        return player;
    }

    public void setViewManager(PlayerViewManager playerViewManager){
        this.playerViewManager = playerViewManager;
    }
    public PlayerViewManager getViewManager(){
        if(playerViewManager == null) playerViewManager = new PlayerViewManager(this);
        return playerViewManager;
    }

    public void setDurationSlop(int durationSlop) {
        this.durationSlop = durationSlop;
    }

    public int getDurationSlop() {
        return durationSlop;
    }

    private OnPlayerStateListener getPlayerStateListener(){
        if(stateListener == null){
            stateListener = new OnPlayerStateListener() {
                @Override
                public void onStatusChanged(int playerStatus) {
                    switch (playerStatus){
                        case PlayerStatus.STATUS_ENDED:
//                            Log.d(TAG, "onStatusChanged: duration: " + player.getDuration() + " ,current: " + player.getCurrentPosition());
                            if(getRepeatMode() != REPEAT_MODE_OFF && player.getDuration() > 0 && player.getDuration() - durationSlop <= player.getCurrentPosition()){
                                if(getRepeatMode() == REPEAT_MODE_ONE){
                                    play(playlist.getCurrentIndex());
                                }else if(getRepeatMode() == REPEAT_MODE_ALL){
                                    next();
                                }
                            }
                            break;
                    }
                    if(userStateListener != null) userStateListener.onStatusChanged(playerStatus);
                }

                @Override
                public void onPrepared(int mediaDuration) {
                    if(userStateListener != null) userStateListener.onPrepared(mediaDuration);

                }

                @Override
                public void onBuffer(int ms) {
                    if(userStateListener != null && isReady()) userStateListener.onBuffer(ms);

                }

                @Override
                public void onProgress(int ms) {
                    if(userStateListener != null && isReady()) userStateListener.onProgress(ms);

                }

                @Override
                public void onError(int errorCode) {
                    if(userStateListener != null) userStateListener.onError(errorCode);

                }
            };
        }
        return stateListener;
    }

    public void setVideoSurfaceView(SurfaceView surfaceView){
        player.setVideoSurfaceView(surfaceView);
    }

    public void setVideoSurfaceHolder(SurfaceHolder surfaceHolder){
        player.setVideoSurfaceHolder(surfaceHolder);
    }

    public void removeVideoSurfaceView(){
        player.removeVideoSurfaceView();
    }

    public void setVideoScalingMode(@VideoScalingMode int videoScalingMode){
        player.setVideoScalingMode(videoScalingMode);
    }

    private void onMediaChanged(PlaylistModel playlistModel){
        for (OnMediaChangedListener onMediaChangedListener : onMediaChangedListeners){
            onMediaChangedListener.onMediaChanged(playlistModel);
        }
    }

    public void setRepeatMode(@RepeatMode int repeatMode){
        this.repeatMode = repeatMode;
    }

    public @RepeatMode int getRepeatMode() {
        return repeatMode;
    }

    /**
     * On media changed listener
     */
    public interface OnMediaChangedListener {
        void onMediaChanged(PlaylistModel playlist);
    }

}
