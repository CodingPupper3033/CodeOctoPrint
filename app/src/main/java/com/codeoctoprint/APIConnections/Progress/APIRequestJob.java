package com.codeoctoprint.APIConnections.Progress;

import android.content.Context;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.codeoctoprint.APIConnections.ConnectionStatus;
import com.codeoctoprint.APIConnections.ConnectionToAPI;
import com.codeoctoprint.Useful.SettingsReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class APIRequestJob extends ConnectionToAPI {
    // Settings
    SettingsReader settings;

    // Print Progress
    PrintProgress mainPrintProgress = new PrintProgress();

    // Print Progress Listeners
    ArrayList<PrintProgressListener> printProgressListeners;

    // Timer
    Timer mainTimer;

    public APIRequestJob(Context context, SettingsReader settings) {
        super(context, settings, false);
        this.settings = settings;

        printProgressListeners = new ArrayList<PrintProgressListener>();

        setupConnectionStatus();

        setupTimer();
    }

    public APIRequestJob(Context context, SettingsReader settings, PrintProgressListener printProgressListener) {
        super(context, settings, false);
        this.settings = settings;

        printProgressListeners = new ArrayList<PrintProgressListener>();
        addPrintProgressListener(printProgressListener);

        setupConnectionStatus();

        setupTimer();
    }

    public PrintProgress getPrintProgress() {
        return mainPrintProgress;
    }

    private void setupConnectionStatus() {
        // Connection Status | What to do when it connects / disconnects
        ConnectionStatus connectionStatus = new ConnectionStatus() {
            @Override
            public void onDisconnect(VolleyError error) {
                setConnected(false);
            }

            @Override
            public void onConnect() {
                setConnected(true);
            }
        };
        addConnectionStatus(connectionStatus);
        updateConnection();
    }

    private void setupTimer() {
        // Get the delay between requests
        try {
            long time = settings.getSettingsJSON().getLong("job_request_delay");
            // Start a timer to update the connection every x seconds
            mainTimer = new Timer();
            mainTimer.scheduleAtFixedRate(new requestsTimerTask(), 0, time);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addPrintProgressListener(PrintProgressListener printProgressListener) {
        printProgressListeners.add(printProgressListener);
    }

    private void setConnected(boolean connected) {
        if (connected != mainPrintProgress.isConnected()) {
            mainPrintProgress.setConnected(connected);

            for (PrintProgressListener printProgressListener : printProgressListeners) {
                printProgressListener.connectionUpdated(mainPrintProgress);
            }
        }
    }

    private void eventState() {
        for (PrintProgressListener printProgressListener : printProgressListeners) {
            printProgressListener.stateUpdated(mainPrintProgress);
        }
    }

    // Timer Task for when to requests the api
    private class requestsTimerTask extends TimerTask {
        @Override
        public void run() {
            // Make a JSON Request for the job status
            // Get Current Status | GET /api/job
            jsonGetRequest("api/job", new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        // Status
                        if (mainPrintProgress.setStatus(response.getString("state").split(" ")[0]))
                            eventState();
                    } catch (MalformedInputException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // TODO What to do with the response?
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        }
    }
}
