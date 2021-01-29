package com.codeoctoprint.APIRequests.Progress;

import com.codeoctoprint.ConnectionToAPI;

public interface PrintProgressListener extends ConnectionToAPI {
    void update(PrintProgress printProgress);
}
