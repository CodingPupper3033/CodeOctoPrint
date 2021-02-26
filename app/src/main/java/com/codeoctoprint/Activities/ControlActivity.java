package com.codeoctoprint.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.codeoctoprint.APIConnections.Progress.APIRequestJob;
import com.codeoctoprint.APIConnections.Progress.PrintProgress;
import com.codeoctoprint.APIConnections.Progress.PrintProgressListener;
import com.codeoctoprint.R;
import com.codeoctoprint.Useful.SettingsReader;

import java.io.IOException;

public class ControlActivity extends AppCompatActivity {
    public static ProgressBar progressBarPrint;

    private SettingsReader settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{ this.getSupportActionBar().hide(); } catch (NullPointerException e){}
        setContentView(R.layout.activity_control);

        // Set the settings file
        while (settings == null) {
            try {
                settings = new SettingsReader(getFilesDir(), MainActivity.SETTINGS_FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Update progressBarPrint
        progressBarPrint = findViewById(R.id.progressBarPrint);

        // Start Progressbar Notification
        //Intent serviceIntent = new Intent(this, ProgressBarService.class);
        //serviceIntent.putExtra(CHANNEL_PROGRESSBAR_ID, "Print Progress Notification");
        //ContextCompat.startForegroundService(this, serviceIntent);

        // Job Progress
        APIRequestJob job = new APIRequestJob(getApplicationContext(), settings);
        job.addPrintProgressListener(new PrintProgressListener() {
            @Override
            public void update(PrintProgress printProgress) {
                Log.d("TAG", "update: " + printProgress.isConnected());
            }
        });
    }
}

