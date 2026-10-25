package com.jptechgenius.notevault.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity representing a course saved in the student's personal "My Courses" workspace.
 */
@Entity(tableName = "my_courses")
public class MyCourseEntity {

    @PrimaryKey
    @NonNull
    private String courseCode = "";

    private String courseTitle;
    private String departmentName;
    private String universityName;
    private String semesterName;
    private float creditHours;
    private long addedAt;

    public MyCourseEntity() {}

    public MyCourseEntity(@NonNull String courseCode, String courseTitle, String departmentName,
                          String universityName, String semesterName, float creditHours, long addedAt) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.departmentName = departmentName;
        this.universityName = universityName;
        this.semesterName = semesterName;
        this.creditHours = creditHours;
        this.addedAt = addedAt;
    }

    @NonNull
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(@NonNull String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getSemesterName() { return semesterName; }
    public void setSemesterName(String semesterName) { this.semesterName = semesterName; }

    public float getCreditHours() { return creditHours; }
    public void setCreditHours(float creditHours) { this.creditHours = creditHours; }

    public long getAddedAt() { return addedAt; }
    public void setAddedAt(long addedAt) { this.addedAt = addedAt; }
}
