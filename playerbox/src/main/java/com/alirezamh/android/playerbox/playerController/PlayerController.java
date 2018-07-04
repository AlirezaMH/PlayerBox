package com.alirezamh.android.playerbox.playerController;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.alirezamh.android.playerbox.interfaces.OnPlayerStateListener;
import com.alirezamh.android.playerbox.models.MediaModel;
import com.alirezamh.android.playerbox.models.PlaylistModel;

/**
 * Author:      Alireza Mahmoodi
 * Created:     4/24/2017
 * Email:       mahmoodi.dev@gmail.com
 * Website:     alirezamh.com
 */

public interface PlayerController {
    PlayerController init();

    void prepare(MediaModel media);

    void setPlayList(PlaylistModel playList);

    void play();

    void pause();

    void stop();

    void playAfterBuffer(boolean status);

    int getDuration();

    int getCurrentPosition();

    void seekTo(int pos);

    boolean isPlaying();

//    int getBufferPercentage();

    boolean canPause();

    void release();

    void destroy();

    void setOnPlayerStateListener(OnPlayerStateListener stateListener);

    MediaModel getMedia();

    void setVideoSurfaceView(SurfaceView videoCanvas);

    void setVideoSurfaceHolder(SurfaceHolder surfaceHolder);

    void removeVideoSurfaceView();

    void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener);

    void setVideoScalingMode(int videoScalingMode);

    Object getPlayer();

    interface OnVideoSizeChangedListener{
        void onVideoSizeChanged(int width, int height);
    }
}