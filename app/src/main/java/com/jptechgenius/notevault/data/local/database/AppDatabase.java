package com.jptechgenius.notevault.data.local.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.jptechgenius.notevault.data.local.dao.MyCourseDao;
import com.jptechgenius.notevault.data.local.dao.NoteDao;
import com.jptechgenius.notevault.data.local.dao.StudyTaskDao;
import com.jptechgenius.notevault.data.local.entity.MyCourseEntity;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.data.local.entity.StudyTaskEntity;
import com.jptechgenius.notevault.utils.Constants;

/**
 * Room database singleton for NoteVault DIU.
 * Holds notes, my_courses, and study_tasks tables with non-destructive migration.
 */
@Database(
        entities = {NoteEntity.class, MyCourseEntity.class, StudyTaskEntity.class},
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract NoteDao noteDao();
    public abstract MyCourseDao myCourseDao();
    public abstract StudyTaskDao studyTaskDao();

    /**
     * Non-destructive migration from version 2 to version 3.
     * Preserves all existing cached notes and local PDF file mappings.
     */
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE notes ADD COLUMN universityName TEXT");
            db.execSQL("ALTER TABLE notes ADD COLUMN resourceCategory TEXT");
            db.execSQL("ALTER TABLE notes ADD COLUMN semesterName TEXT");
            db.execSQL("ALTER TABLE notes ADD COLUMN batchName TEXT");
            db.execSQL("ALTER TABLE notes ADD COLUMN isBookmarked INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE notes ADD COLUMN isVerified INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE notes ADD COLUMN moderationStatus TEXT");
            db.execSQL("ALTER TABLE notes ADD COLUMN ratingAvg REAL NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE notes ADD COLUMN ratingCount INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE notes ADD COLUMN lastOpenedAt INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE notes ADD COLUMN lastReadPage INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE notes ADD COLUMN totalPages INTEGER NOT NULL DEFAULT 0");

            db.execSQL("CREATE TABLE IF NOT EXISTS `my_courses` (" +
                    "`courseCode` TEXT NOT NULL, " +
                    "`courseTitle` TEXT, " +
                    "`departmentName` TEXT, " +
                    "`universityName` TEXT, " +
                    "`semesterName` TEXT, " +
                    "`creditHours` REAL NOT NULL DEFAULT 3.0, " +
                    "`addedAt` INTEGER NOT NULL DEFAULT 0, " +
                    "PRIMARY KEY(`courseCode`))");

            db.execSQL("CREATE TABLE IF NOT EXISTS `study_tasks` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`title` TEXT, " +
                    "`courseCode` TEXT, " +
                    "`taskType` TEXT, " +
                    "`targetDateMillis` INTEGER NOT NULL DEFAULT 0, " +
                    "`notes` TEXT, " +
                    "`isCompleted` INTEGER NOT NULL DEFAULT 0, " +
                    "`createdAt` INTEGER NOT NULL DEFAULT 0)");
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    Constants.DB_NAME
            )
            .addMigrations(MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
}
