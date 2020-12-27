package com.codeoctoprint;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static com.codeoctoprint.MainActivity.CHANNEL_PROGRESSBAR_ID;
import static com.codeoctoprint.MainActivity.DISCONNECT_TIME_MAX;
import static com.codeoctoprint.MainActivity.NOTIFICATION_PROGRESSBAR_ID;
import static com.codeoctoprint.MainActivity.SETTINGS_FILE_NAME;
import static com.codeoctoprint.MainActivity.UPDATE_NOTIFICATION_DELAY;

public class ProgressBarService extends Service {
    public static final String TAG = CHANNEL_PROGRESSBAR_ID + " Notification";

    private Timer timer;

    private int disconnectedTime = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotificationConnecting();
        timer = new Timer();
        timer.scheduleAtFixedRate(new updateProgressBar(), 0, UPDATE_NOTIFICATION_DELAY);
        return START_STICKY;
    }

    // TODO Might want to show a cancel button and pause/resume button
    public void showNotificationConnecting() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_PROGRESSBAR_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("Connecting to OctoPrint")
                .setColor(16737792) // Orange
                .setProgress(0, 0,true);

        startForeground(NOTIFICATION_PROGRESSBAR_ID, notificationBuilder.build());
    }

    public void showNotificationProgress(double progress) {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_PROGRESSBAR_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                // TODO Add time left
                .setContentTitle("Time left: ")
                .setColor(16737792) // Orange
                .setProgress(100, (int)(progress*100),false);

        startForeground(NOTIFICATION_PROGRESSBAR_ID, notificationBuilder.build());
    }

    public void showNotificationCantConnect() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_PROGRESSBAR_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("Unable to connect to Octoprint")
                .setColor(16737792); // Orange

        startForeground(NOTIFICATION_PROGRESSBAR_ID, notificationBuilder.build());
    }

    public void hideNotification() {
        stopForeground(true);
    }


    private class updateProgressBar extends TimerTask {
        int count = 0;
        @Override
        public void run() {
            try {
                // Request Queue
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                // Settings
                SettingsJSON settings = new SettingsJSON(getFilesDir(), SETTINGS_FILE_NAME);
                JSONObject settingsJSON = settings.getSettingsJSON();

                // Get Host and API Key
                String host = settingsJSON.getString("host");
                String apiKey = settingsJSON.getString("api_key");

                // URL
                URLCleanser cleaner = new URLCleanser();
                String url = cleaner.clean(host);


                //Retrieve information about the current job | GET /api/job
                url = cleaner.combineURL(url, "api/job");


                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, new updateProgressbarResponse(), new updateProgressbarErrorResponse());

                // Add the request to the RequestQueue.
                queue.add(jsonRequest);


            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class updateProgressbarResponse implements Response.Listener<JSONObject> {

        @Override
        public void onResponse(JSONObject response) {
            // Not disconnected
            disconnectedTime = 0;

            // Log response
            Log.d("foo", response.toString());
        }
    }

    public class updateProgressbarErrorResponse implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, error.toString());
            if (error instanceof TimeoutError) {
                if (disconnectedTime >= DISCONNECT_TIME_MAX && DISCONNECT_TIME_MAX != -1) {
                    hideNotification();
                } else {
                    disconnectedTime += UPDATE_NOTIFICATION_DELAY;
                    showNotificationCantConnect();
                }
            } else {
                // Not disconnected per say
                disconnectedTime = 0;
            }

        }
    }
}
