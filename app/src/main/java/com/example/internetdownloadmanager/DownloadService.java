package com.example.internetdownloadmanager;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.example.internetdownloadmanager.App.CHANNEL_ID;

public class DownloadService extends IntentService {

    private static final String TAG = "DownloadIntentService";

    private PowerManager.WakeLock wakeLock;


    public DownloadService() {
        super("DownloadService");
        setIntentRedelivery(true);
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Internet Download Manager")
                    .setContentText("Downloading...")
                    .setSmallIcon(R.drawable.ic_baseline_arrow_downward_24)
                    .build();

            startForeground(1, notification);


        }

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d(TAG, "OnHandleIntent: ");

        assert intent != null;
        String input = intent.getStringExtra("inputExtra");


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
