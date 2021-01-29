package com.codeoctoprint.APIRequests.Progress;

import java.util.ArrayList;

public class APIRequestJob {
    // Listeners
    private final ArrayList<PrintProgressListener> PrintProgressListeners;

    public APIRequestJob() {
        PrintProgressListeners = new ArrayList<PrintProgressListener>();
    }

    public void addPrintProgressListener(PrintProgressListener listener) {
        PrintProgressListeners.add(listener);
    }
}
