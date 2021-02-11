package com.codeoctoprint.Activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.codeoctoprint.APIConnections.ConnectionStatus;
import com.codeoctoprint.APIConnections.ConnectionToAPI;
import com.codeoctoprint.R;
import com.codeoctoprint.Useful.SettingsReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    // Settings
    public static final String SETTINGS_FILE_NAME = "settings.json";

    // Default settings
    // API Requests
        // Job
        public static final long DEFAULT_JOB_REQUEST_DELAY = 20000;
        public static final int DEFAULT_MAX_ERRORS_BEFORE_DISCONNECT = 3;



    // Progress bar notification
    public static final String CHANNEL_PROGRESSBAR_ID = "progressBar";
    public static final int NOTIFICATION_PROGRESSBAR_ID = 1;

    private SettingsReader settings;

    private final int failedFindingApi = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the title bar
        try{ this.getSupportActionBar().hide(); } catch (NullPointerException e){}
        setContentView(R.layout.activity_main);

        // Create the ability for notifications
        createNotificationChannels();

        // Set the settings file
        while (settings == null) {
            try {
                settings = new SettingsReader(getFilesDir(), SETTINGS_FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Make sure we have all needed settings
        try {
            // TODO Remove
                JSONObject temp = settings.getSettingsJSON();
                temp.remove("job_request_delay");
                settings.setSettingsJSON(temp);

            if (verifySettings(settings)) {
                // Check if we can connect to the API
                ConnectionToAPI checkIfAlive = new ConnectionToAPI(getApplicationContext(), settings, new ConnectionStatus() {
                    @Override
                    public void onDisconnect(VolleyError error) {
                        Intent intent = new Intent(MainActivity.this, NoInternetActivity.class); // Your list's Intent
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onConnect() {
                        Intent intent = new Intent(MainActivity.this, ControlActivity.class); // Your list's Intent
                        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag (We don't want people coming back here)
                        startActivity(intent);
                        finish();
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void createNotificationChannels() {
        // Progressbar
        NotificationChannel progressBar = new NotificationChannel(
                CHANNEL_PROGRESSBAR_ID,
                "Print Progressbar",
                NotificationManager.IMPORTANCE_LOW);

        progressBar.setDescription("Shows a progressbar of the print progress");
        progressBar.setShowBadge(false);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(progressBar);


    }

    public boolean verifySettings(SettingsReader settings) throws IOException, JSONException {
        // "job_request_delay"
        if (!settings.getSettingsJSON().has("job_request_delay")) settings.setSettingsJSON(settings.getSettingsJSON().put("job_request_delay",DEFAULT_JOB_REQUEST_DELAY));

        // "max_errors_before_disconnect"
        if (!settings.getSettingsJSON().has("max_errors_before_disconnect")) settings.setSettingsJSON(settings.getSettingsJSON().put("max_errors_before_disconnect",DEFAULT_MAX_ERRORS_BEFORE_DISCONNECT));

        // "host" or "api_key"
        if (!settings.getSettingsJSON().has("host") || !settings.getSettingsJSON().has("api_key")) {
            Intent intent = new Intent(MainActivity.this, APIKeyGetterActivity.class); // Your list's Intent
            intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag (We don't want people coming back here)
            startActivity(intent);
            finish();
            return false;
        }

        return true;
    }
}