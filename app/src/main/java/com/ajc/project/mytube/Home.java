package com.ajc.project.mytube;

import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.duration;
import static android.R.attr.positiveButtonText;

/* TODO:
    - Notification Player
    - OnLock Player
    - Params View
    - Playlist Manager!
    - Loop & Shuffle
*/
public class Home extends AppCompatActivity {
    final int ME = 1;

    private DrawerLayout drawerLayout;
    private ArrayList<String> drawerItemsList = new ArrayList<>();
    private ListView myDrawer;

    Playlist playlist = new Playlist();
    Player player = new Player(this, playlist);

    ArrayList<Integer> uniqueIDs = new ArrayList<>();
    Map<String, Integer> urlToID = new HashMap<>();
    Map<String, String> urlToTitle = new HashMap<>();
    int currentUniqueID;

    TextView status, connectionErrorMsg, playlistTitleView;
    Button playButton, pauseButton, searchButton, removeProps, savePlaylist;
    EditText searchText;
    LinearLayout listProps, playlistLayout;
    SeekBar progressBar;

    Thread threadSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // DRAWER (MENU)
        final Context IT = this;
        myDrawer = (ListView) findViewById(R.id.my_drawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerListener(new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // get all playlists
                YoutubeApi api = new YoutubeApi("Playlists/select");
                api.addData("creator", ME);
                JSONArray playlistsJson = api.getData();
                for(int i=0; i<playlistsJson.length(); i++){
                    try {
                        String playlistTitle = playlistsJson.getJSONObject(i).getString("title");
                        // add only if this playlist not added yet
                        if(drawerItemsList.size() < i+1 || !drawerItemsList.get(i).equals(playlistTitle)) {
                            drawerItemsList.add(playlistTitle);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                myDrawer.setAdapter(new ArrayAdapter<String>(IT, R.layout.drawer_line, drawerItemsList));
                myDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            System.out.println(position);
                        }
                    }
                );
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);

        listProps = (LinearLayout) findViewById(R.id.propositionsList);
        playlistLayout = (LinearLayout) findViewById(R.id.playlist);

        playButton = (Button)findViewById(R.id.startPlayerBtn);
        pauseButton = (Button)findViewById(R.id.pausePlayerBtn);
        searchButton = (Button) findViewById(R.id.searchBtn);
        removeProps = (Button) findViewById(R.id.removePropositions);
        savePlaylist = (Button) findViewById(R.id.savePlaylist);

        searchText = (EditText) findViewById(R.id.searchText);

        status = (TextView) findViewById(R.id.status);
        connectionErrorMsg = (TextView) findViewById(R.id.connectionErrorMsg);
        playlistTitleView = (TextView) findViewById(R.id.playlistTitle);

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

        savePlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePlaylist();
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
                JSONArray results = null;
                try {
                    YoutubeApi api = new YoutubeApi("endpoint/search");
                    api.addData("search", search);
                    results = api.getData();
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
        // rename playlist title (because its not a saved playlist)
        playlistTitleView.setText("Playlist");

        final String URL = url;
        playlist.add(URL, title);

        LinearLayout musicLayout = new LinearLayout(this);
        musicLayout.setOrientation(LinearLayout.HORIZONTAL);
        musicLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // MUSIC TITLE
        TextView music = new TextView(this);
        LinearLayout.LayoutParams musicParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                90
        );
        music.setLayoutParams(musicParams);
        music.setPadding(0, 5, 5, 5);
        music.setTextSize(20);
        music.setText(title);

        final int uniqueID = View.generateViewId();
        uniqueIDs.add(uniqueID);
        urlToID.put(URL, uniqueID);
        urlToTitle.put(URL, title);

        music.setId(uniqueID);
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        // TRASH
        ImageButton remove = new ImageButton(this);
        LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(
                50,
                50,
                10
        );
        removeParams.gravity = Gravity.RIGHT;
        remove.setLayoutParams(removeParams);
        remove.setForegroundGravity(Gravity.CENTER);
        remove.setBackgroundResource(R.drawable.close);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFromPlaylist(URL);
            }
        });

        // LINE
        View line = new View(this);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        line.setLayoutParams(lineParams);
        line.setBackgroundColor(Color.parseColor("#000000"));

        musicLayout.addView(music);
        musicLayout.addView(remove);
        this.playlistLayout.addView(musicLayout);
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

    public void savePlaylist(){
        // DIALOG FOR TITLE
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.popup, null);
        final EditText TITLE_FIELD = view.findViewById(R.id.popupTitle);
        final ArrayList<String> PLAYLIST = this.playlist.getPlaylist();
        final Context IT = this;
        builder.setView(view);
        builder.setPositiveButton("ok", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = TITLE_FIELD.getText().toString();
                if(title.equals("")){
                    Toast toast = Toast.makeText(IT, "Please enter a title", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }else {
                    YoutubeApi api = new YoutubeApi("playlists/insert");
                    api.addData("creator", 1);
                    api.addData("title", title);
                    try {
                        int playlist_id = (int) api.getData().get(0);
                        for (String url : PLAYLIST) {
                            api = new YoutubeApi("PlaylistItems/insert");
                            api.addData("playlist_id", playlist_id);
                            api.addData("creator", 1);
                            api.addData("title", urlToTitle.get(url));
                            api.addData("url", url);
                            api.call();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                    Toast toast = Toast.makeText(IT, "Playlist saved", Toast.LENGTH_SHORT);
                    toast.show();
                    playlistTitleView.setText(title);
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
        this.searchButton.setEnabled(status);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle home icon selection
        drawerLayout.openDrawer(Gravity.LEFT);
        return true;
    }
}