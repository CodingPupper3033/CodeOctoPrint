package com.codeoctoprint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;

import static com.codeoctoprint.MainActivity.CHANNEL_PROGRESSBAR_ID;

public class ControlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        // Start Progressbar Notification
        Intent serviceIntent = new Intent(this, ProgressBarService.class);
        serviceIntent.putExtra(CHANNEL_PROGRESSBAR_ID, "Print Progress Notification");
        ContextCompat.startForegroundService(this, serviceIntent);
    }
}