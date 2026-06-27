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
 * Provides all database operations for the notes table.
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

    @Query("SELECT * FROM notes WHERE isDownloaded = 1")
    LiveData<List<NoteEntity>> getDownloadedNotes();

    @Query("SELECT * FROM notes WHERE id = :noteId")
    LiveData<NoteEntity> getNoteById(int noteId);

    @Query("UPDATE notes SET localFilePath = :path, isDownloaded = 1 WHERE id = :noteId")
    void markAsDownloaded(int noteId, String path);

    @Query("UPDATE notes SET localFilePath = null, isDownloaded = 0 WHERE id = :noteId")
    void markAsDeleted(int noteId);

    @Query("DELETE FROM notes WHERE id = :noteId")
    void deleteNoteById(int noteId);

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' " +
           "OR subject LIKE '%' || :query || '%' " +
           "OR courseCode LIKE '%' || :query || '%' " +
           "OR departmentName LIKE '%' || :query || '%' " +
           "OR uploadedBy LIKE '%' || :query || '%' " +
           "ORDER BY savedAt DESC")
    LiveData<List<NoteEntity>> searchNotes(String query);

    @Query("SELECT COUNT(*) FROM notes")
    int getNoteCount();

    @Query("SELECT COUNT(*) FROM notes WHERE isDownloaded = 1")
    int getDownloadedCount();
}
