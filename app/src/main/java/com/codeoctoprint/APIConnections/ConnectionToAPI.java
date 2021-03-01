package com.codeoctoprint.APIConnections;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.codeoctoprint.Useful.SettingsReader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ConnectionToAPI {
    private Context context;
    private final SettingsReader settings;
    private RequestQueue queue;

    private int failedFindingApi = 0;

    private final ArrayList<ConnectionStatus> connectionStatusListeners;

    private boolean connectedOnce = false;
    boolean connected;

    /**
     * Updates Connection
     * @param context
     * @param settings
     */

    public ConnectionToAPI(Context context, SettingsReader settings) {
        this.settings = settings;
        this.context = context;
        connectionStatusListeners = new ArrayList<ConnectionStatus>();
        queue = Volley.newRequestQueue(context);

        updateConnection();
    }

    public ConnectionToAPI(Context context, SettingsReader settings, boolean connectOnStart) {
        this.settings = settings;
        this.context = context;
        connectionStatusListeners = new ArrayList<ConnectionStatus>();
        queue = Volley.newRequestQueue(context);

        if (connectOnStart) updateConnection();
    }

    public ConnectionToAPI(Context context, SettingsReader settings, ConnectionStatus connectionStatus) {
        this.settings = settings;
        this.context = context;
        connectionStatusListeners = new ArrayList<ConnectionStatus>();
        connectionStatusListeners.add(connectionStatus);
        queue = Volley.newRequestQueue(context);

        updateConnection();
    }

    public void addConnectionStatus(ConnectionStatus listener) {
        connectionStatusListeners.add(listener);
    }

    public void jsonGetRequest(String path, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JSONObject params = new JSONObject();
        jsonRequest(path, Request.Method.GET, params, listener, errorListener);
    }

    public void jsonGetRequestNoAPIKey(String path, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        JSONObject params = new JSONObject();
        jsonRequestNoAPIKey(path, Request.Method.GET, params, listener, errorListener);
    }

    public void stringGetRequestNoAPIKey(String path, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        stringRequestNoAPIKey(path, Request.Method.GET, listener, errorListener);
    }

    public void jsonRequestNoAPIKey(String path, int type, JSONObject params, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = getURL(path);

        makeRequest(
                new JsonObjectRequest(type, url, params, listener, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        updateConnection();
                        errorListener.onErrorResponse(error);
                    }
                })
        );
    }

    public void stringRequestNoAPIKey(String path, int type, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        String url = getURL(path);

        makeRequest(
                new StringRequest(type, url, listener, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        updateConnection();
                        errorListener.onErrorResponse(error);
                    }
                })
        );
    }

    public void jsonRequest(String path, int type, JSONObject params, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = getURL(path);
        String apiKey = getAPIKey();
        makeRequest(
                new JsonObjectRequest(type, url, params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        updateConnection(true);
                        listener.onResponse(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        updateConnection();
                        errorListener.onErrorResponse(error);
                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("X-Api-Key", apiKey);
                        return params;
                    }
                }
        );
    }

    public void makeRequest(Request request) {
        queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public String getURL(String path) {
        // Settings
        JSONObject settingsJSON = null;
        try {
            settingsJSON = settings.getSettingsJSON();

            // Get Host
            String host = settingsJSON.getString("host");

            // "Clean" URL
            if (!host.isEmpty()) {
                // Make sure it ends with /
                if (host.charAt(host.length()-1) != '/') {
                    host += "/";
                }

                // Check for http or https tag
                if (!host.contains("http://") && !host.contains("https://")) {
                    host = "http://" + host;
                }
            } else {
                host = "http://";
            }

            String url = host + path;

            return url;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getURL() {
        // Settings
        JSONObject settingsJSON = null;
        try {
            settingsJSON = settings.getSettingsJSON();

            // Get Host
            String host = settingsJSON.getString("host");

            // "Clean" URL
            if (!host.isEmpty()) {
                // Make sure it ends with /
                if (host.charAt(host.length()-1) != '/') {
                    host += "/";
                }

                // Check for http or https tag
                if (!host.contains("http://") && !host.contains("https://")) {
                    host = "http://" + host;
                }
            } else {
                host = "http://";
            }

            return host;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAPIKey() {
        // Settings
        JSONObject settingsJSON = null;
        try {
            settingsJSON = settings.getSettingsJSON();
            return settingsJSON.getString("api_key");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateConnection() {
        String url = getURL("api/version");
        String apiKey = getAPIKey();

        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        boolean connectedAtAll = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();

        if (connectedAtAll) {
            JSONObject params = new JSONObject();
            makeRequest(new JsonObjectRequest(Request.Method.GET, url, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    updateConnection(true);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        failedFindingApi++;

                        int mebd = settings.getSettingsJSON().getInt("max_errors_before_disconnect");
                        if (failedFindingApi >= mebd) {
                            updateConnection(false, error);
                        } else {
                            updateConnection();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("X-Api-Key", apiKey);
                    return params;
                }
            });
        } else {
            updateConnection(false, "No Internet Connection");
        }
    }

    public void updateConnection(boolean connectedNow) {
        // If it has ever tried to connect before (if we need to send a message no matter what
        if (!connectedOnce) {
            connectedOnce = true;
            if (connectedNow) {
                connected = false;
                for (int i = 0; i < connectionStatusListeners.size(); i++) {
                    connectionStatusListeners.get(i).onConnect();
                }
            } else {
                for (int i = 0; i < connectionStatusListeners.size(); i++) {
                    connectionStatusListeners.get(i).onDisconnect(new VolleyError("No Message"));
                }
            }
        } else if (connectedNow != connected) {
            if (connectedNow) {
                connected = false;
                for (int i = 0; i < connectionStatusListeners.size(); i++) {
                    connectionStatusListeners.get(i).onConnect();
                }
            } else {
                for (int i = 0; i < connectionStatusListeners.size(); i++) {
                    connectionStatusListeners.get(i).onDisconnect(new VolleyError("No Message"));
                }
            }
        }
    }

    public void updateConnection(boolean connectedNow, String message) {
        // If it has ever tried to connect before (if we need to send a message no matter what
        if (!connectedOnce) {
            connectedOnce = true;
            if (connectedNow) {
                connected = false;
                for (int i = 0; i < connectionStatusListeners.size(); i++) {
                    connectionStatusListeners.get(i).onConnect();
                }
            } else {
                for (int i = 0; i < connectionStatusListeners.size(); i++) {
                    connectionStatusListeners.get(i).onDisconnect(new VolleyError(message));
                }
            }
        } else if (connectedNow != connected) {
            if (connectedNow) {
                connected = false;
                for (int i = 0; i < connectionStatusListeners.size(); i++) {
                    connectionStatusListeners.get(i).onConnect();
                }
            } else {
                for (int i = 0; i < connectionStatusListeners.size(); i++) {
                    connectionStatusListeners.get(i).onDisconnect(new VolleyError("No Message"));
                }
            }
        }
    }

    public void updateConnection(boolean connectedNow, VolleyError error) {
        // If it has ever tried to connect before (if we need to send a message no matter what
        if (!connectedOnce) {
            connectedOnce = true;
            if (connectedNow) {
                connected = false;
                for (int i = 0; i < connectionStatusListeners.size(); i++) {
                    connectionStatusListeners.get(i).onConnect();
                }
            } else {
                for (int i = 0; i < connectionStatusListeners.size(); i++) {
                    connectionStatusListeners.get(i).onDisconnect(new VolleyError("No Message"));
                }
            }
        } else if (connectedNow != connected) {
            if (connectedNow) {
                connected = false;
                for (int i = 0; i < connectionStatusListeners.size(); i++) {
                    connectionStatusListeners.get(i).onConnect();
                }
            } else {
                for (int i = 0; i < connectionStatusListeners.size(); i++) {
                    connectionStatusListeners.get(i).onDisconnect(error);
                }
            }
        }
    }

    public void changeContext(Context context) {
        this.context = context;
        queue.stop();
        queue = Volley.newRequestQueue(context);
    }
}
