package com.jptechgenius.notevault;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

/**
 * Application class for NoteVault DIU.
 * Initializes app-wide components.
 */
public class NoteVaultApp extends Application {

    public static final String DOWNLOAD_CHANNEL_ID = "download_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel downloadChannel = new NotificationChannel(
                    DOWNLOAD_CHANNEL_ID,
                    "PDF Downloads",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            downloadChannel.setDescription("Notifications for PDF download progress");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(downloadChannel);
            }
        }
    }
}
