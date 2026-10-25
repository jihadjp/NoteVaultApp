package com.jptechgenius.notevault;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.jptechgenius.notevault.utils.AcademicPreferenceManager;

/**
 * Application class for NoteVault DIU.
 * Initializes notification channels and restores saved theme and locale preferences.
 */
public class NoteVaultApp extends Application {

    public static final String DOWNLOAD_CHANNEL_ID = "download_channel";
    public static final String PLANNER_CHANNEL_ID = "planner_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        AcademicPreferenceManager preferenceManager = new AcademicPreferenceManager(this);
        preferenceManager.applySavedThemeAndLocale(this);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager == null) return;

            NotificationChannel downloadChannel = new NotificationChannel(
                    DOWNLOAD_CHANNEL_ID,
                    "PDF Downloads",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            downloadChannel.setDescription("Notifications for offline PDF downloads and status");
            manager.createNotificationChannel(downloadChannel);

            NotificationChannel plannerChannel = new NotificationChannel(
                    PLANNER_CHANNEL_ID,
                    "Study Planner & Exam Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            plannerChannel.setDescription("Reminders for personal study tasks and configured exams");
            manager.createNotificationChannel(plannerChannel);
        }
    }
}
