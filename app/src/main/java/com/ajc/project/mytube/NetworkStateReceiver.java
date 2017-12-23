package com.ajc.project.mytube;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import static com.ajc.project.mytube.R.id.status;

/**
 * Created by pierre on 2017-12-21.
 */

public class NetworkStateReceiver extends BroadcastReceiver {
    private Home home;

    NetworkStateReceiver(Home home){
        this.home = home;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            ConnectivityManager connMgr = (ConnectivityManager) home.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            home.updateConectivityStatus(networkInfo != null && networkInfo.isConnected());
        }
    }
}