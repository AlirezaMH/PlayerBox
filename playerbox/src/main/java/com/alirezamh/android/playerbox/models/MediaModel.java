package com.alirezamh.android.playerbox.models;

import java.io.File;


/**
 * Author:      Alireza Mahmoudi
 * Last Update: 8/10/2016
 * Email:       mahmoodi.dev@gmail.com
 * Website:     alirezamh.com
 */

public class MediaModel {
    public enum STATUS{NOT_READY, READY, BUFFERING, PLAYING}
    public String url = "";
    public File file;
    public MediaType type;
    protected STATUS status = STATUS.NOT_READY;

    public MediaModel(String url){
        if(url.startsWith("file://") || url.startsWith("android://") || url.startsWith("//")){
            this.type = MediaType.FILE;
        }else{
            this.type = MediaType.URL;
        }

        this.url = url;
        file = new File(url);
    }

    public MediaModel(String url, MediaType type){
        this.url = url;
        file = new File(url);
        this.type = type;
    }

    public MediaModel(File file){
        this(file, MediaType.FILE);
    }

    public MediaModel(File file, MediaType type){
        this.file = file;
        this.type = type;
    }

    public boolean isStream(){
        return type == MediaType.STREAM;
    }
    public boolean isArchive(){
        return type == MediaType.URL;
    }
    public boolean isFile(){
        return type == MediaType.FILE;
    }

    public boolean isReady(){
        return status == STATUS.READY;
    }
    public boolean isBuferring(){
        return status == STATUS.BUFFERING;
    }
    public boolean isPlaying(){
        return status == STATUS.PLAYING;
    }

    public int getDuration(){
        return 0;
    }

    public void resetFlags(){
        status = STATUS.NOT_READY;
    }

    public void setStatus(STATUS status){
        this.status = status;
    }

    public STATUS getStatus(){
        return status;
    }




}
