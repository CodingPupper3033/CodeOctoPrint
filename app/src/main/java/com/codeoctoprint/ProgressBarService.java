package com.codeoctoprint;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.codeoctoprint.MainActivity.CHANNEL_PROGRESSBAR_ID;
import static com.codeoctoprint.MainActivity.NOTIFICATION_PROGRESSBAR_ID;

public class ProgressBarService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showNotificationIndeterminate();

        return START_STICKY;
    }

    public void showNotificationIndeterminate() {
        Intent notificationIntent = new Intent(this, MainActivity.class);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_PROGRESSBAR_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle("Time left: To be determined")
                .setColor(16737792) // Orange
                .setProgress(0, 0,true);

        startForeground(NOTIFICATION_PROGRESSBAR_ID, notificationBuilder.build());
    }


}
