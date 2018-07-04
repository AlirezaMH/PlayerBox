package com.alirezamh.android.playerbox;

import android.support.annotation.IntDef;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alirezamh.android.playerbox.interfaces.OnPlayerStateListener;
import com.alirezamh.android.playerbox.playerController.PlayerController;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Author:      Alireza Mahmoudi
 * Last Update: 8/11/2016
 * Email:       mahmoodi.dev@gmail.com
 * Website:     alirezamh.com
 */
public class PlayerViewManager implements OnPlayerStateListener {
    public static final int PLAYBACK_PLAY_STOP = 0;
    public static final int PLAYBACK_PLAY_PAUSE = 1;
    protected PlayerBox playerBox = null;
    protected boolean isSeeking = false;
    protected OnPlayerStateListener userStateListener = null;
    protected int mediaDuration = -1;

    protected List<View> playViews = new ArrayList<>();
    protected List<View> pauseViews = new ArrayList<>();
    protected List<View> playbackViews = new ArrayList<>();
    protected List<View> stopViews = new ArrayList<>();
    protected List<View> nextViews = new ArrayList<>();
    protected List<View> previousViews = new ArrayList<>();
    protected List<SeekBar> seekBarViews = new ArrayList<>();
    protected List<ProgressBar> progressBarViews = new ArrayList<>();
    protected List<TextView> runningTimeViews = new ArrayList<>();
    protected List<TextView> totalTimeViews = new ArrayList<>();
    protected List<TextView> remainingTimeViews = new ArrayList<>();

    @IntDef({PLAYBACK_PLAY_STOP, PLAYBACK_PLAY_PAUSE})
    @Retention(RetentionPolicy.SOURCE)
    protected  @interface PlaybackType {}

    public PlayerViewManager(final PlayerBox playerBox){
        this.playerBox = playerBox;
        playerBox.setOnPlayerStateListener(this);
    }

    public void onStatusChanged(int playerStatus) {
//        Log.i("PlayerBox", "state: " + playerStatus);
        if(userStateListener != null) userStateListener.onStatusChanged(playerStatus);
    }

    @Override
    public void onPrepared(int duration) {
//        Log.i("PlayerBox", "duration: " + duration);

        mediaDuration = duration;
        setMaxProgress(mediaDuration);
        if(totalTimeViews != null) setTotalTime(duration);
        if(remainingTimeViews != null) setRemainingTime(duration); // minus from this position
        if(userStateListener != null) userStateListener.onPrepared(mediaDuration);
    }

    @Override
    public void onBuffer(int ms) {
//        Log.i("PlayerBox", "buffer: " + ms);
        setSecondaryProgress(ms);
        if(userStateListener != null) userStateListener.onBuffer(ms);
    }

    @Override
    public void onProgress(int ms) {
//        Log.i("PlayerBox", "progress: " + ms);

        if(!isSeeking){
            setProgress(ms);
            setPlayerTime(ms);
        }
        if(userStateListener != null) userStateListener.onProgress(ms);
    }

    @Override
    public void onError(int errorCode) {
//        Log.i("PlayerBox", "error: " + errorCode);

        if(userStateListener != null) userStateListener.onError(errorCode);
    }

    public PlayerViewManager clear(){
        playViews.clear();
        pauseViews.clear();
        playbackViews.clear();
        stopViews.clear();
        nextViews.clear();
        previousViews.clear();
        seekBarViews.clear();
        progressBarViews.clear();
        runningTimeViews.clear();
        totalTimeViews.clear();
        remainingTimeViews.clear();
        playerBox.removeVideoSurfaceView();

        return this;
    }

    public PlayerViewManager setOnPlayerStateListener(OnPlayerStateListener onPlayerStateListener){
        userStateListener = onPlayerStateListener;
        return this;
    }

    public PlayerViewManager addPlaybackView(View view){
        return addPlaybackView(view, PLAYBACK_PLAY_STOP);
    }

