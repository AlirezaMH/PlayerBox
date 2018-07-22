package com.alirezamh.android.playerbox.playerController.exoPlayer;

import android.content.Context;
import android.net.Uri;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.alirezamh.android.playerbox.PlayerStatus;
import com.alirezamh.android.playerbox.interfaces.OnPlayerStateListener;
import com.alirezamh.android.playerbox.models.MediaModel;
import com.alirezamh.android.playerbox.playerController.PlayerController;
import com.alirezamh.android.playerbox.models.PlaylistModel;
import com.google.android.exoplayer2.SimpleExoPlayer;

/**
 * Author:      Alireza Mahmoodi
 * Created:     4/24/2017
 * Email:       mahmoodi.dev@gmail.com
 * Website:     alirezamh.com
 */

public class ExoPlayerController implements PlayerController {

    private final Player player;
    private final PlayerStatus playerStatus;
    private MediaModel media;

    public ExoPlayerController(Context context) {
        playerStatus = new PlayerStatus();
        player = new Player(context, playerStatus);
    }

    @Override
    public boolean canPause() {
        return !getMedia().isStream();
    }

    @Override
    public void release() {
        player.reset();
    }

    @Override
    public void destroy() {
        player.destroy();
    }

    @Override
    public void setOnPlayerStateListener(OnPlayerStateListener onPlayerStateListener) {
        playerStatus.setListener(onPlayerStateListener);
    }

    @Override
    public MediaModel getMedia() {
        return media;
    }

    @Override
    public void setVideoSurfaceView(SurfaceView videoCanvas) {
        player.setVideoSurfaceView(videoCanvas);
    }

    @Override
    public void setVideoSurfaceHolder(SurfaceHolder surfaceHolder) {
        player.setVideoSurfaceHolder(surfaceHolder);
    }

    @Override
    public void removeVideoSurfaceView() {
        player.removeVideoSurfaceView();
    }

    @Override
    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener) {
        playerStatus.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
    }

    @Override
    public void setVideoScalingMode(int videoScalingMode) {
        player.setVideoScalingMode(videoScalingMode);
    }

    @Override
    public SimpleExoPlayer getPlayer() {
        return player.getPlayer();
    }


//    @Override
//    public int getBufferPercentage() {
//        return player.getBufferedPercentage();
//    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public PlayerController init() {
        return null;
    }

    @Override
    public void prepare(MediaModel media) {
        this.media = media;
        playerStatus.setMedia(media);
        if(media.isFile()){
            player.prepare(Uri.fromFile(media.file));

        }else{
            player.prepare(media.url);

        }
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
        player.setPlayWhenReady(status);
    }

    @Override
    public void seekTo(int timeMillis) {
        player.seekTo(timeMillis);
    }

}