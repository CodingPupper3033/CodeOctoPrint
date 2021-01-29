package com.codeoctoprint;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import static com.codeoctoprint.Activities.MainActivity.CHANNEL_PROGRESSBAR_ID;

//TODO Rewrite this, it dirty

public class ProgressBarService extends Service {
    public static final String TAG = CHANNEL_PROGRESSBAR_ID + " Notification";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }





    /*
    private Timer timer;
    private TimerTask timerTaskSeconds;

    private NotificationCompat.Builder notificationBuilder;

    private int currentPrintTime;
    private int currentPrintTimeTotal;

    private int disconnectedTime = 0;
    private boolean disconnected = true;

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

    public void showNotificationConnecting() {

        notificationBuilder = getBasicBuilder()
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("Connecting to OctoPrint")
                .setColor(16737792) // Orange
                .setProgress(0, 0,true);

        ControlActivity.progressBarPrint.setIndeterminate(true);

        startForeground(NOTIFICATION_PROGRESSBAR_ID, notificationBuilder.build());
    }

    public void showNotificationAmount(int completed, int secondsLeft) {
        int daysLeftOut = (int)Math.floor(secondsLeft/(60*60*24));
        int hoursLeftOut = (int)Math.floor(secondsLeft/(60*60)%24);
        int minutesLeftOut = (int)Math.floor(secondsLeft/(60)%60);;
        int secondsLeftOut = secondsLeft%60;

        // Total
        int total = completed + secondsLeft;

        // Progress
        double progress = completed*100/total;

        // Format percent left
        String outP = (int)(progress*100)/100.0 + "%";

        // Format time left
        String outT = secondsLeftOut + "s";
        if (minutesLeftOut != 0) {
            outT = minutesLeftOut + "m " + outT;
        }
        if (hoursLeftOut != 0) {
            outT = hoursLeftOut + "h " + outT;
        }
        if (daysLeftOut != 0) {
            outT = daysLeftOut + "d " + outT;
        }

        // Update
        currentPrintTime = completed;
        currentPrintTimeTotal = total;


        notificationBuilder = getProgressBuilder()
                // TODO Add time left
                .setContentTitle(outP + " Time left: " + outT)
                .setProgress(100, (int)(progress),false);

        ControlActivity.progressBarPrint.setIndeterminate(false);
        ControlActivity.progressBarPrint.setMax(100);
        ControlActivity.progressBarPrint.setProgress((int)(progress));
        startForeground(NOTIFICATION_PROGRESSBAR_ID, notificationBuilder.build());
    }

    public void showNotificationProgress(double progress, int secondsLeft) {
        int daysLeftOut = (int)Math.floor(secondsLeft/(60*60*24));
        int hoursLeftOut = (int)Math.floor(secondsLeft/(60*60)%24);
        int minutesLeftOut = (int)Math.floor(secondsLeft/(60)%60);;
        int secondsLeftOut = secondsLeft%60;

        // Format percent left
        String outP = (int)(progress*100)/100.0 + "%";

        // Format time left
        String outT = secondsLeftOut + "s";
        if (minutesLeftOut != 0) {
            outT = minutesLeftOut + "m " + outT;
        }
        if (hoursLeftOut != 0) {
            outT = hoursLeftOut + "h " + outT;
        }
        if (daysLeftOut != 0) {
            outT = daysLeftOut + "d " + outT;
        }


        NotificationCompat.Builder notificationBuilder = getProgressBuilder();
                notificationBuilder.setContentTitle(outP + " Time left: " + outT);
                notificationBuilder.setProgress(100, (int)(progress),false);

        ControlActivity.progressBarPrint.setIndeterminate(false);
        ControlActivity.progressBarPrint.setMax(100);
        ControlActivity.progressBarPrint.setProgress((int)(progress));

        startForeground(NOTIFICATION_PROGRESSBAR_ID, notificationBuilder.build());
    }

    public void showNotificationCantConnect() {

        notificationBuilder = getBasicBuilder()
                .setContentTitle("Unable to connect to Octoprint");

        ControlActivity.progressBarPrint.setIndeterminate(true);

        startForeground(NOTIFICATION_PROGRESSBAR_ID, notificationBuilder.build());
    }

    public void showNotificationNotPrinting() {

        notificationBuilder = getBasicBuilder()
                .setContentTitle("Octoprint is not printing");

        ControlActivity.progressBarPrint.setIndeterminate(true);

        startForeground(NOTIFICATION_PROGRESSBAR_ID, notificationBuilder.build());
    }

    public void hideNotification() {
        ControlActivity.progressBarPrint.setIndeterminate(true);
        stopForeground(true);

    }

    public void createNotificationBuilder() {
        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_PROGRESSBAR_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setColor(16737792) // Orange
                .setOnlyAlertOnce(true);
    }


    public NotificationCompat.Builder getBasicBuilder() {
        if (notificationBuilder == null) {
            createNotificationBuilder();
        }
        return notificationBuilder;
    }

    // TODO Might want to show a cancel button and pause/resume button
    public NotificationCompat.Builder getProgressBuilder() {
        notificationBuilder = getBasicBuilder();
        return notificationBuilder;
    }

    public void onDisconnect() {
        disconnected = true;
    }

    public void onConnect() {
        disconnectedTime = 0;
        disconnected = false;
    }


    private class updateProgressBar extends TimerTask {
        int count = 0;
        @Override
        public void run() {
            try {
                // Request Queue
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

                // Settings
                SettingsReader settings = new SettingsReader(getFilesDir(), SETTINGS_FILE_NAME);
                JSONObject settingsJSON = settings.getSettingsJSON();

                // Get Host and API Key
                String host = settingsJSON.getString("host");
                String apiKey = settingsJSON.getString("api_key");

                // URL
                URLCleanser cleaner = new URLCleanser();
                String url = cleaner.clean(host);


                //Retrieve information about the current job | GET /api/job
                url = cleaner.combineURL(url, "api/job");

                JSONObject params = new JSONObject();
                params.put("X-Api-Key", apiKey);

                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,params, new UpdateProgressbarResponse(), new UpdateProgressbarErrorResponse())
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("X-Api-Key", apiKey);

                        return params;
                    }
                };

                // Add the request to the RequestQueue.
                queue.add(jsonRequest);


            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class UpdateProgressbarResponse implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            // Not disconnected
            if (disconnected) {
                onConnect();
            }

            // Log response
            Log.d(TAG, "Response " + response);

            // Show progress
            try {
                JSONObject progressJSON = response.getJSONObject("progress");
                if (!progressJSON.isNull("printTime") && !progressJSON.isNull("printTimeLeft")) {
                    double completion = progressJSON.getDouble("completion");
                    double printTime = progressJSON.getDouble("printTime");
                    int printTimeLeft = progressJSON.getInt("printTimeLeft");


                    // Makes it update every second until it refreshes
                    if (timerTaskSeconds != null) {
                        timerTaskSeconds.cancel();
                    }
                    timerTaskSeconds = new UpdateProgressTimeEverySecond((int)printTime, (int)printTimeLeft);
                    Timer secondsTimer = new Timer();
                    secondsTimer.scheduleAtFixedRate(timerTaskSeconds,0,1000);
                } else {
                    // No current print job running
                    if (timerTaskSeconds != null) {
                        timerTaskSeconds.cancel();
                    }
                    showNotificationNotPrinting();
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class UpdateProgressbarErrorResponse implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, error.toString());
            if (error instanceof TimeoutError) {
                if (disconnectedTime >= DISCONNECT_TIME_MAX && DISCONNECT_TIME_MAX != -1) {
                    onDisconnect();
                    hideNotification();
                } else {
                    disconnectedTime += UPDATE_NOTIFICATION_DELAY;
                    showNotificationCantConnect();
                }
            } else {
                // Not disconnected per say
                onConnect();
            }

        }
    }

    public class UpdateProgressTimeEverySecond extends TimerTask {
        private int printTime;
        private int printTimeLeft;
        private int secondsPassed = 0;

        public UpdateProgressTimeEverySecond(int printTime, int printTimeLeft) {
            this.printTime = printTime;
            this.printTimeLeft = printTimeLeft;
        }
        @Override
        public void run() {
            showNotificationAmount(printTime+secondsPassed, printTimeLeft-secondsPassed);
            Log.d(TAG, "run: " + secondsPassed + " " + printTime+secondsPassed);
            if (!(printTime+secondsPassed >= printTimeLeft-secondsPassed)) {
                secondsPassed++;
            }

        }
    }

     */
}
