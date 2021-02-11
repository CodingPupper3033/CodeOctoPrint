package com.codeoctoprint.APIConnections.APIKey;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface APIKeyGetterListener {
    void onObtainKey(String apiKey);
    void onDeniedKey(VolleyError error);
    void onSuccessfulProbe(String host);
    void onSuccessfulAuth(String host);
    void onPoll(JSONObject response);
    void onError(VolleyError error);
}