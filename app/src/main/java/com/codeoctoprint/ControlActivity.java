package com.codeoctoprint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.VideoView;

import static com.codeoctoprint.MainActivity.CHANNEL_PROGRESSBAR_ID;

public class ControlActivity extends AppCompatActivity {
    public static ProgressBar progressBarPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{ this.getSupportActionBar().hide(); } catch (NullPointerException e){}
        setContentView(R.layout.activity_control);

        // Update progressBarPrint
        progressBarPrint = findViewById(R.id.progressBarPrint);

        // Start Progressbar Notification
        Intent serviceIntent = new Intent(this, ProgressBarService.class);
        serviceIntent.putExtra(CHANNEL_PROGRESSBAR_ID, "Print Progress Notification");
        ContextCompat.startForegroundService(this, serviceIntent);

        // TEMP Show video

    }
}