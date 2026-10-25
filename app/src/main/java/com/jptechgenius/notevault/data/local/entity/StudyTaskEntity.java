package com.jptechgenius.notevault.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.concurrent.TimeUnit;

/**
 * Room entity representing a student-configured exam date, assignment deadline, or study task.
 * Powers the Study Planner and Exam Countdown without fabricating university schedules.
 */
@Entity(tableName = "study_tasks")
public class StudyTaskEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String courseCode;
    private String taskType; // "EXAM", "ASSIGNMENT", "STUDY_TASK"
    private long targetDateMillis;
    private String notes;
    private boolean isCompleted;
    private long createdAt;

    public StudyTaskEntity() {}

    public StudyTaskEntity(String title, String courseCode, String taskType,
                           long targetDateMillis, String notes, boolean isCompleted, long createdAt) {
        this.title = title;
        this.courseCode = courseCode;
        this.taskType = taskType;
        this.targetDateMillis = targetDateMillis;
        this.notes = notes;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }

    public long getTargetDateMillis() { return targetDateMillis; }
    public void setTargetDateMillis(long targetDateMillis) { this.targetDateMillis = targetDateMillis; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /**
     * Calculates days remaining from the given timestamp (e.g. System.currentTimeMillis()).
     */
    public long getDaysRemaining(long nowMillis) {
        long diff = targetDateMillis - nowMillis;
        if (diff <= 0) return 0;
        return TimeUnit.MILLISECONDS.toDays(diff);
    }
}
