package com.ajc.project.mytube;

import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import static android.R.attr.min;

/**
 * Created by pierre on 2017-12-14.
 */

public class Playlist {

    private Home home;
    private ArrayList<String> playlist;

    Playlist(Home home){
        this.playlist = new ArrayList<String>();
    }

    public void add(final String url, String musicName){
        this.playlist.add(url);
    }

    public void remove(int index){
        this.playlist.remove(index);
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
