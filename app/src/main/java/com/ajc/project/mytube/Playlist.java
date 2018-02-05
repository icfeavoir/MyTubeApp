package com.ajc.project.mytube;

import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by pierre on 2017-12-14.
 */

public class Playlist {

    private Home home;
    private SharedPreferences.Editor editor;
    private ArrayList<String> playlist;
    private int current;

    Playlist(Home home){
        this.home = home;
        this.playlist = new ArrayList<String>();
        this.current = -1;
    }

    public void setPreferences(SharedPreferences.Editor editor){
        this.editor = editor;
    }

    public void add(final String url, String musicName){
        this.editor.putString("url_"+this.getSize(), url);
        this.editor.putString("title_"+this.getSize(), musicName);
        this.editor.commit();
        this.playlist.add(url);
    }
    public void remove(int index){
        this.editor.clear();
        this.editor.commit();
        this.playlist.remove(index);
        String url;
        for(int i=index; i<this.playlist.size()-1; i++){
            url = this.playlist.get(i+1);
            this.playlist.set(i, url);
            this.editor.putString("url_"+i, url);
            this.editor.putString("title_"+i, this.home.urlToTitle.get(url));
            this.editor.commit();
        }
    }
    public void removeAll(){
        this.playlist.clear();
        this.editor.clear();
        this.editor.commit();
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
