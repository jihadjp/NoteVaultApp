package com.jptechgenius.notevault.data.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * POJO representing an academic resource / note returned from the REST API.
 * Backward-compatible with both the legacy diunotes schema and the upgraded NoteVault API v1.
 */
public class NoteResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName(value = "fileUrl", alternate = {"pdf_url", "file_url"})
    private String fileUrl;

    @SerializedName("description")
    private String description;

    @SerializedName("courseId")
    private int courseId;

    @SerializedName(value = "courseCode", alternate = {"course_code"})
    private String courseCode;

    @SerializedName(value = "courseName", alternate = {"subject", "course_name"})
    private String courseName;

    @SerializedName("departmentId")
    private int departmentId;

    @SerializedName(value = "departmentName", alternate = {"department_name"})
    private String departmentName;

    @SerializedName(value = "universityName", alternate = {"university_name"})
    private String universityName;

    @SerializedName(value = "resourceCategory", alternate = {"resource_category", "category"})
    private String resourceCategory;

    @SerializedName(value = "semesterName", alternate = {"semester_name", "semester"})
    private String semesterName;

    @SerializedName(value = "batchName", alternate = {"batch_name", "batch"})
    private String batchName;

    @SerializedName(value = "userName", alternate = {"uploaded_by", "user_name"})
    private String userName;

    @SerializedName(value = "createdAt", alternate = {"uploaded_at", "created_at"})
    private String createdAt;

    @SerializedName(value = "updatedAt", alternate = {"updated_at"})
    private String updatedAt;

    @SerializedName("userId")
    private int userId;

    @SerializedName("likeCount")
    private int likeCount;

    @SerializedName("dislikeCount")
    private int dislikeCount;

    @SerializedName("likedByCurrentUser")
    private boolean likedByCurrentUser;

    @SerializedName("dislikedByCurrentUser")
    private boolean dislikedByCurrentUser;

    @SerializedName("savedByCurrentUser")
    private boolean savedByCurrentUser;

    @SerializedName(value = "viewCount", alternate = {"download_count", "view_count"})
    private int viewCount;

    @SerializedName(value = "fileSize", alternate = {"file_size"})
    private long fileSize;

    @SerializedName("status")
    private String status;

    @SerializedName(value = "isVerified", alternate = {"is_verified"})
    private boolean isVerified;

    @SerializedName(value = "ratingAvg", alternate = {"rating_avg"})
    private float ratingAvg;

    @SerializedName(value = "ratingCount", alternate = {"rating_count"})
    private int ratingCount;

    @SerializedName("approvedAt")
    private String approvedAt;

    @SerializedName(value = "thumbnail_url", alternate = {"thumbnailUrl"})
    private String thumbnailUrl;

    // ─── Getters ────────────────────────────────────────────────────────────────

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getFileUrl() { return fileUrl; }
    public String getDescription() { return description; }
    public int getCourseId() { return courseId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseName() { return courseName; }
    public int getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public String getUniversityName() { return universityName; }
    public String getResourceCategory() { return resourceCategory; }
    public String getSemesterName() { return semesterName; }
    public String getBatchName() { return batchName; }
    public String getUserName() { return userName; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public int getUserId() { return userId; }
    public int getLikeCount() { return likeCount; }
    public int getDislikeCount() { return dislikeCount; }
    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public boolean isDislikedByCurrentUser() { return dislikedByCurrentUser; }
    public boolean isSavedByCurrentUser() { return savedByCurrentUser; }
    public int getViewCount() { return viewCount; }
    public long getFileSize() { return fileSize; }
    public String getStatus() { return status; }
    public boolean isVerified() { return isVerified; }
    public float getRatingAvg() { return ratingAvg; }
    public int getRatingCount() { return ratingCount; }
    public String getApprovedAt() { return approvedAt; }
    public String getThumbnailUrl() { return thumbnailUrl; }

    // ─── Setters ────────────────────────────────────────────────────────────────

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setCourseId(int courseId) { this.courseId = courseId; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }
    public void setResourceCategory(String resourceCategory) { this.resourceCategory = resourceCategory; }
    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public void setDislikeCount(int dislikeCount) { this.dislikeCount = dislikeCount; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) { this.likedByCurrentUser = likedByCurrentUser; }
    public void setDislikedByCurrentUser(boolean dislikedByCurrentUser) { this.dislikedByCurrentUser = dislikedByCurrentUser; }
    public void setSavedByCurrentUser(boolean savedByCurrentUser) { this.savedByCurrentUser = savedByCurrentUser; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public void setStatus(String status) { this.status = status; }
    public void setVerified(boolean verified) { isVerified = verified; }
    public void setRatingAvg(float ratingAvg) { this.ratingAvg = ratingAvg; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    public void setApprovedAt(String approvedAt) { this.approvedAt = approvedAt; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
}
