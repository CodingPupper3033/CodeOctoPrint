package com.codeoctoprint.APIConnections.APIKey;

import com.android.volley.VolleyError;

public interface APIKeyGetterListener {
    void onObtainKey(String apiKey);
    void onSuccessfulProbe();
    void onSuccessfulAuth();
    void onError(VolleyError error);
}