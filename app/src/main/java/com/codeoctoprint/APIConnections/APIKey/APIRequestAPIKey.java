package com.codeoctoprint.APIConnections.APIKey;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.codeoctoprint.APIConnections.ConnectionToAPI;
import com.codeoctoprint.Useful.SettingsReader;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class APIRequestAPIKey extends ConnectionToAPI {
    ArrayList<APIKeyGetterListener> apiKeyGetterListeners;

    public APIRequestAPIKey(Context context, SettingsReader settings) {
        super(context, settings, false);
        apiKeyGetterListeners = new ArrayList<APIKeyGetterListener>();
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

    private class ProbeWorkflowResponse implements Response.Listener<String> {
        @Override
        public void onResponse(String response) {
            // Suggest probing workflow worked
            for (int i = 0; i < apiKeyGetterListeners.size(); i++) {
                apiKeyGetterListeners.get(i).onSuccessfulProbe();
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
                apiKeyGetterListeners.get(i).onSuccessfulAuth();
            }
        }
    }
}
