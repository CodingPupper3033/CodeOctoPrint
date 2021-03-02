package com.codeoctoprint.APIConnections.Progress;

import java.nio.charset.MalformedInputException;

public class PrintProgress {

    double completion;
    int printTime;
    int printTimeLeft;
    boolean connected;
    String status;

    public PrintProgress(double completion, int printTime, int printTimeLeft) {
        this.completion = completion;
        this.printTime = printTime;
        this.printTimeLeft = printTimeLeft;
        this.status = "";
    }

    public PrintProgress(String status) {
        this.status = status;
        this.connected = true;
    }

    public PrintProgress() {
        connected = false;
        status = "";
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean setStatus(String status) throws MalformedInputException {
        switch (status) {
            case ("Operational"):
            case("Printing"):
            case("Pausing"):
            case("Paused"):
            case("Cancelling"):
            case("Error"):
            case("Offline"):
                if (!this.status.equals(status)) {
                    this.status = status;
                    return true;
                }
                return false;
        }

        throw new MalformedInputException(status.length());
    }

    public boolean isConnected() {
        return connected;
    }

    public String getStatus() {
        return status;
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

    public void setPrintTime(int printTime, int printTimeLeft) {
        this.printTime = printTime;
        this.printTimeLeft = printTimeLeft;
        this.completion = (double)printTime/(printTime+printTimeLeft);
    }

    public void changePrintTime(int printTime) {
        this.printTime += printTime;
    }
}