    public PlayerViewManager addPlaybackView(View view, @PlaybackType final int type){
        playbackViews.add(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playerBox.isPlaying()){
                    if(type == PLAYBACK_PLAY_STOP) playerBox.stop();
                    else if(type == PLAYBACK_PLAY_PAUSE) playerBox.pause();
                }
                else playerBox.play();
            }
        });
        return this;
    }

    public PlayerViewManager addPlayView(View view){
        playViews.add(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerBox.play();
            }
        });
        return this;
    }

    public PlayerViewManager addPauseView(View view){
        pauseViews.add(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerBox.pause();

            }
        });
        return this;
    }
    public PlayerViewManager addStopView(View view){
        stopViews.add(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerBox.stop();
                setPlayerTime(0);
                setProgress(0);
            }
        });
        return this;
    }
    public PlayerViewManager addNextView(View view){
        nextViews.add(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playerBox.next()){
                    setPlayerTime(0);
                    resetSeekBar();
                }else{
                    reset();
                }
            }
        });
        return this;
    }
    public PlayerViewManager addPreviousView(View view){
        previousViews.add(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playerBox.previous()){
                    setPlayerTime(0);
                    resetSeekBar();
                }else{
                    reset();
                }
            }
        });
        return this;
    }

    public PlayerViewManager addProgressBar(ProgressBar progressBar){
        progressBarViews.add(progressBar);
        if(playerBox.isPlaying()){
            progressBar.setMax(playerBox.getPlayerController().getDuration());
        }
        return this;
    }

    public PlayerViewManager addSeekBar(SeekBar seekBar){
        seekBarViews.add(seekBar);
        if(playerBox.isPlaying()){
            seekBar.setMax(playerBox.getPlayerController().getDuration());
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            boolean isSeeking = false;
            int time = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isSeeking) {
                    time = progress;
                    setPlayerTime(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeeking = false;
                playerBox.seekTo(time);
            }
        });
        return this;
    }

    public PlayerViewManager addRunningTimeView(TextView runningTimeView){
        runningTimeViews.add(runningTimeView);
        runningTimeView.setText(getDurationFormat(0));
        return this;
    }
    public PlayerViewManager addTotalTimeView(TextView totalTimeView){
        totalTimeViews.add(totalTimeView);
        totalTimeView.setText(getDurationFormat(0));
        return this;
    }
    public PlayerViewManager addRemainingTimeView(TextView remainingTimeView){
        remainingTimeViews.add(remainingTimeView);
        remainingTimeView.setText(getDurationFormat(0));
        return this;
    }

    protected PlayerViewManager setPlayerTime(int time){
        if(time < 0) time = 0;

        setRunningTime(time);
        setRemainingTime(mediaDuration - time);
        return this;
    }

    protected String getDurationFormat(int duration){
        duration = Math.round(duration/1000);
        int hour = duration / 3600;
        int minutes = (duration % 3600)/60;
        int seconds = (duration % 3600)%60;
        String hours = (hour!=0)? (coupleNumber(hour)+":") : "";
        return hours + coupleNumber(minutes) + ":" + coupleNumber(seconds);
    }

    protected String coupleNumber(int number){
        return (number < 10 ? "0" : "") + number;
    }

    protected void reset(){
        setRunningTime(0);
        setRemainingTime(0);
        setTotalTime(0);
        resetSeekBar();
    }

    protected void resetSeekBar(){
        setProgress(0);
        setSecondaryProgress(0);
    }

    protected void setProgress(int progress){
        for (SeekBar s: seekBarViews){
            s.setProgress(progress);
        }
        for (ProgressBar p: progressBarViews){
            p.setProgress(progress);
        }
    }

    protected void setSecondaryProgress(int progress){
        for (SeekBar s: seekBarViews){
            s.setSecondaryProgress(progress);
        }
        for (ProgressBar p: progressBarViews){
            p.setSecondaryProgress(progress);
        }
    }

    protected void setMaxProgress(int max){
        for (SeekBar s: seekBarViews){
            s.setMax(max);
        }
        for (ProgressBar p: progressBarViews){
            p.setMax(max);
        }
    }

    protected void setRunningTime(int time){
        for (TextView t: runningTimeViews){
            t.setText(getDurationFormat(time));
        }
    }

    protected void setRemainingTime(int time){
        for (TextView t: remainingTimeViews){
            t.setText(getDurationFormat(time));
        }
    }

    protected void setTotalTime(int time){
        for (TextView t: totalTimeViews){
            t.setText(getDurationFormat(time));
        }
    }

    public PlayerViewManager setVideoSurfaceView(SurfaceView surfaceView){
        playerBox.setVideoSurfaceView(surfaceView);
        return this;
    }

    public PlayerViewManager setVideoSurfaceHolder(SurfaceHolder surfaceHolder){
        playerBox.setVideoSurfaceHolder(surfaceHolder);
        return this;
    }

    public PlayerViewManager setOnVideoSizeChangedListener(PlayerController.OnVideoSizeChangedListener onVideoSizeChangedListener){
        playerBox.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        return this;
    }



}
