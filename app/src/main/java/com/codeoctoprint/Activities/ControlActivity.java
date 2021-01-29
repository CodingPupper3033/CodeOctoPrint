package com.codeoctoprint.Activities;

import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.codeoctoprint.R;

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
        //Intent serviceIntent = new Intent(this, ProgressBarService.class);
        //serviceIntent.putExtra(CHANNEL_PROGRESSBAR_ID, "Print Progress Notification");
        //ContextCompat.startForegroundService(this, serviceIntent);

        // TEMP
    }
}

