package com.jptechgenius.notevault.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.jptechgenius.notevault.data.local.entity.MyCourseEntity;

import java.util.List;

/**
 * DAO for managing the student's personal "My Courses" list.
 */
@Dao
public interface MyCourseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCourse(MyCourseEntity course);

    @Query("SELECT * FROM my_courses ORDER BY addedAt DESC")
    LiveData<List<MyCourseEntity>> getAllMyCourses();

    @Query("DELETE FROM my_courses WHERE courseCode = :courseCode")
    void removeCourse(String courseCode);

    @Query("SELECT COUNT(*) FROM my_courses WHERE courseCode = :courseCode")
    int isCourseSavedSync(String courseCode);
}
