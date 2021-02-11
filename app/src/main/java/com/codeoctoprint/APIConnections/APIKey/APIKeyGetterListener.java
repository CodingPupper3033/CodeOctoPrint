package com.codeoctoprint.APIConnections.APIKey;

import com.android.volley.VolleyError;

public interface APIKeyGetterListener {
    void onObtainKey(String apiKey);
    void onSuccessfulProbe();
    void onError(VolleyError error);
}
