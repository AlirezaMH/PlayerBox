package com.alirezamh.android.playerbox;

import com.alirezamh.android.playerbox.interfaces.OnPlayerStateListener;
import com.alirezamh.android.playerbox.models.MediaModel;
import com.alirezamh.android.playerbox.playerController.PlayerController;

/**
 * Author:      Alireza Mahmoudi
 * Last Update: 8/8/2016
 * Email:       mahmoodi.dev@gmail.com
 * Website:     alirezamh.com
 */

public class PlayerStatus {
    public static final int STATUS_ERROR = 0;
    public static final int STATUS_PLAYING = 1;
    public static final int STATUS_STOPPED = 2;
    public static final int STATUS_PAUSED = 3;
    public static final int STATUS_BUFFERING = 4;
    public static final int STATUS_PROGRESS = 5;
    public static final int STATUS_BUFFER = 6;
    public static final int STATUS_PREPARED = 7;
    public static final int STATUS_BUFFERED = 8;
    public static final int STATUS_ENDED = 9;

    public static final int ERROR_UNKNOWN       = 0;
    public static final int ERROR_CONNECTION    = 1;
    public static final int ERROR_FILE_NOT_FOUND = 2;
    private OnPlayerStateListener onPlayerStateListener;
    private PlayerController.OnVideoSizeChangedListener onVideoSizeChangedListener;
    private MediaModel media;

    public PlayerStatus(){
    }

    public void setMedia(MediaModel media){
        this.media = media;
    }
    public void setListener(OnPlayerStateListener onPlayerStateListener){
        this.onPlayerStateListener = onPlayerStateListener;

    }

    public void setOnVideoSizeChangedListener(PlayerController.OnVideoSizeChangedListener onVideoSizeChangedListener) {
        this.onVideoSizeChangedListener = onVideoSizeChangedListener;
    }

    public PlayerController.OnVideoSizeChangedListener getOnVideoSizeChangedListener() {
        return onVideoSizeChangedListener;
    }

    public void sendTask(int... params) {

        if (onPlayerStateListener != null) {
            switch (params[0]) {

                case PlayerStatus.STATUS_PROGRESS:
                    onPlayerStateListener.onProgress(params[1]);
                    break;

                case PlayerStatus.STATUS_BUFFER:
                    onPlayerStateListener.onBuffer(params[1]);
                    break;

                case PlayerStatus.STATUS_PLAYING:
                    onPlayerStateListener.onStatusChanged(PlayerStatus.STATUS_BUFFERED); //media is buffered when that is playing.
                case PlayerStatus.STATUS_BUFFERING:
                case PlayerStatus.STATUS_BUFFERED:
                case PlayerStatus.STATUS_STOPPED:
                case PlayerStatus.STATUS_PAUSED:
                    onPlayerStateListener.onStatusChanged(params[0]);
                    break;

                case PlayerStatus.STATUS_ENDED:
                    onPlayerStateListener.onStatusChanged(PlayerStatus.STATUS_STOPPED);
                    onPlayerStateListener.onStatusChanged(params[0]);
                    break;

                case PlayerStatus.STATUS_PREPARED:
                    onPlayerStateListener.onPrepared(params[1]);
                    break;

                case PlayerStatus.STATUS_ERROR:
                    onPlayerStateListener.onError(params[1]);
            }
        }


        switch (params[0]) {
            case PlayerStatus.STATUS_PLAYING:
                media.setStatus(MediaModel.STATUS.PLAYING);
                break;

            case PlayerStatus.STATUS_STOPPED:
            case PlayerStatus.STATUS_PAUSED:
//                if (isPrepare()) media.setStatus(MediaModel.STATUS.READY);
                break;

            case PlayerStatus.STATUS_ERROR:
                media.setStatus(MediaModel.STATUS.NOT_READY);
                break;

            case PlayerStatus.STATUS_BUFFERING:
                media.setStatus(MediaModel.STATUS.BUFFERING);
                break;

            case PlayerStatus.STATUS_PREPARED:
                media.setStatus(MediaModel.STATUS.READY);
                break;

        }

    }

}
