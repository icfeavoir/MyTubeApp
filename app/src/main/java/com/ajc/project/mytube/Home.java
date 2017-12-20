package com.ajc.project.mytube;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
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
    SeekBar progressBar;

    Thread threadSearch;

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

        progressBar = (SeekBar) findViewById(R.id.progressBar);

        removeProps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText.setText("");
                removePropositions();
            }
        });
        final Context it = this;
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                removePropositions();
                if(threadSearch != null && threadSearch.isAlive()){
                    threadSearch.interrupt();
                }
                threadSearch = new Thread(){
                    @Override
                    public void run(){
                        String search = searchText.getText().toString();
                        Api api = new Api("search", search);
                        JSONArray results = api.getData();
                        try{
                            for(int i=0; i<results.length(); i++){
                                TextView prop = new TextView(it);
                                JSONObject video = results.getJSONObject(i);
                                final String TITLE = video.getString("title");
                                final String URL = video.getString("id");
                                prop.setText(TITLE);
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
                                                Api video = new Api("url", URL);
                                                video.call();
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addToPlaylist(URL, TITLE);
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
                                final String TITLE = video.getString("title");
                                final String URL = video.getString("id");
                                prop.setText(TITLE);
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
                                                Api video = new Api("url", URL);
                                                video.call();
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        addToPlaylist(URL, TITLE);
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

        // PROGRESS BAR
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }
        });
    }

    protected void addProposition(TextView b){
        View line = new View(this);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5
        );
        lineParams.setMargins(0, 10, 0, 10);
        line.setLayoutParams(lineParams);
        line.setBackgroundColor(Color.parseColor("#000000"));
        this.listProps.addView(b);
        this.listProps.addView(line);
    }
    protected void removePropositions(){
        this.listProps.removeAllViews();
    }

    protected void addToPlaylist(String url, String title){
        final String URL = url;
        playlist.add(URL, title);
        TextView music = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(5, 10, 5, 10);
        music.setLayoutParams(params);
        music.setTextSize(20);
        music.setText(title);
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playMusic(URL);
            }
        });
        this.playlistLayout.addView(music);

        if(this.playlist.getSize() == 1){
            playMusic(this.playlist.getNext());
        }
    }

    protected void playMusic(String url){
        mediaPlayer.stop();
        this.progressBar.setProgress(0);
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
            mediaPlayer.prepare();
            this.progressBar.setMax(mediaPlayer.getDuration());
            mediaPlayer.start();
            playlist.next();

            // progress bar
            this.progressBar.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer.isPlaying()) {
                        progressBar.setProgress(mediaPlayer.getCurrentPosition());
                        progressBar.postDelayed(this, 100);
                    }
                }
            }, 100);

            //end of music
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    String next = playlist.getNext();
                    if(next != "") {
                        playMusic(next);
                    }else{
                        pauseButton.setEnabled(false);
                        playButton.setEnabled(true);
                        mediaPlayer.stop();
                    }
                }
            });
            playButton.setEnabled(false);
            pauseButton.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
