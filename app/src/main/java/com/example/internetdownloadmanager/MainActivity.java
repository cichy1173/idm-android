package com.example.internetdownloadmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
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

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    Button downloadButton, getDataButton;
    TextView addressTextView, fileSizeTextView, fileTypeTextView, downloadedTextView;
    EditText addressEditText;
    ProgressBar progressBar;
    ProgressDialog progressDialog;
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

      //  progressBar = findViewById(R.id.progressBar);

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

                     new DownloadFile().execute(addressEditText.getText().toString());
                 }

             }
         }
     });

    }

    private class DownloadFile extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Progress bar");
            progressDialog.setMessage("Downloading...");
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                int fileLength = connection.getContentLength();
                String filePath = Environment.getExternalStorageDirectory().getPath();

                InputStream input = new BufferedInputStream(url.openStream());

                OutputStream output = new FileOutputStream(filePath + "/" + System.currentTimeMillis());

                byte data[] =  new byte[1024];

                long total = 0;

                int count;

                while ((count = input.read(data)) != -1) {
                    total+=count;

                    publishProgress((int) (total*100/fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }
    }


    private String getMimeType(Uri url) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(url));

    }

    public void startDownloading() {

        String url = addressEditText.getText().toString().trim();
        Uri uri = Uri.parse(url);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);

        request.setTitle(url);
        request.setDescription("Downloading...");
        request.setAllowedOverMetered(true);
       // request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, System.currentTimeMillis() + ".png");

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

