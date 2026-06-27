package com.jptechgenius.notevault.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.jptechgenius.notevault.data.local.dao.NoteDao;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.utils.Constants;

/**
 * Room database singleton for NoteVault DIU.
 * Holds the notes table and provides DAO access.
 */
@Database(entities = {NoteEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract NoteDao noteDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    Constants.DB_NAME
            )
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
}
