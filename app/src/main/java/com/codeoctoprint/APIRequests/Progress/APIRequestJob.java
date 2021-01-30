package com.codeoctoprint.APIRequests.Progress;

import android.util.Log;

import com.codeoctoprint.SettingsReader;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class APIRequestJob {
    // Settings
    SettingsReader settings;
        int MAX_ERRORS_BEFORE_DISCONNECT;

    // Timers
    Timer requestFromAPITimer;

    // Listeners
    private final ArrayList<PrintProgressListener> PrintProgressListeners;

    // TAG
    private final String TAG = "API Request Job";

    public APIRequestJob(SettingsReader settings) {
        PrintProgressListeners = new ArrayList<PrintProgressListener>();

        this.settings = settings;


        try {
            MAX_ERRORS_BEFORE_DISCONNECT = settings.getSettingsJSON().getInt("max_errors_before_disconnect");
            long delay = settings.getSettingsJSON().getLong("job_request_delay");
            // Start Timer
            requestFromAPITimer = new Timer();
            requestFromAPITimer.scheduleAtFixedRate(new RequestFromAPI(), 0, delay);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addPrintProgressListener(PrintProgressListener listener) {
        PrintProgressListeners.add(listener);
    }

    private class RequestFromAPI extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "Going to request now!");
        }
    }

}
