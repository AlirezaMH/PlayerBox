package com.alirezamh.android.playerbox.playerController.mediaPlayer;

import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.opengl.EGL14;
import android.opengl.GLES20;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.alirezamh.android.playerbox.PlayerStatus;
import com.alirezamh.android.playerbox.models.MediaModel;
import com.alirezamh.android.playerbox.models.MediaType;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;


/**
 * Author:      Alireza Mahmoudi
 * Last Update: 8/8/2016
 * Email:       mahmoodi.dev@gmail.com
 * Website:     alirezamh.com
 */

public class Player extends AsyncTask<Integer, Integer, Integer>
        implements MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener,
        SurfaceHolder.Callback{

    private final PlayerStatus playerStatus;
    private MediaType type = null;
    private Timer timer = null;
    private MediaPlayer mp = null;
    private int seekTo = -1;
    private MediaModel media;
    private boolean isPrepareMP = false;
    private boolean autoPlay = false;
    private boolean playAfterBuffer = false;
    private final static String TAG = "MEDIA_PLAYER";
    private int currentPosition;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private boolean isSurfaceCreated = false;
    private int videoScalingMode = -1;


    public Player(MediaModel media, PlayerStatus playerStatus){
        this.media = media;
        this.playerStatus = playerStatus;
        this.playerStatus.setMedia(media);
    }

    /**
     * Show playing status
     * @return
     */
    public boolean isPlaying() {
        try {
            if (mp == null) return false;
            return mp.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }


    public void play() {

        if (!media.isFile() && !connectionCheck())
            return;
        // if(playerState == STATUS_BUFFERING) return;
        if (isPrepare() && !media.isStream()) {
            mp.start();
            playerStatus.sendTask(PlayerStatus.STATUS_PLAYING);

        } else {
            if (media.isStream()) {
                playStream();

            } else {
                playArchive();
            }
        }
    }

    private void playArchive() {

//        Log.d(TAG, "play archive");
        if (!media.isFile() && !connectionCheck()) return;
        if (media == null) return;

        if (media.isReady()) {

//            Log.d(TAG, "archive prepared");
            if (isPlaying()) {
//                Log.d(TAG, "archive stop");
                mp.pause();
                playerStatus.sendTask(PlayerStatus.STATUS_PAUSED);

            } else {
//                Log.d(TAG, "archive start");
                mp.start();
                playerStatus.sendTask(PlayerStatus.STATUS_PLAYING);
            }

        } else if (media.isBuferring()) {
            playAfterBuffer = true;
            return;

        } else {
//            Log.d(TAG, "archive load new");
            playAfterBuffer = true;
            playerStatus.sendTask(PlayerStatus.STATUS_PAUSED);
            playerStatus.sendTask(PlayerStatus.STATUS_PROGRESS, 0);
            playerStatus.sendTask(PlayerStatus.STATUS_BUFFERING);
//            run();
            if(mp != null) mp.start();
        }

    }

    private boolean playStream() {

        if (!connectionCheck()) return false;
        if (media == null) return false;

        if (media.isReady()) { // stop
            playerStatus.sendTask(PlayerStatus.STATUS_STOPPED);
            stop();
            return false;
        }

        else if (media.isBuferring()) {

//            playerStatus.sendTask(PlayerStatus.STATUS_BUFFERING);
            return true;
        } else {
            playAfterBuffer = true;
            playerStatus.sendTask(PlayerStatus.STATUS_STOPPED);
            playerStatus.sendTask(PlayerStatus.STATUS_PROGRESS, 0);
            playerStatus.sendTask(PlayerStatus.STATUS_BUFFERING, 0);
            run();
            return true;
        }

    }

    public void pause() {
        playAfterBuffer = false;
        if (!isPrepare())
            destroy();

        else
            mp.pause();
        playerStatus.sendTask(PlayerStatus.STATUS_PAUSED);
    }

    public void stop() {
        playAfterBuffer = false;
        seekTo = -1;

        if (!isPrepare()) {
            return;
        }

        if (media.isStream()) {
            destroy(); // what do I do?
            return;
        }
//        Log.v(TAG, "stop: " + mp.getCurrentPosition() + " ,is playing: " + mp.isPlaying());
        if(mp.getCurrentPosition() > 0){
//            Log.d(TAG, "stop1: pause");
            mp.seekTo(0);
            mp.pause();
        }

        playerStatus.sendTask(PlayerStatus.STATUS_STOPPED);
        playerStatus.sendTask(PlayerStatus.STATUS_PROGRESS, 0);
    }

    public void seekTo(int time) {
        if (media.isStream()) {
            return;
        }
//        Log.v(TAG, "seekTo: " + time + " ,is playing: " + mp.isPlaying());

        playerStatus.sendTask(PlayerStatus.STATUS_PROGRESS, time);
        if (mp != null) {
            if(!isPrepare()){
                seekTo = time;
                return;
            }
//            if(mp.isPlaying()){
            playAfterBuffer = mp.isPlaying();
//            }
            playerStatus.sendTask(PlayerStatus.STATUS_BUFFERING);
            mp.start();
            mp.seekTo(time);
        } else {
            playerStatus.sendTask(PlayerStatus.STATUS_BUFFERING);
            run(time);
        }

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
//        Log.v(TAG, "onProgressUpdate " + values[1]);

        switch (values[0]) {
            case PlayerStatus.STATUS_PROGRESS:
                if (media.isStream()) break;
                currentPosition = values[1];
                playerStatus.sendTask(PlayerStatus.STATUS_PROGRESS, values[1]);
                break;

            case PlayerStatus.STATUS_PLAYING:
                playerStatus.sendTask(PlayerStatus.STATUS_PLAYING);
                break;

//            case PlayerStatus.STATUS_BUFFERING:
//                playerStatus.sendTask(PlayerStatus.STATUS_BUFFERING);
//                break;

            case PlayerStatus.STATUS_ERROR:
                playerStatus.sendTask(PlayerStatus.STATUS_STOPPED);
                playerStatus.sendTask(PlayerStatus.STATUS_ERROR, (values.length > 1 ? values[1] : PlayerStatus.ERROR_UNKNOWN));
                destroy();
                break;
        }
        super.onProgressUpdate(values);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
//        Log.v(TAG, "onBufferingUpdate %" + percent + " duration: " + mp.getDuration() + " buffer: " + Math.round(mp.getDuration() * ((float)percent / 100.f)));

        playerStatus.sendTask(PlayerStatus.STATUS_BUFFER, Math.round(mp.getDuration() * ((float)percent / 100.f)));

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
//        Log.v(TAG, "onCompletion");

        if(isAutoPlay()){
            mp.start();
            return;
        }
        playerStatus.sendTask(PlayerStatus.STATUS_ENDED);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError what: " + what + " extra: " + extra);

        isPrepareMP = false;

        playerStatus.sendTask(PlayerStatus.STATUS_ERROR, extra);
        return false;
    }

    @Override
    protected void onCancelled() {
//        Log.v(TAG, "onCancelled");

        isPrepareMP = false;
        playerStatus.sendTask(PlayerStatus.STATUS_ERROR, PlayerStatus.ERROR_UNKNOWN);
        super.onCancelled();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
//        Log.v(TAG, "onPrepared: ,playAfterBuffer:" + playAfterBuffer + " ,autoPlay:" + autoPlay);
        playerStatus.sendTask(PlayerStatus.STATUS_BUFFERED);
        if (isCancelled())
            return;
        isPrepareMP = true;
        playerStatus.sendTask(PlayerStatus.STATUS_PREPARED, mp.getDuration());

        if(autoPlay || playAfterBuffer){
            playAfterBuffer = false;
            mp.start();
            playerStatus.sendTask(PlayerStatus.STATUS_PLAYING);
        }
        if(seekTo > 0){
            mp.seekTo(seekTo);
            seekTo = -1;
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
//        Log.v(TAG, "onSeekComplete ,playAfterBuffer:" + playAfterBuffer + " ,is playing:" + mp.isPlaying());
        playerStatus.sendTask(PlayerStatus.STATUS_BUFFERED);
        if (playAfterBuffer){
            playerStatus.sendTask(PlayerStatus.STATUS_PLAYING);
            playAfterBuffer = false;
            mp.start();
        }else{
            mp.pause();
        }
    }

    @Override
    protected void onPreExecute() {
//        Log.v(TAG, "onPreExecute");
        playerStatus.sendTask(PlayerStatus.STATUS_BUFFERING);
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Integer... params) {
//        Log.v(TAG, "doInBackground: " + getStatus());
        if (type != MediaType.FILE && !connectionCheck()) {
            publishProgress(PlayerStatus.STATUS_ERROR, null);
            return null;
        }

        if (mp != null) {
            prepareFileToPlay();
            return null;
        }

        String link;
        if(media.isFile()){
            if(!media.file.exists()){
                publishProgress(PlayerStatus.STATUS_ERROR, Error.FILE_NOT_FOUND);
                return null;
            }
            link = media.file.getPath();

        }else{
            link = media.url;
        }

        if (link == ""){
            publishProgress(PlayerStatus.STATUS_ERROR, Error.FILE_NOT_FOUND);
            return null;
        }


        mp = new MediaPlayer();
        mp.setOnBufferingUpdateListener(this);
        mp.setOnCompletionListener(this);
        mp.setOnErrorListener(this);
        mp.setOnSeekCompleteListener(this);
        mp.setOnPreparedListener(this);
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setOnVideoSizeChangedListener(this);
        if(videoScalingMode > -1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
//            mp.setVideoScalingMode(videoScalingMode);
        }
        initSurfaceView();


        try {
            mp.setDataSource(link);
        } catch (IOException e) {
            e.printStackTrace();
            publishProgress(PlayerStatus.STATUS_ERROR, Error.FILE_NOT_FOUND);
        }

        prepareFileToPlay();

        if (params.length > 0) seekTo = params[0];


        if (type != MediaType.STREAM) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {

                    if (isPlaying())
                        publishProgress(PlayerStatus.STATUS_PROGRESS, mp.getCurrentPosition()); // TODO
                }

            }, 0, 1000);
        }
        return null;
    }

    private boolean connectionCheck() {
        return true;
    }

    protected void prepareFileToPlay() {
        try {
            if (media.isFile()) {
                mp.prepare();
            } else {
                mp.prepareAsync();

            }

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "PFTP Error1: ", e);
            publishProgress(PlayerStatus.STATUS_ERROR, null);
             e.printStackTrace();

        } catch (SecurityException e) {
            Log.e(TAG, "PFTP Error2: ", e);
            publishProgress(PlayerStatus.STATUS_ERROR, Error.SECURITY);
             e.printStackTrace();

        } catch (IllegalStateException e) {
            Log.e(TAG, "PFTP Error3: ", e);
            publishProgress(PlayerStatus.STATUS_ERROR, null);
            // TODO Auto-generated catch block
             e.printStackTrace();

        } catch (IOException e) {
            Log.e(TAG, "PFTP Error4: ", e);

             e.printStackTrace();
        }

    }

    private boolean isPrepare() {
        if (mp == null) return false;
        return isPrepareMP;
    }

    public void destroy() {
//        Log.v(TAG, "destroy");
        currentPosition = 0;
        try {

            cancel(true);
            // PLAYER = null;

            if (timer != null) {
                timer.cancel();
                timer.purge();
                timer = null;
            }
            if (mp != null) {
                clearSurfaceView();
                final MediaPlayer mediaPlayer = mp;
                mp = null;

                new Thread() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null) {
                            mediaPlayer.reset();
                            mediaPlayer.release();
                        }

                        super.run();
                    }
                }.start();

            }

        } catch (Exception e) {
            Log.e(TAG, "destroy Error: ", e);
        }

    }


    private void run() {
        run(-1);
    }
    private void run(int seek) {
        Log.d(TAG, "run ,seek: " + seek);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            int corePoolSize = 60;
            int maximumPoolSize = 80;
            int keepAliveTime = 10;

            BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(maximumPoolSize);
            Executor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
            executeOnExecutor(threadPoolExecutor, seek);
        } else {
            execute(seek);
        }
