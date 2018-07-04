package com.alirezamh.android.playerboxdemo;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alirezamh.android.playerbox.PlayerBox;
import com.alirezamh.android.playerbox.PlayerStatus;
import com.alirezamh.android.playerbox.PlayerViewManager;
import com.alirezamh.android.playerbox.interfaces.OnPlayerStateListener;
import com.alirezamh.android.playerbox.models.MediaType;
import com.alirezamh.android.playerbox.models.PlaylistModel;
import com.alirezamh.android.playerbox.playerController.PlayerController;
import com.alirezamh.android.playerbox.playerController.exoPlayer.ExoPlayerController;
import com.alirezamh.android.playerbox.playerController.mediaPlayer.MediaPlayerController;

import helpers.Utility;

public class MainActivity extends AppCompatActivity {

    private boolean isPlayed = false;
    private static PlayerBox playerBox;
    private boolean playbackStatus;
    public static MainActivity instance;
    private static final String TAG = "MAIN_ACTIVITY";
    private SurfaceView surfaceView;
    private TextView errorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializePlayer();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            findViewById(R.id.panel).setVisibility(View.GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().hide();
            surfaceView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            surfaceView.requestLayout();

        }else{
            findViewById(R.id.panel).setVisibility(View.VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getSupportActionBar().show();
            surfaceView.getLayoutParams().height = (int) Utility.getHeightByRatioInPixel(1.77f);
            surfaceView.requestLayout();
        }
    }

    public static PlayerBox getPlayerBox(){
        return playerBox;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerBox.destroy();
    }

    protected void initializePlayer(){
        Button btnRefreshView = (Button) findViewById(R.id.btn_refresh_view);
        TextView playerName = (TextView) findViewById(R.id.playerName);

        PlayerController playerControl = new ExoPlayerController(this);
        playerBox = new PlayerBox<>(this, playerControl);
        if(playerControl instanceof ExoPlayerController){
            playerName.setText("Controller: ExoPlayer");

        }else if(playerControl instanceof MediaPlayerController){
            playerName.setText("Controller: MediaPlayer");

        }

        initPlayer();
        btnRefreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initPlayer();
            }
        });

//        Log.v("PlayerDemo", "File path: " + getAssets().open("chavoshi.mp3"));


        PlaylistModel playlist= new PlaylistModel();
        playlist.add(MediaType.STREAM, "http://cdn1.live.irib.ir:1935/channel-live/smil:tv1/playlist.m3u8");
        playlist.add(MediaType.STREAM, "http://cdn1.live.irib.ir:1935/channel-live/smil:tv2/playlist.m3u8");
        playlist.add(MediaType.STREAM, "http://cdn1.live.irib.ir:1935/channel-live/smil:tv3/playlist.m3u8");
        playlist.add(MediaType.STREAM, "http://cdn1.live.irib.ir:1935/channel-live/smil:tv4/playlist.m3u8");
        playlist.add(MediaType.STREAM, "http://cdn1.live.irib.ir:1935/channel-live/smil:tv5/playlist.m3u8");
        playlist.add("http://hafezname.ir/filesDownload/003.mp3");
        playlist.add("//android_asset/chavoshi.mp3");
        playlist.add(MediaType.STREAM, "http://wpc.785f5.zetacdn.net/24785F5/bosnian/bosnianStream.mpd");
        playlist.add("http://civil.vmobile.ir/media/video/467d4d86c8ffe197dd587a29f6ba5b0d.mp4");
        playlist.add("http://aod.parstoday.com:81/archive/mp3/20170612/20170612-English-1_4.mp3");
//        playlist.add(MediaType.STREAM, "rtsp://stream.parstoday.com:1935/ws/irib8");
//        playlist.add(MediaType.URL, "http://civil.vmobile.ir/media/video/800e609a3c013adf9e95c155d4b76e4f.mp4");
//        playlist.add(MediaType.URL, "http://aod.parstoday.com:81/archive/mp3/20170613/20170613-English-1_5.mp3");
//        playlist.add(MediaType.URL, "http://aod.parstoday.com:81/archive/mp3/20170613/20170613-English-1_6.mp3");
        playlist.add(MediaType.STREAM, "http://stream.parstoday.com:1935/ws/irib8/playlist.m3u8");

        playerBox.setPlayList(playlist);

//
//        final AnimatedVectorDrawableCompat playToPause = AnimatedVectorDrawableCompat.create(this, R.drawable.play_to_pause);
//        final AnimatedVectorDrawableCompat pauseToPlay = AnimatedVectorDrawableCompat.create(this, R.drawable.pause_to_play);


//        playback.setBackgroundResource(R.drawable.play_to_pause);


