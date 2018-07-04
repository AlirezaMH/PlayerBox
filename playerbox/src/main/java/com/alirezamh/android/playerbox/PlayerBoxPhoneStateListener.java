package com.alirezamh.android.playerbox;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Author:      Alireza Mahmoodi
 * Created:     6/11/2017
 * Email:       mahmoodi.dev@gmail.com
 * Website:     alirezamh.com
 */

public class PlayerBoxPhoneStateListener extends PhoneStateListener {
    private static boolean pauseOnCalling = false;
    private final PlayerBox playerBox;

    public PlayerBoxPhoneStateListener(PlayerBox playerBox){
        this.playerBox = playerBox;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                if(pauseOnCalling){
                    pauseOnCalling = false;
                    playerBox.play();
                }
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                if(playerBox.isPlaying()){
                    pauseOnCalling = true;
                    if(playerBox.getPlayerController().getMedia().isStream()){
                        playerBox.stop();
                    }else{
                        playerBox.pause();
                    }
                }
                break;

            case TelephonyManager.CALL_STATE_RINGING:
                if(playerBox.isPlaying()){
                    pauseOnCalling = true;
                    if(playerBox.getPlayerController().getMedia().isStream()){
                        playerBox.stop();
                    }else{
                        playerBox.pause();
                    }
                }
                break;
        }
    }
}
