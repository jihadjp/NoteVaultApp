package com.jptechgenius.notevault.data.remote.model;

import com.google.gson.annotations.SerializedName;

/**
 * POJO for University Questions.
 */
public class QuestionResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("department_name")
    private String departmentName;

    @SerializedName("department_short")
    private String departmentShort;

    @SerializedName("course_name")
    private String courseName;

    @SerializedName("semester_name")
    private String semesterName;

    @SerializedName("exam_type")
    private String examType;

    @SerializedName("pdf_link")
    private String pdfLink;

    @SerializedName("submissions_count")
    private int submissionsCount;

    @SerializedName("created_at")
    private String createdAt;

    // Getters
    public int getId() { return id; }
    public String getDepartmentName() { return departmentName; }
    public String getDepartmentShort() { return departmentShort; }
    public String getCourseName() { return courseName; }
    public String getSemesterName() { return semesterName; }
    public String getExamType() { return examType; }
    public String getPdfLink() { return pdfLink; }
    public int getSubmissionsCount() { return submissionsCount; }
    public String getCreatedAt() { return createdAt; }
}