//// 1. Create a default TrackSelector
//        Handler mainHandler = new Handler();
//        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
//        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
//
//// 2. Create a default LoadControl
//        LoadControl loadControl = new DefaultLoadControl();
//
//// 3. Create the player
//        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
//
//        String aau = "http://aod.parstoday.com:1935/wsaod/mp3:archive/mp3/20170209/20170209-English-2_3.mp3/playlist.m3u8";
//        String llv = "http://stream.parstoday.com:1935/ws/irib8/playlist.m3u8";
//
//        // Measures bandwidth during playback. Can be null if not required.
//        DefaultBandwidthMeter bandwidthMeter2 = new DefaultBandwidthMeter();
//// Produces DataSource instances through which media data is loaded.
//        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,Util.getUserAgent(this, "yourApplicationName"), bandwidthMeter2);
//// Produces Extractor instances for parsing the media data.
//        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
//// This is the MediaSource representing the media to be played.
//        MediaSource videoSource = new ExtractorMediaSource(Uri.parse(aau), dataSourceFactory, extractorsFactory, null, null);
//// Prepare the player with the source.
//        player.prepare(videoSource);
//
//
//
//
//
//
//
//        playback.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                player.getPlayWhenReady();
//                if(isPlayed){
////                    playback.setImageDrawable(pauseToPlay);
////                    pauseToPlay.start();
//                    playback.setBackgroundResource(R.drawable.pause_to_play);
//                }else{
////                    playback.setImageDrawable(playToPause);
////                    playToPause.start();
//                    playback.setBackgroundResource(R.drawable.play_to_pause);
//                }
//                isPlayed = !isPlayed;
//                ((Animatable) playback.getBackground()).start();
//
//
//            }
//        });
//
//
    }

    private void initPlayer() {
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekbar);
        SeekBar seekBar2 = (SeekBar) findViewById(R.id.seekbar2);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final ProgressBar loading = (ProgressBar) findViewById(R.id.loading);
        final View play = findViewById(R.id.play);
        final View pause = findViewById(R.id.pause);
        View stop = findViewById(R.id.stop);
        View previous = findViewById(R.id.previous);
        View next = findViewById(R.id.next);
        surfaceView = (SurfaceView) findViewById(R.id.surface);
        errorView = (TextView) findViewById(R.id.error);


        TextView runningTime = (TextView) findViewById(R.id.running_time);
        TextView remainingTime = (TextView) findViewById(R.id.remaining_time);
        final ImageView playback = (ImageView) findViewById(R.id.imageView);
        final TextView title = (TextView) findViewById(R.id.title);

        playerBox.setRepeatMode(PlayerBox.REPEAT_MODE_ALL);
        playerBox.setLooping(true);
        playerBox.setVideoScalingMode(PlayerBox.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        playerBox.addOnMediaChangedListener(new PlayerBox.OnMediaChangedListener() {
            @Override
            public void onMediaChanged(PlaylistModel playlist) {
                Log.d(TAG, "onMediaChanged: index: " + playlist.getCurrentIndex());
                title.setText(playlist.getMedia().file.getPath());
            }
        });

//        surfaceView.getLayoutParams().height = (int) Utility.getHeightByRatioInPixel(1.77f);
//        surfaceView.requestLayout();

        playerBox.getViewManager()
                .clear()
                .addPlayView(play)
                .addPauseView(pause)
                .addPlaybackView(playback, PlayerViewManager.PLAYBACK_PLAY_PAUSE)
                .addSeekBar(seekBar)
                .addSeekBar(seekBar2)
                .addProgressBar(progressBar)
                .addStopView(stop)
                .addNextView(next)
                .addPreviousView(previous)
                .addRunningTimeView(runningTime)
                .addRemainingTimeView(remainingTime)
                .setVideoSurfaceView(surfaceView)
//                .setVideoSurfaceHolder(surfaceView.getHolder())
                .setOnVideoSizeChangedListener(new PlayerController.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(int width, int height) {
                        Log.d(TAG, "onVideoSizeChanged: width:" + width + " ,height:" + height);
//                        surfaceView.getLayoutParams().height = (int) Utility.getHeightByRatioInPixel((float)width / (float)height);
//                        surfaceView.requestLayout();
                    }
                })
                .setOnPlayerStateListener(new OnPlayerStateListener() {
                    @Override
                    public void onStatusChanged(int playerStatus) {
                        switch (playerStatus){
                            case PlayerStatus.STATUS_BUFFERING:
                                loading.setVisibility(View.VISIBLE);
                                break;

                            case PlayerStatus.STATUS_BUFFERED:
                                loading.setVisibility(View.INVISIBLE);
                                break;

                            case PlayerStatus.STATUS_STOPPED:
                                if(playbackStatus) {
                                    play.setVisibility(View.VISIBLE);
                                    pause.setVisibility(View.INVISIBLE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        playback.setBackgroundResource(R.drawable.pause_to_play);
                                        ((Animatable) playback.getBackground()).start();
                                    }else{
                                        playback.setBackgroundResource(R.drawable.ic_play);

                                    }
                                    playbackStatus = false;
                                }
                                break;

                            case PlayerStatus.STATUS_PAUSED:
                                if(playbackStatus) {
                                    play.setVisibility(View.VISIBLE);
                                    pause.setVisibility(View.INVISIBLE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        playback.setBackgroundResource(R.drawable.pause_to_play);
                                        ((Animatable) playback.getBackground()).start();
                                    }else{
                                        playback.setBackgroundResource(R.drawable.ic_play);

                                    }
                                    playbackStatus = false;
                                }
                                break;

                            case PlayerStatus.STATUS_PLAYING:
                                if(!playbackStatus){
                                    play.setVisibility(View.INVISIBLE);
                                    pause.setVisibility(View.VISIBLE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        playback.setBackgroundResource(R.drawable.play_to_pause);
                                        ((Animatable) playback.getBackground()).start();
                                    }else{
                                        playback.setBackgroundResource(R.drawable.ic_pause);
                                    }
                                    playbackStatus = true;
                                }
                                break;

                        }
                    }

                    @Override
                    public void onPrepared(int mediaDuration) {
                        Log.v("PlayerDemo", "onPrepared: " + mediaDuration);

//                surfaceView.requestLayout();

                    }

                    @Override
                    public void onBuffer(int sec) {
                        Log.v("PlayerDemo", "onBuffer: " + sec);

                    }

                    @Override
                    public void onProgress(int sec) {

                    }

                    @Override
                    public void onError(int errorCode) {
                        Log.v("PlayerDemo", "onError: " + errorCode);
                        errorView.setText("Error: " + errorCode);


                    }
                });
    }


}
