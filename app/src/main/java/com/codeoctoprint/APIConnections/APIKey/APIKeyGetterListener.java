package com.codeoctoprint.APIConnections.APIKey;

import com.android.volley.VolleyError;

public interface APIKeyGetterListener {
    void onObtainKey(String apiKey);
    void onSuccessfulProbe(String host);
    void onSuccessfulAuth(String host);
    void onError(VolleyError error);
}