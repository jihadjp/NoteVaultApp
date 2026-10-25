package com.jptechgenius.notevault.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
     * Generate a multi-page academic PDF document using Android's native PdfDocument API
     * when offline demo resources are opened without a local backend server.
     */
    public static File generateAcademicPdfFallback(
            Context context,
            String fileName,
            String title,
            String courseCode,
            String department,
            String description,
            int pageCount
    ) {
        File dir = ensureDownloadDir(context);
        File outFile = new File(dir, fileName);
        if (isValidPdfFile(outFile)) {
            return outFile;
        }

        int totalPages = Math.max(4, Math.min(12, pageCount > 0 ? pageCount : 6));
        PdfDocument document = new PdfDocument();
        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        String safeTitle = title != null ? title : "DIU Academic Lecture Resource";
        String safeCourse = courseCode != null ? courseCode : "SWE211";
        String safeDept = department != null ? department : "SWE";
        String safeDesc = description != null ? description
                : "Verified Daffodil International University (DIU) course study material.";

        try {
            for (int i = 1; i <= totalPages; i++) {
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, i).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                canvas.drawColor(Color.WHITE);

                // Top Emerald Banner
                headerPaint.setColor(Color.parseColor("#086B61"));
                canvas.drawRect(0, 0, 595, 86, headerPaint);

                titlePaint.setColor(Color.WHITE);
                titlePaint.setTextSize(13f);
                titlePaint.setFakeBoldText(true);
                canvas.drawText("DAFFODIL INTERNATIONAL UNIVERSITY (DIU) • NOTEVAULT", 36, 36, titlePaint);

                titlePaint.setTextSize(11f);
                titlePaint.setFakeBoldText(false);
                canvas.drawText("Department of " + safeDept + " • Course: " + safeCourse + " • Page " + i + " of " + totalPages, 36, 60, titlePaint);

                // Document Title
                titlePaint.setColor(Color.parseColor("#17211F"));
                titlePaint.setTextSize(17f);
                titlePaint.setFakeBoldText(true);
                String shortTitle = safeTitle.length() > 54 ? safeTitle.substring(0, 54) + "..." : safeTitle;
                canvas.drawText(shortTitle, 36, 130, titlePaint);

                // Section Subtitle
                bodyPaint.setColor(Color.parseColor("#086B61"));
                bodyPaint.setTextSize(13f);
                bodyPaint.setFakeBoldText(true);
                canvas.drawText("Section " + i + ".0 — Core Concepts, Lecture Summary & Worked Examples", 36, 162, bodyPaint);

                // Summary Box
                boxPaint.setColor(Color.parseColor("#EEF3F2"));
                canvas.drawRect(36, 180, 559, 265, boxPaint);

                bodyPaint.setColor(Color.parseColor("#17211F"));
                bodyPaint.setTextSize(11.5f);
                bodyPaint.setFakeBoldText(false);
                String descLine1 = safeDesc.length() > 75 ? safeDesc.substring(0, 75) : safeDesc;
                String descLine2 = safeDesc.length() > 75 ? safeDesc.substring(75, Math.min(safeDesc.length(), 150)) : "Outcome-Based Education (OBE) aligned lecture notes and exam preparation guide.";
                canvas.drawText(descLine1, 48, 210, bodyPaint);
                canvas.drawText(descLine2, 48, 232, bodyPaint);
                canvas.drawText("Course Code: " + safeCourse + "   |   Verified DIU Academic Repository", 48, 252, bodyPaint);

                // Structured Lecture Points
                int y = 305;
                String[] topics = new String[]{
                        "1. Theoretical Foundation & Formal Definitions (" + safeCourse + " Topic " + i + ".1)",
                        "   • Key properties, invariants, and mathematical/algorithmic formulation.",
                        "   • Step-by-step derivation used in DIU Midterm and Final examinations.",
                        "2. Practical Implementation & Architecture (" + safeCourse + " Topic " + i + ".2)",
                        "   • Standard schema / algorithm design pattern with complexity analysis.",
                        "   • Common pitfalls and optimization strategies for lab assessments.",
                        "3. Past Examination Question & Solution Walkthrough (Page " + i + ")",
                        "   • Q" + i + ": Analyze the core model and demonstrate normalization / trace execution.",
                        "   • Solution: Apply the canonical 3-step verification rule shown below."
                };
                for (String line : topics) {
                    bodyPaint.setFakeBoldText(!line.startsWith("   "));
                    bodyPaint.setColor(line.startsWith("   ") ? Color.parseColor("#4B635E") : Color.parseColor("#17211F"));
                    canvas.drawText(line, 36, y, bodyPaint);
                    y += 28;
                }

                // Diagram / Formula Reference Box
                boxPaint.setColor(Color.parseColor("#ECFDF5"));
                canvas.drawRect(36, 575, 559, 730, boxPaint);
                bodyPaint.setFakeBoldText(true);
                bodyPaint.setColor(Color.parseColor("#06463F"));
                canvas.drawText("Worked Reference Block — " + safeCourse + " (Sheet #" + i + ")", 50, 605, bodyPaint);
                bodyPaint.setFakeBoldText(false);
                bodyPaint.setColor(Color.parseColor("#17211F"));
                canvas.drawText("• Input Specification  -->  Validation & Normalization  -->  Verified Output", 50, 635, bodyPaint);
                canvas.drawText("• Time / Space Bound: O(N log N) optimal | ACID / BCNF compliant", 50, 662, bodyPaint);
                canvas.drawText("• Key Takeaway: Review this diagram before the " + safeCourse + " assessment.", 50, 689, bodyPaint);

                // Footer
                bodyPaint.setColor(Color.parseColor("#7B938E"));
                bodyPaint.setTextSize(10f);
                canvas.drawText("NoteVault DIU Offline PDF Engine • Page " + i + " of " + totalPages, 36, 805, bodyPaint);

                document.finishPage(page);
            }

            try (FileOutputStream fos = new FileOutputStream(outFile)) {
                document.writeTo(fos);
                fos.flush();
            }
        } catch (Exception ignored) {
        } finally {
            document.close();
        }
        return outFile;
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
