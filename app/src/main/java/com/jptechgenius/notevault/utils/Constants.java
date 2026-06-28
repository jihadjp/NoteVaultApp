package com.jptechgenius.notevault.utils;

/**
 * Application-wide constants.
 */
public final class Constants {

    private Constants() {}

    // API - Pointing to your NoteVault backend (which now uses diunotes schema)
    public static final String BASE_URL = "https://notevault.jihadjp.com/";

    // Database
    public static final String DB_NAME = "notevault_db";

    // SharedPreferences
    public static final String PREFS_NAME = "notevault_prefs";
    public static final String PREF_LAST_SYNC = "last_sync_timestamp";

    // Download directory name
    public static final String DOWNLOAD_DIR = "NoteVault";

    // Intent extras
    public static final String EXTRA_NOTE_ID = "extra_note_id";
    public static final String EXTRA_NOTE_TITLE = "extra_note_title";
    public static final String EXTRA_PDF_URL = "extra_pdf_url";

    // Notification
    public static final String DOWNLOAD_CHANNEL_ID = "download_channel";

    // File
    public static final String PDF_MIME_TYPE = "application/pdf";

    // Splash delay (ms)
    public static final long SPLASH_DELAY_MS = 2000L;

    // Search debounce delay (ms)
    public static final long SEARCH_DEBOUNCE_MS = 400L;
}
