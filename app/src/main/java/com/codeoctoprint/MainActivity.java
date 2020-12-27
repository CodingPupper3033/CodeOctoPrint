package com.codeoctoprint;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;


import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public static final String SETTINGS_FILE_NAME = "settings.json";

    // Progress bar notification
    public static final String CHANNEL_PROGRESSBAR_ID = "progressBar";
    public static final int NOTIFICATION_PROGRESSBAR_ID = 1;
    public static final long UPDATE_NOTIFICATION_DELAY = 5000;

    SettingsJSON settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the title bar
        try{ this.getSupportActionBar().hide(); } catch (NullPointerException e){}
        setContentView(R.layout.activity_main);


        // Set the settings file
        while (settings == null) {
            try {
                settings = new SettingsJSON(getFilesDir(), SETTINGS_FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Determine whether we need API Key

        try {
            Intent intent;
            if (settings.getSettingsJSON().has("api_key")) {
                // Create the ability for notifications
                createNotificationChannels();

                // Open Control Activity
                intent = new Intent(MainActivity.this, ControlActivity.class); // Your list's Intent
            } else {
                // Open API Getter Activity
                intent = new Intent(MainActivity.this, APIKeyGetter.class); // Your list's Intent
            }
            intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag
            startActivity(intent);
            finish();
        } catch (IOException | JSONException e) {
            System.exit(0);
        }
    }

    public void createNotificationChannels() {
        NotificationChannel progressBar = new NotificationChannel(
                CHANNEL_PROGRESSBAR_ID,
                "Print Progressbar",
                NotificationManager.IMPORTANCE_LOW);

        progressBar.setDescription("Shows a progressbar of the print progress");
        progressBar.setShowBadge(false);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(progressBar);
    }
}