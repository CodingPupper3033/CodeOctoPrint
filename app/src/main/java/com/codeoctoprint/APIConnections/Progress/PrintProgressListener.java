package com.codeoctoprint.APIConnections.Progress;

public interface PrintProgressListener{
    void printTimeUpdated(PrintProgress printProgress);
    void connectionUpdated(PrintProgress printProgress);
    void stateUpdated(PrintProgress printProgress);
}
