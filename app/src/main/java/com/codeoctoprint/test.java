package com.codeoctoprint;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.codeoctoprint.MainActivity.CHANNEL_PROGRESSBAR_ID;
import static com.codeoctoprint.MainActivity.NOTIFICATION_PROGRESSBAR_ID;

public class test extends AppCompatActivity {
    private NotificationManagerCompat notificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        TextView view = findViewById(R.id.textView);

        SettingsJSON settings = null;
        String SETTINGS_FILE_NAME = "settings.json";
        while (settings == null) {
            try {
                settings = new SettingsJSON(getFilesDir(), SETTINGS_FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JSONObject settingsJSON = null;
        try {
            settingsJSON = settings.getSettingsJSON();
            view.setText("We got an api key!:" + settingsJSON.getString("api_key"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent serviceIntent = new Intent(this, ProgressBarService.class);
        serviceIntent.putExtra(CHANNEL_PROGRESSBAR_ID, "Print Progress Notification");
        ContextCompat.startForegroundService(this, serviceIntent);

    }
}