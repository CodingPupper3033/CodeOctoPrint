package com.codeoctoprint.APIConnections;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.codeoctoprint.Useful.SettingsReader;
import com.codeoctoprint.Useful.URLCleanser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConnectionToAPI {
    private final Context context;
    private final SettingsReader settings;
    private int failedFindingApi = 0;

    private final ArrayList<ConnectionStatus> connectionStatusListeners;

    boolean connected = false;
    public ConnectionToAPI(Context context, SettingsReader settings) {
        this.settings = settings;
        this.context = context;
        connectionStatusListeners = new ArrayList<ConnectionStatus>();

        updateConnection();
    }

    public ConnectionToAPI(Context context, SettingsReader settings, boolean connectOnStart) {
        this.settings = settings;
        this.context = context;
        connectionStatusListeners = new ArrayList<ConnectionStatus>();

        if (connectOnStart) updateConnection();
    }

    public ConnectionToAPI(Context context, SettingsReader settings, ConnectionStatus connectionStatus) {
        this.settings = settings;
        this.context = context;
        connectionStatusListeners = new ArrayList<ConnectionStatus>();
        connectionStatusListeners.add(connectionStatus);

        updateConnection();
    }

    public void addConnectionStatus(ConnectionStatus listener) {
        connectionStatusListeners.add(listener);
    }

    public void jsonGetRequest(String path, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {

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
            url = cleaner.combineURL(url, path);

            JSONObject params = new JSONObject();
            params.put("X-Api-Key", apiKey);

            JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url,params, listener, errorListener)
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

    public void updateConnection() {
        Log.d("TAG", "updateConnection: " + failedFindingApi);
        jsonGetRequest("api/version", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                connected = true;
                for (int i = 0; i < connectionStatusListeners.size(); i++) {
                    connectionStatusListeners.get(i).onConnect();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("TAG", "updateConnection: " + "errored");
                failedFindingApi++;
                if (failedFindingApi >= 3) {
                    connected = false;
                    for (int i = 0; i < connectionStatusListeners.size(); i++) {
                        connectionStatusListeners.get(i).onDisconnect(error);
                    }
                } else {
                    updateConnection();
                }
            }
        });
    }
}
