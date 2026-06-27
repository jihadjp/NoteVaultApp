package com.jptechgenius.notevault.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a PDF note stored locally.
 */
@Entity(tableName = "notes")
public class NoteEntity {

    @PrimaryKey
    private int id;

    private String title;
    private String description;
    private String subject;
    private String uploadedBy;
    private String uploadedAt;
    private String serverUrl;       // Remote PDF URL
    private String localFilePath;   // Local saved path after download
    private boolean isDownloaded;   // true if PDF is saved locally
    private long fileSize;          // In bytes
    private int downloadCount;
    private String thumbnailUrl;
    private long savedAt;           // Timestamp when saved to Room

    // ─── Getters ────────────────────────────────────────────────────────────────

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSubject() { return subject; }
    public String getUploadedBy() { return uploadedBy; }
    public String getUploadedAt() { return uploadedAt; }
    public String getServerUrl() { return serverUrl; }
    public String getLocalFilePath() { return localFilePath; }
    public boolean isDownloaded() { return isDownloaded; }
    public long getFileSize() { return fileSize; }
    public int getDownloadCount() { return downloadCount; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public long getSavedAt() { return savedAt; }

    // ─── Setters ────────────────────────────────────────────────────────────────

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
    public void setLocalFilePath(String localFilePath) { this.localFilePath = localFilePath; }
    public void setDownloaded(boolean downloaded) { isDownloaded = downloaded; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setSavedAt(long savedAt) { this.savedAt = savedAt; }
}
