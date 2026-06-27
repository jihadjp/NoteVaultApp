package com.jptechgenius.notevault.data.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * POJO representing a note returned from the REST API.
 */
public class NoteResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("subject")
    private String subject;

    @SerializedName("uploaded_by")
    private String uploadedBy;

    @SerializedName("uploaded_at")
    private String uploadedAt;

    @SerializedName("pdf_url")
    private String pdfUrl;

    @SerializedName("file_size")
    private long fileSize;

    @SerializedName("download_count")
    private int downloadCount;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    // ─── Getters ────────────────────────────────────────────────────────────────

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSubject() { return subject; }
    public String getUploadedBy() { return uploadedBy; }
    public String getUploadedAt() { return uploadedAt; }
    public String getPdfUrl() { return pdfUrl; }
    public long getFileSize() { return fileSize; }
    public int getDownloadCount() { return downloadCount; }
    public String getThumbnailUrl() { return thumbnailUrl; }

    // ─── Setters ────────────────────────────────────────────────────────────────

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public void setUploadedAt(String uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
}
