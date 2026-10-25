package com.jptechgenius.notevault.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.jptechgenius.notevault.data.local.entity.NoteEntity;

import java.util.List;

/**
 * Data Access Object for NoteEntity.
 * Provides all database operations for the notes table including bookmarks,
 * reading progress, and multi-criteria filtering.
 */
@Dao
public interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(NoteEntity note);

    @Query("SELECT * FROM notes WHERE id = :noteId")
    NoteEntity getNoteByIdSync(int noteId);

    @Delete
    void deleteNote(NoteEntity note);

    @Query("SELECT * FROM notes ORDER BY savedAt DESC")
    LiveData<List<NoteEntity>> getAllNotes();

    @Query("SELECT * FROM notes WHERE isDownloaded = 1 ORDER BY savedAt DESC")
    LiveData<List<NoteEntity>> getDownloadedNotes();

    @Query("SELECT * FROM notes WHERE isBookmarked = 1 ORDER BY savedAt DESC")
    LiveData<List<NoteEntity>> getBookmarkedNotes();

    @Query("SELECT * FROM notes WHERE lastOpenedAt > 0 ORDER BY lastOpenedAt DESC LIMIT 5")
    LiveData<List<NoteEntity>> getContinueStudyingNotes();

    @Query("SELECT * FROM notes WHERE id = :noteId")
    LiveData<NoteEntity> getNoteById(int noteId);

    @Query("UPDATE notes SET localFilePath = :path, isDownloaded = 1 WHERE id = :noteId")
    void markAsDownloaded(int noteId, String path);

    @Query("UPDATE notes SET localFilePath = null, isDownloaded = 0 WHERE id = :noteId")
    void markAsDeleted(int noteId);

    @Query("UPDATE notes SET isBookmarked = :bookmarked WHERE id = :noteId")
    void setBookmarked(int noteId, boolean bookmarked);

    @Query("UPDATE notes SET lastReadPage = :lastPage, totalPages = :totalPages, lastOpenedAt = :openedAt WHERE id = :noteId")
    void updateReadingProgress(int noteId, int lastPage, int totalPages, long openedAt);

    @Query("DELETE FROM notes WHERE id = :noteId")
    void deleteNoteById(int noteId);

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' " +
           "OR subject LIKE '%' || :query || '%' " +
           "OR courseCode LIKE '%' || :query || '%' " +
           "OR departmentName LIKE '%' || :query || '%' " +
           "OR description LIKE '%' || :query || '%' " +
           "OR uploadedBy LIKE '%' || :query || '%' " +
           "ORDER BY savedAt DESC")
    LiveData<List<NoteEntity>> searchNotes(String query);

    @Query("SELECT * FROM notes WHERE " +
           "(:dept = '' OR departmentName LIKE '%' || :dept || '%') AND " +
           "(:course = '' OR courseCode LIKE '%' || :course || '%' OR subject LIKE '%' || :course || '%') AND " +
           "(:query = '' OR title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR courseCode LIKE '%' || :query || '%') " +
           "ORDER BY savedAt DESC")
    LiveData<List<NoteEntity>> filterNotes(String dept, String course, String query);

    @Query("SELECT COUNT(*) FROM notes")
    int getNoteCount();

    @Query("SELECT COUNT(*) FROM notes WHERE isDownloaded = 1")
    int getDownloadedCount();
}
