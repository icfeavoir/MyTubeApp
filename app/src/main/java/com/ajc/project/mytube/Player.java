package com.ajc.project.mytube;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by pierre on 2017-12-20.
 */

public class Player {
    private static final String API_URL = "http://54.213.9.163/mytube/";

    private Home home;
    private MediaPlayer mediaPlayer;
    private Playlist playlist;

    private ArrayList<String> musicDownload = new ArrayList<>();
    private String currentMusic;

    Thread downloadThread;

    private boolean loop;
    private boolean shuffle;

    Player(Home home, Playlist playlist){
        this.home = home;
        this.playlist = playlist;
        this.currentMusic = "";
    }

    private void downloadMusic(String url){
        final String youtubeUrl = url;
        this.downloadThread = new Thread(){
            @Override
            public void run(){
                Api downloadMusic = new Api("url", youtubeUrl);
                downloadMusic.call();
                home.musicDownloaded(youtubeUrl);
            }
        };
        downloadThread.start();
        this.musicDownload.add(url);
    }

    public void play(String url){
        this.currentMusic = url;
        if(this.mediaPlayer != null){
            this.mediaPlayer.stop();
            this.mediaPlayer = null;
        }
        if(!this.isDownloaded(url)){
            this.downloadMusic(url);
            try {
                this.downloadThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.play();
    }

    public void play(){
        // PLAY TO START (NOT RESUME)
        if(this.mediaPlayer == null){
            this.home.setProgressBar(0);
            mediaPlayer = new MediaPlayer();
            Uri myUri = Uri.parse(API_URL+this.currentMusic+".mp3");
            try {
                mediaPlayer.setDataSource(home, myUri);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.prepare();
                this.home.setProgressBarMax(this.mediaPlayer.getDuration());
                mediaPlayer.start();
                playlist.next();

                //end of music
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        playNext();
                    }
                });
                this.home.uiButton("PLAY");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.mediaPlayer.start();
        this.home.uiButton("PLAY");
        this.startProgressBar();
    }

    public void pause(){
        this.mediaPlayer.pause();
        this.home.uiButton("PAUSE");
    }

    public void stop(){
        this.mediaPlayer.stop();
        this.home.uiButton("STOP");
        this.home.setProgressBar(0);
    }

    public void playNext(){
        String next = playlist.getNext();
        if(next != "") {
            play(next);
        }else{
            stop();
        }
    }

    public void playPrev(){

    }

    public void progressBarChanged(int progress){
        this.mediaPlayer.seekTo(progress);
    }

    private void startProgressBar(){
        Thread progressBar = new Thread(){
            @Override
            public void run(){
                while(mediaPlayer.isPlaying()){
                    home.setProgressBar(mediaPlayer.getCurrentPosition());
                    try {
                        this.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        progressBar.start();
    }

    public boolean isDownloaded(String url){
        return this.musicDownload.contains(url);
    }

    public void playlistPreparator(){
        Thread prepare = new Thread(){
            @Override
            public void run(){
                while(true){
                    for (String url: playlist.getPlaylist()) {
                        if(!isDownloaded(url)){
                            downloadMusic(url);
                        }
                    }
                    try {
                        this.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        prepare.start();
    }
}
