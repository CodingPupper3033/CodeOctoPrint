package com.codeoctoprint.APIConnections.Progress;

public class PrintProgress {
    double completion;
    int printTime;
    int printTimeLeft;
    boolean connected;
    boolean printing;

    public PrintProgress(double completion, int printTime, int printTimeLeft) {
        this.completion = completion;
        this.printTime = printTime;
        this.printTimeLeft = printTimeLeft;
        this.printing = printing;
    }

    public PrintProgress(boolean printing) {
        this.printing = printing;
        this.connected = true;
    }

    public PrintProgress() {
        connected = false;
        printing = false;
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

    public void setPrintTime(int printTime) {
        this.printTime = printTime;
    }

    public void changePrintTime(int printTime) {
        this.printTime += printTime;
    }
}
