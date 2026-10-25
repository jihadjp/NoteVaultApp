package com.jptechgenius.notevault.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.jptechgenius.notevault.data.local.entity.StudyTaskEntity;

import java.util.List;

/**
 * DAO for managing personal study tasks and configured exam countdown dates.
 */
@Dao
public interface StudyTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTask(StudyTaskEntity task);

    @Query("SELECT * FROM study_tasks ORDER BY isCompleted ASC, targetDateMillis ASC")
    LiveData<List<StudyTaskEntity>> getAllTasks();

    @Query("SELECT * FROM study_tasks WHERE isCompleted = 0 ORDER BY targetDateMillis ASC")
    LiveData<List<StudyTaskEntity>> getUpcomingActiveTasks();

    @Query("UPDATE study_tasks SET isCompleted = :completed WHERE id = :taskId")
    void setTaskCompleted(int taskId, boolean completed);

    @Query("DELETE FROM study_tasks WHERE id = :taskId")
    void deleteTask(int taskId);
}
