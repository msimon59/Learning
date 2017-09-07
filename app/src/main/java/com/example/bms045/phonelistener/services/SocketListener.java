package com.example.bms045.phonelistener.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class SocketListener extends IntentService {


    public static final String START_LISTENING = "start_listening";
    public static final String STOP_LISTENING = "stop_listening";
    public static final String TAG = "SocketListener";
    public static final int SERVERPORT = 6000;

    public static final String MY_SERVICE_MESSAGE = "myServiceMessage";
    public static final String MY_SERVICE_PAYLOAD = "myServicePayload";
    public static final String CLIENT_IP = "client_ip";

    private static ServerSocket serverSocket;


    Handler updateConversationHandler;
    Thread serverThread = null;
    private TextView text;

    public SocketListener() {
        super("MySocket");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Uri uri = intent.getData();
        Log.i(TAG, "onHandleIntent: " + uri.toString());

        if (uri.toString().equals(START_LISTENING)) {
            try {
                start_Listener();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private void start_Listener() throws InterruptedException {
        updateConversationHandler = new Handler();
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
        this.serverThread.join();
    }


    @Override
    public void onDestroy() {

        super.onDestroy();


        try {
            this.serverThread.interrupt();
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "onDestroy: Shouldn't be here");
        }

    }


    private class ServerThread implements Runnable {
        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {

                try {

                    socket = serverSocket.accept();
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (IOException e) {
                    Log.i(TAG, "run: Socket has been closed");
                }
            }
        }
    }

    class CommunicationThread implements Runnable {

        private Socket clientSocket;

        private BufferedReader input;

        public CommunicationThread(Socket clientSocket) {

            this.clientSocket = clientSocket;

            try {

                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {

            while (!Thread.currentThread().isInterrupted()) {

                String read;
                Intent messageIntent = new Intent(MY_SERVICE_MESSAGE);

                try {
                    read = input.readLine();
                    if (read != null) {
                        String clientIP = clientSocket.getInetAddress().getHostName().toString();
                        clientIP += ":" + String.valueOf((clientSocket.getPort()));
                        messageIntent.putExtra(MY_SERVICE_PAYLOAD, read);
                        messageIntent.putExtra(CLIENT_IP, clientIP);
                        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
                        manager.sendBroadcast(messageIntent);
                        Log.i(TAG, "updateUIThread: " + read);
                    }
                    //updateConversationHandler.post(new updateUIThread(read));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    class updateUIThread implements Runnable {
        private String msg;

        public updateUIThread(String str) {
            this.msg = str;
            Log.i(TAG, "updateUIThread: " + this.msg);
        }

        @Override
        public void run() {
            text.setText(text.getText().toString() + "Client Says: " + msg + "\n");
        }
    }
}