//        sendTask(PlayerStatus.STATUS_PLAYING);
    }

    public MediaModel getMedia() {
        return media;
    }

    public void setAutoPlay(boolean status){
        autoPlay = status;
    }

    public boolean isAutoPlay(){
        return autoPlay;
    }

    public void prepare(){
        if(isPrepare()) return;
        run();

    }

    public void playAfterBuffer(boolean status){
        playAfterBuffer = status;
    }

    public int getDuration(){
        if(mp == null) return 0;
        return mp.getDuration();
    }

    public int getCurrentPosition(){
        return currentPosition;
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder){
        this.surfaceHolder = surfaceHolder;
    }

    public void setSurfaceView(SurfaceView surfaceView){
        this.surfaceView = surfaceView;
        setSurfaceHolder(surfaceView.getHolder());
        Log.d(TAG, "setSurfaceView: " + isSurfaceCreated);
    }

    public void clearSurfaceView(){
        if(surfaceHolder != null){
            Log.d(TAG, "clearSurfaceView: SurfaceHolder");
            surfaceHolder.removeCallback(this);
            surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
            surfaceHolder.setFormat(PixelFormat.OPAQUE);
        }
        if(surfaceView != null){
            Log.d(TAG, "clearSurfaceView: SurfaceView");
            surfaceView.requestLayout();
//            clearSurface(surfaceView);
        }
    }
    public void removeSurfaceView(){
        if(surfaceHolder != null) {
            Log.d(TAG, "removeSurfaceView: SurfaceHolder");
            surfaceHolder.removeCallback(this);
            surfaceHolder = null;
        }
        if(surfaceView != null){
            Log.d(TAG, "removeSurfaceView: SurfaceView");
            surfaceView = null;
//            isSurfaceCreated = true; //I don't know why?
        }
    }


    private void initSurfaceView(){
        if(surfaceHolder == null) return;
        Log.d(TAG, "initSurfaceView: ");
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        surfaceHolder.addCallback(this);
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
        Log.d(TAG, "surfaceCreated:");
        isSurfaceCreated = true;
        if(mp != null) mp.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: ");
        if(mp != null) mp.setDisplay(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isSurfaceCreated = false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

        if(playerStatus.getOnVideoSizeChangedListener() != null) playerStatus.getOnVideoSizeChangedListener().onVideoSizeChanged(width, height);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setVideoScalingMode(int videoScalingMode){
        this.videoScalingMode = videoScalingMode;
        if(mp != null) mp.setVideoScalingMode(videoScalingMode);
    }

    public MediaPlayer getPlayer(){
        return mp;
    }
}
