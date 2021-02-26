package com.codeoctoprint.APIConnections.Progress;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.codeoctoprint.APIConnections.ConnectionStatus;
import com.codeoctoprint.APIConnections.ConnectionToAPI;
import com.codeoctoprint.Useful.SettingsReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class APIRequestJob extends ConnectionToAPI {
    // Settings
    SettingsReader settings;
        int MAX_ERRORS_BEFORE_DISCONNECT;

    // Timers
    Timer requestFromAPITimer;

    // Listeners
    private final ArrayList<PrintProgressListener> printProgressListeners;

    // TAG
    private final String TAG = "API Request Job";

    // Print Progress
    private PrintProgress mainPrintProgress;

    // Context
    private Context context;

    public APIRequestJob(Context context, SettingsReader settings) {
        super(context, settings, true);
        this.context = context;
        printProgressListeners = new ArrayList<PrintProgressListener>();

        this.settings = settings;
        mainPrintProgress = new PrintProgress();

        // On updates it will update the print progress listener
        addConnectionStatus(new ConnectionStatus() {
            @Override
            public void onDisconnect(VolleyError error) {
                mainPrintProgress.setConnected(false);
                updateAll();
            }

            @Override
            public void onConnect() {
                mainPrintProgress.setConnected(true);
                updateAll();
            }
        });


        try {
            MAX_ERRORS_BEFORE_DISCONNECT = settings.getSettingsJSON().getInt("max_errors_before_disconnect");
            long delay = settings.getSettingsJSON().getLong("job_request_delay");

            // Start Timer for request
            requestFromAPITimer = new Timer();
            requestFromAPITimer.scheduleAtFixedRate(new RequestFromAPI(), 0, delay);

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeContext(Context context) {
        this.context = context;
    }

    public void addPrintProgressListener(PrintProgressListener listener) {
        printProgressListeners.add(listener);
    }

    private void updateAll() {
        updateAll(mainPrintProgress);
    }
    private void updateAll(PrintProgress printProgress) {
        for (int i = 0; i < printProgressListeners.size(); i++) {
            printProgressListeners.get(i).update(printProgress);
        }
    }

    private class RequestFromAPI extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "Going to request now!");
            //Retrieve information about the current job | GET /api/job
            jsonGetRequest("api/job", new JobResponse(), new JobErrorResponse());
        }

    }
    private class JobResponse implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            try {
                if (response.getString("state").contains("Offline") || response.getString("state").contains("Operational")) {
                    mainPrintProgress = new PrintProgress(false);
                    // TODO not main thing, call onchange or return
                } else {
                    double completion = response.getDouble("completion");
                    int printTime = response.getInt("printTime");
                    int printTimeLeft = response.getInt("printTimeLeft");
                    mainPrintProgress = new PrintProgress(completion, printTime, printTimeLeft);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private class JobErrorResponse implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            updateConnection();
        }
    }
}
