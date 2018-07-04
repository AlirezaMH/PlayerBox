package com.alirezamh.android.playerbox.playerController.mediaPlayer;

import android.media.MediaPlayer;
import android.os.Build;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.alirezamh.android.playerbox.PlayerStatus;
import com.alirezamh.android.playerbox.interfaces.OnPlayerStateListener;
import com.alirezamh.android.playerbox.models.MediaModel;
import com.alirezamh.android.playerbox.models.PlaylistModel;
import com.alirezamh.android.playerbox.playerController.PlayerController;

/**
 * Author:      Alireza Mahmoodi
 * Created:     4/24/2017
 * Email:       mahmoodi.dev@gmail.com
 * Website:     alirezamh.com
 */

public class MediaPlayerController implements PlayerController {

    private Player player;
    private OnPlayerStateListener onPlayerStateListener;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private PlayerStatus playerStatus;
    private int videoScalingMode = MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT;

//    public MediaPlayerController(Player player){
//        this.player = player;
//    }

    public MediaPlayerController(){
        playerStatus = new PlayerStatus();

    }


    @Override
    public MediaPlayerController init() {
//        if(player == null) player = new Player();
        return this;
    }

    @Override
    public void prepare(MediaModel media) {
        if(player == null){
            player = new Player(media, playerStatus);

        }else if(!media.equals(player.getMedia())){
            player.destroy();
            player = new Player(media, playerStatus);
        }
        else{
            return;
        }
        if(surfaceView != null) player.setSurfaceView(surfaceView);
        else if(surfaceHolder != null) player.setSurfaceHolder(surfaceHolder);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            player.setVideoScalingMode(videoScalingMode);
        }
        player.prepare();
    }

    @Override
    public void setPlayList(PlaylistModel playList) {

    }

    @Override
    public void play() {
        player.play();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void stop() {
        player.stop();
    }

    @Override
    public void playAfterBuffer(boolean status) {
        player.playAfterBuffer(status);
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        player.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        if(player == null) return false;
        return player.isPlaying();
    }

    @Override
    public boolean canPause() {
        return !getMedia().isStream();
    }

    @Override
    public void release() {
        destroy();
    }

    @Override
    public void destroy() {
        if(player != null) player.destroy();
        player = null;
    }

    @Override
    public void setOnPlayerStateListener(OnPlayerStateListener onPlayerStateListener) {
        playerStatus.setListener(onPlayerStateListener);
    }

    @Override
    public MediaModel getMedia() {
        return player.getMedia();
    }

    @Override
    public void setVideoSurfaceView(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    @Override
    public void setVideoSurfaceHolder(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    @Override
    public void removeVideoSurfaceView() {
        if(player != null) player.removeSurfaceView();
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener) {
        playerStatus.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
    }

    @Override
    public void setVideoScalingMode(int videoScalingMode) {
        this.videoScalingMode = videoScalingMode;
        if(player != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            player.setVideoScalingMode(videoScalingMode);
        }
    }

    @Override
    public MediaPlayer getPlayer() {
        return player.getPlayer();
    }

}
