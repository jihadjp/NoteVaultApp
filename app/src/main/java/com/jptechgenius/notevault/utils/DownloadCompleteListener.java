package com.jptechgenius.notevault.utils;

/**
 * Callback interface for PDF download completion events.
 */
public interface DownloadCompleteListener {
    /**
     * Called when the PDF download has completed successfully.
     *
     * @param localFilePath The absolute path to the downloaded file on the device.
     */
    void onComplete(String localFilePath);

    /**
     * Called when the PDF download fails.
     *
     * @param reason A human-readable description of the failure.
     */
    void onFailure(String reason);
}
