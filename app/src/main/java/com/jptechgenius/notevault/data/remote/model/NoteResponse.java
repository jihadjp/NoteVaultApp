package com.jptechgenius.notevault.data.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * POJO representing a note returned from the REST API.
 * Updated to match the diunotes.com API schema.
 */
public class NoteResponse {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("fileUrl")
    private String fileUrl;

    @SerializedName("description")
    private String description;

    @SerializedName("courseId")
    private int courseId;

    @SerializedName("courseCode")
    private String courseCode;

    @SerializedName("courseName")
    private String courseName;

    @SerializedName("departmentId")
    private int departmentId;

    @SerializedName("departmentName")
    private String departmentName;

    @SerializedName("userName")
    private String userName;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
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

    @SerializedName("viewCount")
    private int viewCount;

    @SerializedName("status")
    private String status;

    @SerializedName("approvedAt")
    private String approvedAt;

    @SerializedName("thumbnail_url") // Keep for backward compatibility or if needed later
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
    public String getStatus() { return status; }
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
    public void setStatus(String status) { this.status = status; }
    public void setApprovedAt(String approvedAt) { this.approvedAt = approvedAt; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
}
