package com.ajc.project.mytube;

import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.id;

public class Home extends AppCompatActivity {

    Playlist playlist = new Playlist();
    Player player = new Player(this, playlist);

    ArrayList<Integer> uniqueIDs = new ArrayList<>();
    Map<String, Integer> urlToID = new HashMap<>();
    int currentUniqueID;

    TextView status, connectionErrorMsg;
    Button playButton, pauseButton, searchButton, removeProps;
    EditText searchText;
    LinearLayout listProps, playlistLayout;
    SeekBar progressBar;

    Thread threadSearch;
    NetworkStateReceiver networkStateReceiver;

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
        connectionErrorMsg = (TextView) findViewById(R.id.connectionErrorMsg);

        progressBar = (SeekBar) findViewById(R.id.progressBar);

        removeProps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText.setText("");
                removePropositions();
            }
        });

        // CONNECTION CHANGED LISTENER
        registerReceiver(new NetworkStateReceiver(this), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        this.uiButton("DEFAULT");
        this.player.playlistPreparator();

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.play();
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
            }
        });

        // SEARCH FIELD
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                searchMusic();
        }});
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchMusic();
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
                    player.progressBarChanged(progress);
                }
            }
        });
    }

    protected void searchMusic(){
        removePropositions();
        if(threadSearch != null && threadSearch.isAlive()){
            threadSearch.interrupt();
        }
        final Home it = this;
        threadSearch = new Thread(){
            @Override
            public void run(){
                String search = searchText.getText().toString();
                Api api = new Api("search", search);
                JSONArray results = api.getData();
                try{
                    for(int i=0; i<results.length(); i++){
                        final TextView prop = new TextView(it);
                        JSONObject video = results.getJSONObject(i);
                        final String TITLE = video.getString("title");
                        final String URL = video.getString("id");
                        prop.setText(TITLE);
                        prop.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                runOnUiThread(new Runnable() {
                                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                                    @Override
                                    public void run() {
                                        removePropositions();
                                        addToPlaylist(URL, TITLE);
                                    }
                                });
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
        LinearLayout uniqueMusic = new LinearLayout(this);

        uniqueMusic.setBackgroundColor(getResources().getColor(R.color.orange));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(5, 10, 5, 10);
        uniqueMusic.setLayoutParams(params);

        TextView music = new TextView(this);
        music.setTextSize(20);
        music.setText(title);

        final int uniqueID = View.generateViewId();
        uniqueIDs.add(uniqueID);
        urlToID.put(URL, uniqueID);
        music.setId(uniqueID);
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {   // TODO: Change color if loaded or not.
                player.play(URL);
                // REMOVE COLOR OLD
                findViewById(currentUniqueID).setBackgroundColor(player.isDownloaded(URL) ? getResources().getColor(R.color.green) :  getResources().getColor(R.color.orange));
                findViewById(currentUniqueID).invalidate();
                // SET COLOR CURRENT
                findViewById(uniqueID).setBackgroundColor(getResources().getColor(R.color.lightBlue));
                findViewById(uniqueID).invalidate();
                // SAVING CURRENT ID
                currentUniqueID = uniqueID;
            }
        });

        View line = new View(this);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                5
        );
        lineParams.setMargins(0, 10, 0, 10);
        line.setLayoutParams(lineParams);
        line.setBackgroundColor(Color.parseColor("#000000"));

        ImageButton remove = new ImageButton(this);
        remove.setBackgroundResource(R.drawable.close);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                50,
                50
        );
        remove.setLayoutParams(imageParams);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFromPlaylist(URL);
            }
        });

        uniqueMusic.addView(music);
        uniqueMusic.addView(remove);
        this.playlistLayout.addView(uniqueMusic);
        this.playlistLayout.addView(line);

        if(this.playlist.getSize() == 1){
            this.currentUniqueID = this.uniqueIDs.get(0);
        }
    }

    public void removeFromPlaylist(String url){
        int id = urlToID.get(url);
        ((LinearLayout) findViewById(id).getParent()).removeAllViews();
        int indexInArray = this.playlist.getPlaylist().indexOf(url);
        this.playlist.remove(indexInArray);
    }

    public void musicDownloaded(String url){
        final String URL = url;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int id = urlToID.get(URL);
                // CHANGE COLOR TO LOADED
                if(playlist.getSize() == 1){
                    player.play(URL);
                    findViewById(currentUniqueID).setBackgroundColor(getResources().getColor(R.color.lightBlue));
                    findViewById(currentUniqueID).invalidate();
                }else {
                    findViewById(id).setBackgroundColor(getResources().getColor(R.color.green));
                    findViewById(id).invalidate();
                }
            }
        });
    }

    public void setProgressBar(int val){
        this.progressBar.setProgress(val);
    }
    public void setProgressBarMax(int max){
        this.progressBar.setMax(max);
    }

    public void uiButton(String action){
        switch(action){
            case "DEFAULT":
                this.playButton.setEnabled(false);
                this.pauseButton.setEnabled(false);
                break;
            case "PLAY":
                this.playButton.setEnabled(false);
                this.pauseButton.setEnabled(true);
                break;
            case "PAUSE":
                this.playButton.setEnabled(true);
                this.pauseButton.setEnabled(false);
                break;
            case "STOP":
                this.playButton.setEnabled(true);
                this.pauseButton.setEnabled(false);
                break;
            default:
                System.out.println("NO ACTION");
                break;
        }
    }

    public void updateConectivityStatus(boolean status){
        this.connectionErrorMsg.setVisibility(status ? View.INVISIBLE : View.VISIBLE);
    }
}