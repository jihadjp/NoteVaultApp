package com.jptechgenius.notevault.data.repository;

import android.app.Application;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jptechgenius.notevault.data.local.dao.NoteDao;
import com.jptechgenius.notevault.data.local.database.AppDatabase;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.data.remote.RetrofitClient;
import com.jptechgenius.notevault.data.remote.api.ApiService;
import com.jptechgenius.notevault.data.remote.model.NoteResponse;
import com.jptechgenius.notevault.utils.AppExecutors;
import com.jptechgenius.notevault.utils.Constants;
import com.jptechgenius.notevault.utils.DownloadCompleteListener;
import com.jptechgenius.notevault.utils.FileUtils;

import java.io.File;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository that acts as the single source of truth for note data.
 * Mediates between the Room database (offline cache) and the remote API.
 */
public class NoteRepository {

    private final NoteDao noteDao;
    private final ApiService apiService;
    private final Context context;

    public NoteRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        noteDao = db.noteDao();
        apiService = RetrofitClient.getApiService();
        context = application.getApplicationContext();
    }

    // ─── Remote API ──────────────────────────────────────────────────────────────

    /**
     * Fetch all notes from the server and cache them in Room.
     * Returns LiveData that emits the API result (null on failure → offline fallback).
     */
    public MutableLiveData<List<NoteResponse>> fetchNotesFromServer() {
        MutableLiveData<List<NoteResponse>> liveData = new MutableLiveData<>();

        apiService.getAllNotes().enqueue(new Callback<List<NoteResponse>>() {
            @Override
            public void onResponse(Call<List<NoteResponse>> call,
                                   Response<List<NoteResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    liveData.postValue(response.body());
                    // Cache to Room on a background thread
                    AppExecutors.getInstance().diskIO().execute(() -> {
                        for (NoteResponse n : response.body()) {
                            NoteEntity entity = mapToEntity(n);
                            // Preserve local state if already exists
                            NoteEntity existing = noteDao.getNoteByIdSync(entity.getId());
                            if (existing != null) {
                                entity.setDownloaded(existing.isDownloaded());
                                entity.setLocalFilePath(existing.getLocalFilePath());
                                // Also preserve savedAt if we want to keep order
                                entity.setSavedAt(existing.getSavedAt());
                            } else {
                                entity.setSavedAt(System.currentTimeMillis());
                            }
                            noteDao.insertNote(entity);
                        }
                    });
                } else {
                    liveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<NoteResponse>> call, Throwable t) {
                // Network unavailable → ViewModel will fall back to Room
                liveData.postValue(null);
            }
        });

        return liveData;
    }

    // ─── Room (Offline) ──────────────────────────────────────────────────────────

    public LiveData<List<NoteEntity>> getOfflineNotes() {
        return noteDao.getAllNotes();
    }

    public LiveData<List<NoteEntity>> getDownloadedNotes() {
        return noteDao.getDownloadedNotes();
    }

    public LiveData<NoteEntity> getNoteById(int noteId) {
        return noteDao.getNoteById(noteId);
    }

    public LiveData<List<NoteEntity>> searchNotes(String query) {
        return noteDao.searchNotes(query);
    }

    // ─── Download ────────────────────────────────────────────────────────────────

    /**
     * Download a PDF using Android DownloadManager.
     * Registers a BroadcastReceiver to detect completion and update Room.
     */
    public void downloadPDF(NoteEntity note, DownloadCompleteListener listener) {
        String fileName = "note_" + note.getId() + ".pdf";
        File dir = FileUtils.ensureDownloadDir(context);
        File destFile = new File(dir, fileName);

        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(note.getServerUrl()))
                .setTitle(note.getTitle())
                .setDescription("Downloading PDF…")
                .setDestinationUri(Uri.fromFile(destFile))
                .setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setMimeType(Constants.PDF_MIME_TYPE);

        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm == null) {
            if (listener != null) listener.onFailure("DownloadManager unavailable.");
            return;
        }

        long downloadId = dm.enqueue(request);

        // Register one-shot receiver for this specific download
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    // Check download status
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    android.database.Cursor cursor = dm.query(query);
                    if (cursor != null && cursor.moveToFirst()) {
                        int statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int status = cursor.getInt(statusCol);
                        cursor.close();

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            String localPath = destFile.getAbsolutePath();
                            AppExecutors.getInstance().diskIO().execute(() ->
                                    noteDao.markAsDownloaded(note.getId(), localPath));
                            if (listener != null) listener.onComplete(localPath);
                        } else {
                            if (listener != null) listener.onFailure("Download failed.");
                        }
                    }
                    try {
                        ctx.unregisterReceiver(this);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(receiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
    }

    // ─── Delete / Remove ─────────────────────────────────────────────────────────

    /**
     * Remove the locally downloaded PDF file and mark the note as not downloaded in Room.
     */
    public void deleteLocalFile(NoteEntity note) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (note.getLocalFilePath() != null) {
                new File(note.getLocalFilePath()).delete();
            }
            noteDao.markAsDeleted(note.getId());
        });
    }

    /**
     * Permanently delete a note from Room (and its local file if present).
     */
    public void deleteNote(NoteEntity note) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (note.getLocalFilePath() != null) {
                new File(note.getLocalFilePath()).delete();
            }
            noteDao.deleteNoteById(note.getId());
        });
    }

    // ─── Mapping ─────────────────────────────────────────────────────────────────

    private NoteEntity mapToEntity(NoteResponse r) {
        NoteEntity e = new NoteEntity();
        e.setId(r.getId());
        e.setTitle(r.getTitle());
        e.setDescription(r.getDescription());
        e.setSubject(r.getSubject());
        e.setUploadedBy(r.getUploadedBy());
        e.setUploadedAt(r.getUploadedAt());

        // Fix relative PDF URLs if necessary
        String pdfUrl = r.getPdfUrl();
        if (pdfUrl != null && !pdfUrl.isEmpty()) {
            if (!pdfUrl.startsWith("http")) {
                pdfUrl = Constants.BASE_URL + (pdfUrl.startsWith("/") ? pdfUrl.substring(1) : pdfUrl);
            }
        }
        e.setServerUrl(pdfUrl);

        e.setFileSize(r.getFileSize());
        e.setDownloadCount(r.getDownloadCount());

        // Fix relative or placeholder Thumbnail URLs
        String thumbUrl = r.getThumbnailUrl();
        if (thumbUrl != null && !thumbUrl.trim().isEmpty()) {
            // Handle placeholders
            if (thumbUrl.contains("yourdomain.com")) {
                thumbUrl = thumbUrl.replace("yourdomain.com", "notevault.jihadjp.com");
            }
            // Handle relative paths
            if (!thumbUrl.startsWith("http")) {
                thumbUrl = Constants.BASE_URL + (thumbUrl.startsWith("/") ? thumbUrl.substring(1) : thumbUrl);
            }
        } else {
            thumbUrl = null; // Ensure null if empty to avoid Glide issues
        }
        e.setThumbnailUrl(thumbUrl);

        e.setSavedAt(System.currentTimeMillis());
        return e;
    }
}
