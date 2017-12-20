package com.ajc.project.mytube;

import java.util.ArrayList;

/**
 * Created by pierre on 2017-12-14.
 */

public class Playlist {

    private ArrayList<String> playlist;
    private int current;

    Playlist(){
        this.playlist = new ArrayList<String>();
        this.current = -1;
    }

    public void add(final String url, String musicName){
        this.playlist.add(url);
    }
    public void remove(int index){
        this.playlist.remove(index);
    }

    public int getSize(){
        return this.playlist.size();
    }

    public String getNext(){
        try {
            return this.playlist.get(this.current+1);
        } catch ( IndexOutOfBoundsException e ) {
            // TODO: loop mode
            return "";
        }
    }
    public void next(){
        this.current++;
    }

    public ArrayList<String> getPlaylist(){
        return this.playlist;
    }

    public void move(int from, int to){
        int j;
        String url = this.playlist.get(from);
        for(int i=Math.max(from, to); i<Math.max(from, to); i++){
            j = from < to ? i+1 : i-1;      // j = i+1 --> asc we get the next to put in the prev
            if(i == to){
                this.playlist.set(to, url);
            }else {
                this.playlist.set(i, this.playlist.get(j));
            }
        }
    }
}
