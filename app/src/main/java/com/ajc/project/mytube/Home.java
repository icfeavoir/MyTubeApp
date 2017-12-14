package com.ajc.project.mytube;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class Home extends AppCompatActivity {
    private static final String API_URL = "http://54.213.9.163/mytube/";

    MediaPlayer mediaPlayer = new MediaPlayer();
    Playlist playlist = new Playlist(this);

    TextView status;
    Button playButton, pauseButton, searchButton, removeProps;
    EditText searchText;
    LinearLayout listProps, playlistLayout;

    //z4PWAu9HfxM

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        listProps = (LinearLayout) findViewById(R.id.propositionsList);
        playlistLayout = (LinearLayout) findViewById(R.id.playlist);

        playButton = (Button)findViewById(R.id.startPlayerBtn);
        pauseButton = (Button)findViewById(R.id.pausePlayerBtn);
        searchButton = (Button) findViewById(R.id.searchBtn);
        removeProps = (Button) findViewById(R.id.removePropositions);

        searchText = (EditText) findViewById(R.id.searchText);

        status = (TextView) findViewById(R.id.status);

        removeProps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePropositions();
            }
        });
        final Context it = this;
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePropositions();
                Thread threadSearch = new Thread(){
                    @Override
                    public void run(){
                        String search = searchText.getText().toString();
                        Api api = new Api("search", search);
                        JSONArray results = api.getData();
                        try{
                            for(int i=0; i<results.length(); i++){
                                TextView prop = new TextView(it);
                                JSONObject video = results.getJSONObject(i);
                                prop.setText(video.getString("title"));
                                final String url = video.getString("id");
                                prop.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                removePropositions();
                                                status.setText("Loading video...");
                                            }
                                        });
                                        Thread threadUrl = new Thread(){
                                            @Override
                                            public void run(){
                                                Api video = new Api("url", url);
                                                video.call();
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addToPlaylist(url, "Test");
                                                        status.setText("");
                                                    }
                                                });
                                            }
                                        };
                                        threadUrl.start();
                                    }
                                });
                                prop.setTextSize(20);
                                final TextView PROPOSITION = prop;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addProposition(PROPOSITION);
                                    }
                                });
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                };
                threadSearch.start();
            }
        });
    }

    protected void addProposition(TextView b){
        this.listProps.addView(b);
    }
    protected void removePropositions(){
        this.listProps.removeAllViews();
    }

    protected void addToPlaylist(String url, String title){
        final String URL = url;
        playlist.add(URL, "Test");
        TextView music = new TextView(this);
        music.setText(title);
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic(URL);
            }
        });
        this.playlistLayout.addView(music);
    }

    protected void playMusic(String url){
        mediaPlayer.stop();
        mediaPlayer = new MediaPlayer();
        playButton.setEnabled(false);
        pauseButton.setEnabled(false);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                pauseButton.setEnabled(true);
                playButton.setEnabled(false);
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
                pauseButton.setEnabled(false);
                playButton.setEnabled(true);
            }
        });
        Uri myUri = Uri.parse(API_URL+url+".mp3");
        try {
            mediaPlayer.setDataSource(this, myUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepare(); //don't use prepareAsync for mp3 playback
            mediaPlayer.start();
            playButton.setEnabled(false);
            pauseButton.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
