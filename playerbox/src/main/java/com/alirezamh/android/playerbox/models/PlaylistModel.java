package com.alirezamh.android.playerbox.models;

import java.io.File;
import java.util.LinkedList;
import java.util.List;


/**
 * Author:      Alireza Mahmoudi
 * Last Update: 8/10/2016
 * Email:       mahmoodi.dev@gmail.com
 * Website:     alirezamh.com
 */

public class PlaylistModel {
    private List<MediaModel> playlist = new LinkedList<>();
    private int currentIndex = -1;
    private Object extra;

    /**
     * Put extra data
     * @param extra
     */
    public void putExtra(Object extra) {
        this.extra = extra;
    }

    /**
     * Get Extra data
     * @return
     */
    public Object getExtra() {
        return extra;
    }

    /**
     * Get current playlist index
     * @return
     */
    public int getCurrentIndex() {
        return currentIndex;
    }

    public MediaModel getMedia(int location){
        if(location < 0 || playlist.size()-1 < location) return null;
        currentIndex = location;
        return playlist.get(location);
    }
    public MediaModel getMedia(){
        if(currentIndex < 0) return null;
        return playlist.get(currentIndex);
    }

    public void add(MediaModel... medias){
        for (MediaModel media: medias) {
            playlist.add(media);
            mediaCounter(true);
        }
    }
    public void add(String... urls){
        for (String url: urls) {
            playlist.add(new MediaModel(url));
            mediaCounter(true);
        }
    }
    public void add(MediaType type, String... urls){
        for (String url: urls) {
            playlist.add(new MediaModel(url, type));
            mediaCounter(true);
        }
    }

    public void add(File... files){
        add(MediaType.FILE, files);
    }
    public void add(MediaType type, File... files){
        for (File file: files) {
            playlist.add(new MediaModel(file, type));
            mediaCounter(true);
        }
    }

    public void remove(int location){
        if(playlist.size() < location) return;
        playlist.remove(location);
        mediaCounter(false);
    }

    public void clear(){
        playlist.clear();
        mediaCounter(false);
    }

    public boolean next(){
        if(playlist.isEmpty()) return false;
        if(playlist.size() <= currentIndex +1) return false;
        getMedia().resetFlags();
        currentIndex++;
        return true;
    }

    public boolean previous(){
        if(currentIndex <= 0) return false;
        getMedia().resetFlags();
        currentIndex--;
        return true;
    }

    public boolean first(){
        if(playlist.isEmpty()) return false;
        if(currentIndex != 0) getMedia().resetFlags();
        currentIndex = 0;
        return true;
    }

    public boolean last(){
        if(playlist.isEmpty()) return false;
        if(currentIndex != playlist.size()-1) getMedia().resetFlags();
        currentIndex = playlist.size()-1;
        return true;
    }

    public boolean goTo(int location){
        if(playlist.size() - 1 < location || location < 0) return false;
        if(currentIndex != location) getMedia().resetFlags();
        currentIndex = location;
        return true;
    }

    public int size(){
        return playlist.size();
    }

    private void mediaCounter(boolean status){
        if(status && currentIndex < 0) currentIndex = 0;
        else if(!status && playlist.size() == 0) currentIndex = -1;
    }

    public void resetFlags() {
        for (MediaModel media: playlist) {
            media.resetFlags();
        }
        currentIndex = playlist.size() > 0 ? 0 : -1;
    }

    public boolean isLast(){
        return currentIndex + 1 == size();
    }

    public boolean isFirst(){
        return currentIndex == 0;
    }
}
