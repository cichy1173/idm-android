package com.example.internetdownloadmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    Button downloadButton, getDataButton;
    TextView addressTextView, fileSizeTextView, fileTypeTextView, downloadedTextView;
    EditText addressEditText;
    ProgressBar progressBar;
    private static final int PERMISSION_STORAGE_CODE = 69;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent serviceIntent = new Intent(this, DownloadService.class);


        fileSizeTextView = findViewById(R.id.fileSize);
        fileTypeTextView = findViewById(R.id.fileType);
        addressEditText = findViewById(R.id.address_textEdit);
        downloadButton = findViewById(R.id.download_button);

        getDataButton = findViewById(R.id.getData_button);

        progressBar = findViewById(R.id.progressBar);

        getDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addressEditText.getText().toString().isEmpty()) {
                    addressEditText.setError("Empty!");

                } else {
                    getDownloadInfo();

                }

            }
        });


     downloadButton.setOnClickListener(new View.OnClickListener() {


         @Override
         public void onClick(View v) {
            // ContextCompat.startForegroundService(MainActivity.this, serviceIntent);

             if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                     PackageManager.PERMISSION_DENIED) {

                 String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                 requestPermissions(permissions, PERMISSION_STORAGE_CODE);
             }

             else {

                 if (addressEditText.getText().toString().isEmpty()) {
                     addressEditText.setError("Empty!");
                 } else {
                     startDownloading();
                 }

             }
         }
     });

    }

    private String getMimeType(Uri url) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(url));

    }

    public void startDownloading() {

        String url = addressEditText.getText().toString().trim();
       // Uri uri = Uri.parse(url);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);

        request.setTitle(url);
        request.setDescription("Downloading...");
        request.setAllowedOverMetered(true);
       // request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "" + System.currentTimeMillis());

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_STORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED)
                    startDownloading();

                else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }


        }



    }

    @SuppressLint("StaticFieldLeak")
    protected class DownloadTaskAsynchronous extends AsyncTask<String, Integer, String[]> {

        @SuppressLint("DefaultLocale")
        @Override
        protected String[] doInBackground(String... strings) {
            Log.d("DownloadTaskAsynchronous", "Run in background");
            URL url;
            try {
                url = new URL(strings[0]);
            } catch (MalformedURLException e) {
                Log.d("Error: ", e.getMessage());
                return null;
            }
            HttpsURLConnection httpsURLConnection = null;
            try {
                httpsURLConnection = (HttpsURLConnection) url.openConnection();

            } catch (IOException e) {
                Log.d("Error: ", e.getMessage());
            }

            if (httpsURLConnection == null) {
                return null;
            }
            try {
                httpsURLConnection.setRequestMethod("GET");
            } catch (ProtocolException e) {
                Log.d("Error: ", Objects.requireNonNull(e.getMessage()));
            }

            int size = httpsURLConnection.getContentLength();
            String type = httpsURLConnection.getContentType();

            Log.d("Info: ", size + " " + type);

            httpsURLConnection.disconnect();

            String[] ret = new String[2];

            ret[0] = String.format("%.2f", size / 1000000.0f);
            ret[1] = type;

            return ret;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            String size = strings[0] + " megabytes";
            fileSizeTextView.setText(size);
            fileTypeTextView.setText(strings[1]);
            super.onPostExecute(strings);
        }
    }

    public void getDownloadInfo() {
        DownloadTaskAsynchronous downloadTaskAsynchronous = new DownloadTaskAsynchronous();
        downloadTaskAsynchronous.execute(addressEditText.getText().toString());
    }


}

