package com.alirezamh.android.playerbox.interfaces;

/**
 * Author:      Alireza Mahmoudi
 * Last Update: 8/8/2016
 * Email:       mahmoodi.dev@gmail.com
 * Website:     alirezamh.com
 */

public interface OnPlayerStateListener {

    void onStatusChanged(int playerStatus);
    void onPrepared(int mediaDuration);
    void onBuffer(int ms);
    void onProgress(int ms);
    void onError(int errorCode);

}
