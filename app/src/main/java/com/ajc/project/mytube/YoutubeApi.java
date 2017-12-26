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

class YoutubeApi {
    private static final String API_URL = "http://54.213.9.163/mytube/";
    private Map<String, Object> data;
    private String endpoint;
    private int my_id;

    YoutubeApi(String endpoint, Map<String, Object> data) {
        this.data = data;
        this.endpoint = endpoint;
        this.my_id = 1;
    }
    YoutubeApi(String endpoint) {
        this(endpoint, new HashMap<String, Object>());
    }

    void addData(String key, Object value) {
        this.data.put(key, value);
    }

    public JSONArray getData() {
        JSONArray json = new JSONArray();
        try {
            String resp = new CallAPI(API_URL, endpoint, data, my_id).execute().get();
            System.out.println(resp);
            json = new JSONArray(resp);
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

    public CallAPI(String apiUrl, String endpoint, Map<String, Object> data, int my_id){
        this.apiUrl = apiUrl;
        String postData = "";
        try {
            this.data = URLEncoder.encode("endpoint", "UTF-8") + "=" + URLEncoder.encode(endpoint, "UTF-8");
            for (Map.Entry<String, Object> entry : data.entrySet()){
                this.data += "&" + URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue().toString(), "UTF-8");
            }
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