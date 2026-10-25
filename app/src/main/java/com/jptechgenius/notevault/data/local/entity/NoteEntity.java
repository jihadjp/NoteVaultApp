package com.jptechgenius.notevault.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Locale;

/**
 * Room entity representing an academic resource / PDF note stored locally.
 * Supports offline caching, bookmarking, reading progress, and academic hierarchy metadata.
 */
@Entity(tableName = "notes")
public class NoteEntity {

    @PrimaryKey
    private int id;

    private String title;
    private String description;
    private String subject;         // Maps to courseName
    private String courseCode;
    private String departmentName;
    private String universityName;
    private String resourceCategory;
    private String semesterName;
    private String batchName;
    private String uploadedBy;      // Maps to userName
    private String uploadedAt;      // Maps to createdAt
    private String serverUrl;       // Maps to fileUrl
    private String localFilePath;
    private boolean isDownloaded;
    private boolean isBookmarked;
    private boolean isVerified;
    private String moderationStatus;
    private long fileSize;
    private int downloadCount;      // Maps to viewCount
    private int likeCount;
    private int dislikeCount;
    private float ratingAvg;
    private int ratingCount;
    private String thumbnailUrl;
    private long savedAt;
    private long lastOpenedAt;
    private int lastReadPage;
    private int totalPages;

    // ─── Getters ────────────────────────────────────────────────────────────────

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSubject() { return subject; }
    public String getCourseCode() { return courseCode; }
    public String getDepartmentName() { return departmentName; }
    public String getUniversityName() { return universityName; }
    public String getResourceCategory() { return resourceCategory; }
    public String getSemesterName() { return semesterName; }
    public String getBatchName() { return batchName; }
    public String getUploadedBy() { return uploadedBy; }
    public String getUploadedAt() { return uploadedAt; }
    public String getServerUrl() { return serverUrl; }
    public String getLocalFilePath() { return localFilePath; }
    public boolean isDownloaded() { return isDownloaded; }
    public boolean isBookmarked() { return isBookmarked; }
    public boolean isVerified() { return isVerified; }
    public String getModerationStatus() { return moderationStatus; }
    public long getFileSize() { return fileSize; }
    public int getDownloadCount() { return downloadCount; }
    public int getLikeCount() { return likeCount; }
    public int getDislikeCount() { return dislikeCount; }
    public float getRatingAvg() { return ratingAvg; }
    public int getRatingCount() { return ratingCount; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public long getSavedAt() { return savedAt; }
    public long getLastOpenedAt() { return lastOpenedAt; }
    public int getLastReadPage() { return lastReadPage; }
    public int getTotalPages() { return totalPages; }

    // ─── Setters ────────────────────────────────────────────────────────────────

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }
    public void setResourceCategory(String resourceCategory) { this.resourceCategory = resourceCategory; }
    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
    public void setLocalFilePath(String localFilePath) { this.localFilePath = localFilePath; }
    public void setDownloaded(boolean downloaded) { isDownloaded = downloaded; }
    public void setBookmarked(boolean bookmarked) { isBookmarked = bookmarked; }
    public void setVerified(boolean verified) { isVerified = verified; }
    public void setModerationStatus(String moderationStatus) { this.moderationStatus = moderationStatus; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public void setDislikeCount(int dislikeCount) { this.dislikeCount = dislikeCount; }
    public void setRatingAvg(float ratingAvg) { this.ratingAvg = ratingAvg; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public void setSavedAt(long savedAt) { this.savedAt = savedAt; }
    public void setLastOpenedAt(long lastOpenedAt) { this.lastOpenedAt = lastOpenedAt; }
    public void setLastReadPage(int lastReadPage) { this.lastReadPage = lastReadPage; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    /**
     * Deterministically resolves the resource category from explicit metadata or title/description keywords.
     */
    public String getResolvedCategory() {
        if (resourceCategory != null && !resourceCategory.trim().isEmpty()) {
            return resourceCategory.trim();
        }
        String combined = ((title != null ? title : "") + " " + (description != null ? description : ""))
                .toLowerCase(Locale.ROOT);
        if (combined.contains("syllabus") || combined.contains("outline")) {
            return "Official Syllabus";
        }
        if (combined.contains("slide") || combined.contains("ppt") || combined.contains("presentation")) {
            return "Lecture Slides";
        }
        if (combined.contains("question") || combined.contains("midterm") || combined.contains("final exam")) {
            return "Question Papers";
        }
        if (combined.contains("suggestion") || combined.contains("shortlist")) {
            return "Suggestions";
        }
        if (combined.contains("lab") || combined.contains("manual") || combined.contains("experiment")) {
            return "Lab Manuals";
        }
        if (combined.contains("assignment") || combined.contains("project")) {
            return "Assignments";
        }
        return "Lecture Notes";
    }
}
