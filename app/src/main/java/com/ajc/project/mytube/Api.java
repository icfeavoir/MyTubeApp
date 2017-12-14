package com.ajc.project.mytube;

/**
 * Created by pierre on 2017-12-11.
 */

import android.os.AsyncTask;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.key;

class Api {
    private static final String API_URL = "http://54.213.9.163/mytube/";
    private String videoUrl;
    private String endpoint;

    Api(String endpoint, String videoUrl) {
        this.videoUrl = videoUrl;
        this.endpoint = endpoint;
    }

    public JSONArray getData() {
        JSONArray json = new JSONArray();
        try {
            System.out.println("Started");
            String resp = new CallAPI(API_URL, this.endpoint, this.videoUrl).execute().get();
            json = new JSONArray(resp);
            System.out.println(json);
        } catch (Exception e) {
            System.out.println("No JSON");
            e.printStackTrace();
        }
        return json;
    }

    public JSONArray call(){
        return this.getData();
    }
}

class CallAPI extends AsyncTask<String, String, String> {

    private String apiUrl;
    private String data;

    public CallAPI(String apiUrl, String endpoint, String videoUrl){
        this.apiUrl = apiUrl;
        try {
            this.data = URLEncoder.encode(endpoint, "UTF-8") + "=" + URLEncoder.encode(videoUrl, "UTF-8");
        }catch(Exception e){
            System.out.println("NO DATA");
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String text = "";
        BufferedReader reader = null;
        try{
            URL url = new URL(this.apiUrl);

            // Send POST data request
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(this.data);
            wr.flush();

            // Get the server response
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            text = sb.toString();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                reader.close();
            }catch(Exception ex) {}
        }
        return text;
    }
}