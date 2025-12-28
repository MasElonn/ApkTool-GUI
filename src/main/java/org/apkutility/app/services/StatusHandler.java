package org.apkutility.app.services;

public interface StatusHandler {
    void setStatus(String status);
    void setProgressVisible(boolean visible);
    void setProgress(double progress);
}
