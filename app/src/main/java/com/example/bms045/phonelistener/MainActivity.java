package com.example.bms045.phonelistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bms045.phonelistener.services.SocketListener;
import com.example.bms045.phonelistener.utils.NetworkHelper;

public class MainActivity extends AppCompatActivity {

    private static Intent intent = null;
    private boolean networkOK;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(SocketListener.MY_SERVICE_PAYLOAD);
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    };

    Button startButton;
    Button stopButton;


    TextView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = (TextView) findViewById(R.id.output);
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, new IntentFilter(SocketListener.MY_SERVICE_MESSAGE));

        startButton.setBackgroundColor(Color.GREEN);
        stopButton.setBackgroundColor(Color.LTGRAY);


    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.activity_main);
        Drawable drBackground = null;

// Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            drBackground = ContextCompat.getDrawable(this, R.drawable.pooh_landscape);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            drBackground = ContextCompat.getDrawable(this, R.drawable.pooh);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            rl.setBackground(drBackground);
        } else {
            rl.setBackgroundDrawable(drBackground);
        }

        if (intent != null) {
            output.refreshDrawableState();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
    }


    public void startClickHandler(View view) {

        networkOK = NetworkHelper.hasNetworkAccess(this);
        if (networkOK != true) {
            Toast.makeText(this, "Network Unavailable", Toast.LENGTH_LONG).show();
        } else if (intent == null) {

            output.append(NetworkHelper.getNetworkIpAddress(this) + ":" + SocketListener.SERVERPORT + "\n");

            Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
            startButton.setBackgroundColor(Color.LTGRAY);
            stopButton.setBackgroundColor(Color.RED);

            intent = new Intent(this, SocketListener.class);
            intent.setData(Uri.parse(SocketListener.START_LISTENING));
            startService(intent);
        }

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(intent == null){
            finish();
        }  else {
            Toast.makeText(this, "Unable to exit while connected", Toast.LENGTH_SHORT).show();
        }

    }

    public void stopClickHandler(View view) {
        if (intent != null) {
            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
            startButton.setBackgroundColor(Color.GREEN);
            stopButton.setBackgroundColor(Color.LTGRAY);
            intent.setData(Uri.parse(SocketListener.STOP_LISTENING));
            stopService(intent);
            intent = null;
            output.setText("");
        }
    }

}

