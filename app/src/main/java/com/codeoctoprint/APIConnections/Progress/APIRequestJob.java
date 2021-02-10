package com.codeoctoprint.APIConnections.Progress;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.codeoctoprint.APIConnections.ConnectionToAPI;
import com.codeoctoprint.Useful.SettingsReader;
import com.codeoctoprint.Useful.URLCleanser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class APIRequestJob extends ConnectionToAPI {
    // Settings
    SettingsReader settings;
        int MAX_ERRORS_BEFORE_DISCONNECT;

    // Timers
    Timer requestFromAPITimer;

    // Listeners
    private final ArrayList<PrintProgressListener> PrintProgressListeners;

    // TAG
    private final String TAG = "API Request Job";

    // Print Progress
    private PrintProgress mainPrintProgress;

    // Context
    private Context context;

    public APIRequestJob(Context context, SettingsReader settings) {
        super(context, settings);
        this.context = context;
        PrintProgressListeners = new ArrayList<PrintProgressListener>();

        this.settings = settings;
        mainPrintProgress = new PrintProgress();


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
        PrintProgressListeners.add(listener);
    }

    private class RequestFromAPI extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "Going to request now!");
            // Request Queue
            RequestQueue queue = Volley.newRequestQueue(context);

            // Settings
            JSONObject settingsJSON = null;
            try {
                settingsJSON = settings.getSettingsJSON();

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

                JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,params, new JobResponse(), new JobErrorResponse())
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
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
            // TODO Disconnect (3 times till gone stuff)
            Log.d(TAG, "Bruh");

        }
    }
}
