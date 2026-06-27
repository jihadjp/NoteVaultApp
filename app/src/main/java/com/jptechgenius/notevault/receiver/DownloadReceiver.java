package com.jptechgenius.notevault.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * System-level BroadcastReceiver for Android DownloadManager completion events.
 * Inline receivers registered in NoteRepository handle per-download callbacks;
 * this manifest-registered receiver provides a fallback notification.
 */
public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadId == -1) return;

            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm == null) return;

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            android.database.Cursor cursor = dm.query(query);

            if (cursor != null && cursor.moveToFirst()) {
                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int status = cursor.getInt(statusIndex);

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    // Individual download callbacks in NoteRepository handle Room updates.
                    // This receiver just ensures a notification is visible.
                } else if (status == DownloadManager.STATUS_FAILED) {
                    Toast.makeText(context, "PDF download failed.", Toast.LENGTH_SHORT).show();
                }
                cursor.close();
            }
        }
    }
}
