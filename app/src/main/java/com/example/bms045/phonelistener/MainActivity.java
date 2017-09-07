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
import android.widget.ToggleButton;

import com.example.bms045.phonelistener.services.SocketListener;
import com.example.bms045.phonelistener.utils.NetworkHelper;

public class MainActivity extends AppCompatActivity {

    private static Intent intent = null;
    private boolean networkOK;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(SocketListener.MY_SERVICE_PAYLOAD);
            String clientIP = intent.getStringExtra(SocketListener.CLIENT_IP);
            Toast.makeText(context, clientIP, Toast.LENGTH_SHORT).show();
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    };

      ToggleButton toggleButton;

    TextView output;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        output = (TextView) findViewById(R.id.output);

        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, new IntentFilter(SocketListener.MY_SERVICE_MESSAGE));


        toggleButton.setBackgroundColor(Color.RED);

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




    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (intent == null) {
            finish();
        } else {
            Toast.makeText(this, "Unable to exit while connected", Toast.LENGTH_SHORT).show();
        }

    }


    public void toggleHandler(View view) {



        if (((ToggleButton) view).isChecked()) {
            networkOK = NetworkHelper.hasNetworkAccess(MainActivity.this);
            if (networkOK != true) {
                Toast.makeText(MainActivity.this, "Network Unavailable", Toast.LENGTH_SHORT).show();
            } else if (intent == null) {

                output.append(NetworkHelper.getNetworkIpAddress(MainActivity.this) + ":" + SocketListener.SERVERPORT + "\n");

                Toast.makeText(MainActivity.this, "Start", Toast.LENGTH_SHORT).show();

                toggleButton.setBackgroundColor(Color.GREEN);

                intent = new Intent(MainActivity.this, SocketListener.class);
                intent.setData(Uri.parse(SocketListener.START_LISTENING));
                startService(intent);
            }
        } else {
            if (intent != null) {
                Toast.makeText(MainActivity.this, "Stop", Toast.LENGTH_SHORT).show();
                toggleButton.setBackgroundColor(Color.RED);
                intent.setData(Uri.parse(SocketListener.STOP_LISTENING));
                stopService(intent);
                intent = null;
                output.setText("");
            }
        }
    }

}

