package com.codeoctoprint.APIRequests.Progress;

public class PrintProgress {
    double completion;
    int printTime;
    int printTimeLeft;

    public PrintProgress(double completion, int printTime, int printTimeLeft) {
        this.completion = completion;
        this.printTime = printTime;
        this.printTimeLeft = printTimeLeft;
    }

    public double getCompletion() {
        return completion;
    }

    public int getPrintTime() {
        return printTime;
    }

    public int getPrintTimeLeft() {
        return printTimeLeft;
    }
}
