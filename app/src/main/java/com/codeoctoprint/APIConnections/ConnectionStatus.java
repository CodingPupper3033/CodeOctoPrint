package com.codeoctoprint.APIConnections;

import com.android.volley.VolleyError;

public interface ConnectionStatus {
    void onDisconnect(VolleyError error);
    void onConnect();
}
