package com.jptechgenius.notevault.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;

/**
 * Utility methods for file operations, storage calculation, and PDF validation.
 */
public final class FileUtils {

    private FileUtils() {}

    /**
     * Format a file size in bytes to a human-readable string.
     * e.g. 2457600 → "2.4 MB", 348000 → "340 KB"
     */
    public static String formatSize(long bytes) {
        if (bytes <= 0) return "PDF";
        if (bytes < 1024) return bytes + " B";

        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format(Locale.getDefault(), "%.1f KB", kb);

        double mb = kb / 1024.0;
        if (mb < 1024) return String.format(Locale.getDefault(), "%.1f MB", mb);

        double gb = mb / 1024.0;
        return String.format(Locale.getDefault(), "%.2f GB", gb);
    }

    /**
     * Format download count to short form.
     * e.g. 1234 → "1.2k", 456 → "456"
     */
    public static String formatDownloadCount(int count) {
        if (count < 1000) return String.valueOf(count);
        return String.format(Locale.getDefault(), "%.1fk", count / 1000.0);
    }

    /**
     * Get file extension from a path or URL.
     */
    public static String getFileExtension(String path) {
        if (path == null || path.isEmpty()) return "";
        int lastDot = path.lastIndexOf('.');
        if (lastDot == -1 || lastDot == path.length() - 1) return "";
        return path.substring(lastDot + 1).toLowerCase(Locale.getDefault());
    }

    /**
     * Check if a local file exists and is non-empty at the given path.
     */
    public static boolean fileExists(String path) {
        if (path == null || path.isEmpty()) return false;
        File file = new File(path);
        return file.exists() && file.isFile() && file.length() > 0;
    }

    /**
     * Verify that a file begins with the standard "%PDF-" magic signature.
     */
    public static boolean isValidPdfFile(File file) {
        if (file == null || !file.exists() || !file.isFile() || file.length() < 5) {
            return false;
        }
        byte[] header = new byte[5];
        try (FileInputStream fis = new FileInputStream(file)) {
            int read = fis.read(header);
            if (read < 5) return false;
            return header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F' && header[4] == '-';
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calculate total bytes used by downloaded PDFs in the NoteVault directory.
     */
    public static long getTotalDownloadedBytes(Context context) {
        File dir = ensureDownloadDir(context);
        File[] files = dir.listFiles();
        if (files == null) return 0L;
        long total = 0L;
        for (File f : files) {
            if (f.isFile()) {
                total += f.length();
            }
        }
        return total;
    }

    /**
     * Count how many offline PDF files exist in the NoteVault download directory.
     */
    public static int getDownloadedFileCount(Context context) {
        File dir = ensureDownloadDir(context);
        File[] files = dir.listFiles();
        if (files == null) return 0;
        int count = 0;
        for (File f : files) {
            if (f.isFile() && f.length() > 0) count++;
        }
        return count;
    }

    /**
     * Get a shareable URI for a file using FileProvider (required on Android 7+).
     */
    public static Uri getShareableUri(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    file
            );
        } else {
            return Uri.fromFile(file);
        }
    }

    /**
     * Build the local PDF file path for a given note ID.
     */
    public static String buildLocalPath(Context context, int noteId) {
        File dir = new File(context.getExternalFilesDir(null), Constants.DOWNLOAD_DIR);
        return dir.getAbsolutePath() + "/note_" + noteId + ".pdf";
    }

    /**
     * Ensure the NoteVault download directory exists.
     */
    public static File ensureDownloadDir(Context context) {
        File dir = new File(context.getExternalFilesDir(null), Constants.DOWNLOAD_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
