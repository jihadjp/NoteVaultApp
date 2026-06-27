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
    private String subject;         // Keeping for compatibility (maps to courseName)
    private String courseCode;
    private String departmentName;
    private String uploadedBy;      // Maps to userName
    private String uploadedAt;      // Maps to createdAt
    private String serverUrl;       // Maps to fileUrl
    private String localFilePath;
    private boolean isDownloaded;
    private long fileSize;
    private int downloadCount;      // Maps to viewCount
    private int likeCount;
    private int dislikeCount;
    private String thumbnailUrl;
    private long savedAt;

    // ─── Getters ────────────────────────────────────────────────────────────────

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSubject() { return subject; }
    public String getCourseCode() { return courseCode; }
    public String getDepartmentName() { return departmentName; }
    public String getUploadedBy() { return uploadedBy; }
    public String getUploadedAt() { return uploadedAt; }
    public String getServerUrl() { return serverUrl; }
    public String getLocalFilePath() { return localFilePath; }
    public boolean isDownloaded() { return isDownloaded; }
    public long getFileSize() { return fileSize; }
    public int getDownloadCount() { return downloadCount; }
    public int getLikeCount() { return likeCount; }
    public int getDislikeCount() { return dislikeCount; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public long getSavedAt() { return savedAt; }

    // ─── Setters ────────────────────────────────────────────────────────────────

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
    public void setLocalFilePath(String localFilePath) { this.localFilePath = localFilePath; }
    public void setDownloaded(boolean downloaded) { isDownloaded = downloaded; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public void setDislikeCount(int dislikeCount) { this.dislikeCount = dislikeCount; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setSavedAt(long savedAt) { this.savedAt = savedAt; }
}
