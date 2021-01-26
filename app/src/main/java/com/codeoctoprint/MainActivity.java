package com.codeoctoprint;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.DateTimeException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    // Settings
    public static final String SETTINGS_FILE_NAME = "settings.json";

    // Disconnect
    public static final int DISCONNECT_TIME_MAX = 30000;


    // Progress bar notification
    public static final String CHANNEL_PROGRESSBAR_ID = "progressBar";
    public static final int NOTIFICATION_PROGRESSBAR_ID = 1;
    public static final long UPDATE_NOTIFICATION_DELAY = 20000;

    private SettingsJSON settings;

    private int failedFindingApi = 0;

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
                settings = new SettingsJSON(getFilesDir(), SETTINGS_FILE_NAME);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Determine whether we need API Key
        try {
            Intent intent;
            if (settings.getSettingsJSON().has("api_key")) {
                // Check if api is there
                checkIfApiIsAlive();
            } else {
                // Open API Getter Activity
                intent = new Intent(MainActivity.this, APIKeyGetter.class); // Your list's Intent
                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag (We don't want people coming back here)
                startActivity(intent);
                finish();
            }

        } catch (IOException | JSONException e) {
            finish();
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

    public void checkIfApiIsAlive() {
        // Request Queue
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        // Settings
        SettingsJSON settings = null;
        try {
            settings = new SettingsJSON(getFilesDir(), SETTINGS_FILE_NAME);

            JSONObject settingsJSON = settings.getSettingsJSON();

            // Check whether the api is accessible
            // Get Host and API Key
            String host = settingsJSON.getString("host");
            String apiKey = settingsJSON.getString("api_key");

            // URL
            URLCleanser cleaner = new URLCleanser();
            String url = cleaner.clean(host);

            //Check for version (see if the api is there)
            url = cleaner.combineURL(url, "api/version");

            JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("foo","responced");
                            Intent intent = new Intent(MainActivity.this, ControlActivity.class); // Your list's Intent
                            intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY); // Adds the FLAG_ACTIVITY_NO_HISTORY flag (We don't want people coming back here)
                            startActivity(intent);
                            finish();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            failedFindingApi++;
                            if (failedFindingApi >= 3) {
                                // TODO open logout/reconnect page / can't connect
                            } else {
                                // Try again
                                checkIfApiIsAlive();
                            }
                        }
                    }
            ) {
                // API Key
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("X-Api-Key", apiKey);

                    return params;
                }
            };

            queue.add(getRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}