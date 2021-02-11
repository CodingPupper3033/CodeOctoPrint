package com.codeoctoprint.APIConnections.APIKey;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.codeoctoprint.APIConnections.ConnectionToAPI;
import com.codeoctoprint.Useful.SettingsReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class APIRequestAPIKey extends ConnectionToAPI {
    SettingsReader settings;

    ArrayList<APIKeyGetterListener> apiKeyGetterListeners;

    public APIRequestAPIKey(Context context, SettingsReader settings) {
        super(context, settings, false);
        apiKeyGetterListeners = new ArrayList<APIKeyGetterListener>();
        this.settings = settings;
    }

    public void addKeyGetterListener(APIKeyGetterListener apiKeyGetterListener) {
        apiKeyGetterListeners.add(apiKeyGetterListener);
    }

    public void obtainAPIKey() {
        //Probe for workflow support | GET /plugin/appkeys/probe
        stringGetRequestNoAPIKey("plugin/appkeys/probe", new ProbeWorkflowResponse(), new ReceivedVolleyError());
    }

    private class ReceivedVolleyError implements Response.ErrorListener {
        public void onErrorResponse(VolleyError error) {
            // Pass down the error
            for (int i = 0; i < apiKeyGetterListeners.size(); i++) {
                apiKeyGetterListeners.get(i).onError(error);
            }
        }
    }

    private class ReceivedVolleyErrorPoll implements Response.ErrorListener {
        Timer timer;
        public ReceivedVolleyErrorPoll(Timer timer) {
            this.timer = timer;
        }

        public void onErrorResponse(VolleyError error) {
            timer.cancel();
            // Pass down the error
            for (int i = 0; i < apiKeyGetterListeners.size(); i++) {
                apiKeyGetterListeners.get(i).onDeniedKey(error);
            }
        }
    }

    private class ProbeWorkflowResponse implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {
            // Suggest probing workflow worked
            for (int i = 0; i < apiKeyGetterListeners.size(); i++) {
                apiKeyGetterListeners.get(i).onSuccessfulProbe(getURL());
            }

            // App Name (CodeOctoPrint)
            Map<String, String> params = new HashMap<String, String>();
            params.put("app", "CodeOctoPrint");

            jsonRequestNoAPIKey("plugin/appkeys/request", Request.Method.POST, new JSONObject(params), new AuthResponse(), new ReceivedVolleyError());
        }
    }

    private class AuthResponse implements Response.Listener<JSONObject> {
        @Override
        public void onResponse(JSONObject response) {
            // Suggest auth request worked
            for (int i = 0; i < apiKeyGetterListeners.size(); i++) {
                apiKeyGetterListeners.get(i).onSuccessfulAuth(getURL());
            }

            try {
                // Save Response | Saves in settings.json for some reason
                JSONObject settingsJSON = settings.getSettingsJSON();
                settingsJSON.put("temp_app_token", response.get("app_token"));
                settings.setSettingsJSON(settingsJSON);

                // Start the timer for polling
                Timer timer = new Timer();
                timer.scheduleAtFixedRate(new PollResponseTimerTask(timer), 1000, 1000);


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class PollResponseTimerTask extends TimerTask {
        private final Timer timer;
        public PollResponseTimerTask(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void run() {
            try {
                JSONObject settingsJSON = settings.getSettingsJSON();
                String appToken = settingsJSON.getString("temp_app_token");
                jsonGetRequestNoAPIKey("plugin/appkeys/request/" + appToken, new PollResponse(timer), new ReceivedVolleyErrorPoll(timer));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class PollResponse implements Response.Listener<JSONObject> {
        Timer timer;

        public PollResponse(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void onResponse(JSONObject response) {
            for (int i = 0; i < apiKeyGetterListeners.size(); i++) {
                apiKeyGetterListeners.get(i).onPoll(response);
            }
            try{
                if (response.has("message")) {
                    if (response.getString("message").equals("Awaiting decision")) {
                        // Still Awaiting decision
                    } else {
                        timer.cancel();
                        new ReceivedVolleyError().onErrorResponse(new VolleyError(response.getString("message")));
                    }
                } else if (response.has("api_key")) {
                    // Give api key
                    for (int i = 0; i < apiKeyGetterListeners.size(); i++) {
                        apiKeyGetterListeners.get(i).onObtainKey(response.getString("api_key"));
                    }
                    timer.cancel();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
