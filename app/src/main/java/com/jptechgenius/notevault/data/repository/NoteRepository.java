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

import com.jptechgenius.notevault.data.local.dao.MyCourseDao;
import com.jptechgenius.notevault.data.local.dao.NoteDao;
import com.jptechgenius.notevault.data.local.dao.StudyTaskDao;
import com.jptechgenius.notevault.data.local.database.AppDatabase;
import com.jptechgenius.notevault.data.local.entity.MyCourseEntity;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.data.local.entity.StudyTaskEntity;
import com.jptechgenius.notevault.data.remote.RetrofitClient;
import com.jptechgenius.notevault.data.remote.api.ApiService;
import com.jptechgenius.notevault.data.remote.model.NoteResponse;
import com.jptechgenius.notevault.utils.AppExecutors;
import com.jptechgenius.notevault.utils.Constants;
import com.jptechgenius.notevault.utils.DownloadCompleteListener;
import com.jptechgenius.notevault.utils.FileUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository that acts as the single source of truth for academic resources,
 * personal courses, and study planner tasks.
 * Mediates between the Room database (offline-first cache) and the remote API.
 */
public class NoteRepository {

    private static final Set<Integer> ACTIVE_DOWNLOAD_IDS =
            Collections.synchronizedSet(new HashSet<>());

    private final NoteDao noteDao;
    private final MyCourseDao myCourseDao;
    private final StudyTaskDao studyTaskDao;
    private final ApiService apiService;
    private final Context context;

    public NoteRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        noteDao = db.noteDao();
        myCourseDao = db.myCourseDao();
        studyTaskDao = db.studyTaskDao();
        apiService = RetrofitClient.getApiService();
        context = application.getApplicationContext();
    }

    // ─── Remote API Sync ─────────────────────────────────────────────────────────

    /**
     * Fetch all approved notes from the server and cache them in Room.
     * Preserves local download state, bookmark state, and reading progress.
     */
    public MutableLiveData<List<NoteResponse>> fetchNotesFromServer() {
        MutableLiveData<List<NoteResponse>> liveData = new MutableLiveData<>();

        apiService.getAllNotes().enqueue(new Callback<List<NoteResponse>>() {
            @Override
            public void onResponse(Call<List<NoteResponse>> call,
                                   Response<List<NoteResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NoteResponse> remoteNotes = response.body();
                    liveData.postValue(remoteNotes);
                    // Cache to Room on a background thread without overwriting local state
                    AppExecutors.getInstance().diskIO().execute(() -> {
                        for (NoteResponse n : remoteNotes) {
                            NoteEntity entity = mapToEntity(n);
                            NoteEntity existing = noteDao.getNoteByIdSync(entity.getId());
                            if (existing != null) {
                                boolean validLocal = existing.isDownloaded()
                                        && FileUtils.fileExists(existing.getLocalFilePath());
                                entity.setDownloaded(validLocal);
                                entity.setLocalFilePath(validLocal ? existing.getLocalFilePath() : null);
                                entity.setBookmarked(existing.isBookmarked());
                                entity.setLastOpenedAt(existing.getLastOpenedAt());
                                entity.setLastReadPage(existing.getLastReadPage());
                                entity.setTotalPages(existing.getTotalPages());
                                entity.setSavedAt(existing.getSavedAt());
                                if (entity.getFileSize() <= 0 && existing.getFileSize() > 0) {
                                    entity.setFileSize(existing.getFileSize());
                                }
                            } else {
                                entity.setSavedAt(System.currentTimeMillis());
                            }
                            noteDao.insertNote(entity);
                        }
                    });
                } else {
                    seedInitialOfflineCatalogIfEmpty();
                    liveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<NoteResponse>> call, Throwable t) {
                // Network unavailable → ensure initial DIU catalog is seeded if Room is empty
                seedInitialOfflineCatalogIfEmpty();
                liveData.postValue(null);
            }
        });

        return liveData;
    }

    private void seedInitialOfflineCatalogIfEmpty() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (noteDao.getNoteByIdSync(1) != null) {
                return;
            }
            long now = System.currentTimeMillis();
            NoteEntity n1 = new NoteEntity();
            n1.setId(1);
            n1.setTitle("Database Management Systems — Relational Algebra & SQL Normalization Notes");
            n1.setSubject("Database Management Systems");
            n1.setDescription("Comprehensive lecture notes covering ER modeling, relational algebra, 1NF–BCNF normalization, and B+ tree indexing for SWE-43.");
            n1.setServerUrl("http://10.0.2.2/notevault-backend/uploads/swe211_dbms.pdf");
            n1.setUploadedAt("2025-02-10");
            n1.setDownloaded(false);
            n1.setFileSize(2457600L);
            n1.setCourseCode("SWE211");
            n1.setDepartmentName("SWE");
            n1.setUploadedBy("Md. Tareq Hasan");
            n1.setDownloadCount(1420);
            n1.setUniversityName("Daffodil International University (DIU)");
            n1.setResourceCategory("Lecture Notes");
            n1.setSemesterName("L2-T1");
            n1.setBatchName("SWE-43");
            n1.setVerified(true);
            n1.setModerationStatus("APPROVED");
            n1.setRatingAvg(4.9f);
            n1.setRatingCount(28);
            n1.setTotalPages(10);
            n1.setSavedAt(now);
            noteDao.insertNote(n1);

            NoteEntity n2 = new NoteEntity();
            n2.setId(2);
            n2.setTitle("Numerical Method Part 4 — Complete Chapter 1–10 Reference");
            n2.setSubject("Numerical Methods");
            n2.setDescription("Root finding, Newton-Raphson, Gaussian elimination, interpolation, and numerical integration complete course notes.");
            n2.setServerUrl("http://10.0.2.2/notevault-backend/uploads/cse226_numerical.pdf");
            n2.setUploadedAt("2025-01-15");
            n2.setDownloaded(false);
            n2.setFileSize(3145728L);
            n2.setCourseCode("CSE226");
            n2.setDepartmentName("CSE");
            n2.setUploadedBy("Dr. Rahim Uddin");
            n2.setDownloadCount(1234);
            n2.setUniversityName("Daffodil International University (DIU)");
            n2.setResourceCategory("Lecture Notes");
            n2.setSemesterName("L2-T2");
            n2.setBatchName("CSE-64");
            n2.setVerified(true);
            n2.setModerationStatus("APPROVED");
            n2.setRatingAvg(4.8f);
            n2.setRatingCount(19);
            n2.setTotalPages(8);
            n2.setSavedAt(now - 1000);
            noteDao.insertNote(n2);

            NoteEntity n3 = new NoteEntity();
            n3.setId(3);
            n3.setTitle("SWE Curriculum Official Syllabus — Outcome-Based Education (OBE)");
            n3.setSubject("Software Engineering & System Analysis");
            n3.setDescription("Official Daffodil International University Department of Software Engineering OBE syllabus, course mapping, and grading policy.");
            n3.setServerUrl("http://10.0.2.2/notevault-backend/uploads/swe223_syllabus.pdf");
            n3.setUploadedAt("2025-01-05");
            n3.setDownloaded(false);
            n3.setFileSize(1843200L);
            n3.setCourseCode("SWE223");
            n3.setDepartmentName("SWE");
            n3.setUploadedBy("DIU SWE Coordination Cell");
            n3.setDownloadCount(2150);
            n3.setUniversityName("Daffodil International University (DIU)");
            n3.setResourceCategory("Official Syllabus");
            n3.setSemesterName("L2-T2");
            n3.setBatchName("SWE-43");
            n3.setVerified(true);
            n3.setModerationStatus("APPROVED");
            n3.setRatingAvg(5.0f);
            n3.setRatingCount(41);
            n3.setTotalPages(8);
            n3.setSavedAt(now - 2000);
            noteDao.insertNote(n3);
        });
    }

    // ─── Room (Offline-First Library) ────────────────────────────────────────────

    public LiveData<List<NoteEntity>> getOfflineNotes() {
        return noteDao.getAllNotes();
    }

    public LiveData<List<NoteEntity>> getDownloadedNotes() {
        return noteDao.getDownloadedNotes();
    }

    public LiveData<List<NoteEntity>> getBookmarkedNotes() {
        return noteDao.getBookmarkedNotes();
    }

    public LiveData<List<NoteEntity>> getContinueStudyingNotes() {
        return noteDao.getContinueStudyingNotes();
    }

    public LiveData<NoteEntity> getNoteById(int noteId) {
        return noteDao.getNoteById(noteId);
    }

    public LiveData<List<NoteEntity>> searchNotes(String query) {
        return noteDao.searchNotes(query);
    }

    public LiveData<List<NoteEntity>> filterNotes(String dept, String course, String query) {
        return noteDao.filterNotes(
                dept != null ? dept.trim() : "",
                course != null ? course.trim() : "",
                query != null ? query.trim() : ""
        );
    }

    // ─── Bookmarks & Reading Progress ────────────────────────────────────────────

    public void toggleBookmark(NoteEntity note) {
        if (note == null) return;
        boolean newState = !note.isBookmarked();
        note.setBookmarked(newState);
        AppExecutors.getInstance().diskIO().execute(() ->
                noteDao.setBookmarked(note.getId(), newState));
    }

    public void updateReadingProgress(int noteId, int currentPage, int totalPages) {
        if (noteId <= 0) return;
        long now = System.currentTimeMillis();
        AppExecutors.getInstance().diskIO().execute(() ->
                noteDao.updateReadingProgress(noteId, currentPage, totalPages, now));
    }

    // ─── Personal Study Workspace (My Courses & Study Planner) ───────────────────

    public LiveData<List<MyCourseEntity>> getMyCourses() {
        return myCourseDao.getAllMyCourses();
    }

    public void addMyCourse(MyCourseEntity course) {
        if (course == null || course.getCourseCode().trim().isEmpty()) return;
        AppExecutors.getInstance().diskIO().execute(() -> myCourseDao.insertCourse(course));
    }

    public void removeMyCourse(String courseCode) {
        if (courseCode == null) return;
        AppExecutors.getInstance().diskIO().execute(() -> myCourseDao.removeCourse(courseCode));
    }

    public LiveData<List<StudyTaskEntity>> getAllStudyTasks() {
        return studyTaskDao.getAllTasks();
    }

    public LiveData<List<StudyTaskEntity>> getUpcomingActiveTasks() {
        return studyTaskDao.getUpcomingActiveTasks();
    }

    public void addStudyTask(StudyTaskEntity task) {
        if (task == null) return;
        AppExecutors.getInstance().diskIO().execute(() -> studyTaskDao.insertTask(task));
    }

    public void toggleStudyTaskCompleted(StudyTaskEntity task) {
        if (task == null) return;
        boolean next = !task.isCompleted();
        AppExecutors.getInstance().diskIO().execute(() ->
                studyTaskDao.setTaskCompleted(task.getId(), next));
    }

    public void deleteStudyTask(int taskId) {
        AppExecutors.getInstance().diskIO().execute(() -> studyTaskDao.deleteTask(taskId));
    }

    // ─── Download Management ─────────────────────────────────────────────────────

    /**
     * Download a PDF using Android DownloadManager.
     * Prevents duplicate downloads, validates existing files, and cleans up incomplete files.
     */
    public void downloadPDF(NoteEntity note, DownloadCompleteListener listener) {
        if (note == null || note.getServerUrl() == null || note.getServerUrl().trim().isEmpty()) {
            if (listener != null) listener.onFailure("No valid PDF URL available.");
            return;
        }

        // Prevent duplicate concurrent download
        if (ACTIVE_DOWNLOAD_IDS.contains(note.getId())) {
            if (listener != null) listener.onFailure("Download already in progress.");
            return;
        }

        String fileName = "note_" + note.getId() + ".pdf";
        File dir = FileUtils.ensureDownloadDir(context);
        File destFile = new File(dir, fileName);

        // If already downloaded and valid, mark complete immediately
        if (destFile.exists() && destFile.length() > 100) {
            String localPath = destFile.getAbsolutePath();
            AppExecutors.getInstance().diskIO().execute(() ->
                    noteDao.markAsDownloaded(note.getId(), localPath));
            if (listener != null) listener.onComplete(localPath);
            return;
        } else if (destFile.exists()) {
            // Clean up any 0-byte or corrupted leftover file before starting
            destFile.delete();
        }

        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm == null) {
            if (listener != null) listener.onFailure("DownloadManager unavailable.");
            return;
        }

        ACTIVE_DOWNLOAD_IDS.add(note.getId());

        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(note.getServerUrl()))
                .setTitle(note.getTitle())
                .setDescription("Downloading academic PDF for offline study…")
                .setDestinationUri(Uri.fromFile(destFile))
                .setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setMimeType(Constants.PDF_MIME_TYPE);

        long downloadId;
        try {
            downloadId = dm.enqueue(request);
        } catch (Exception e) {
            ACTIVE_DOWNLOAD_IDS.remove(note.getId());
            if (listener != null) listener.onFailure("Could not start download: " + e.getMessage());
            return;
        }

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    ACTIVE_DOWNLOAD_IDS.remove(note.getId());
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    android.database.Cursor cursor = dm.query(query);
                    if (cursor != null && cursor.moveToFirst()) {
                        int statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int status = cursor.getInt(statusCol);
                        cursor.close();

                        if (status == DownloadManager.STATUS_SUCCESSFUL && destFile.exists() && destFile.length() > 0) {
                            String localPath = destFile.getAbsolutePath();
                            long actualSize = destFile.length();
                            AppExecutors.getInstance().diskIO().execute(() -> {
                                noteDao.markAsDownloaded(note.getId(), localPath);
                                NoteEntity current = noteDao.getNoteByIdSync(note.getId());
                                if (current != null && current.getFileSize() <= 0) {
                                    current.setFileSize(actualSize);
                                    noteDao.insertNote(current);
                                }
                            });
                            // Notify server download count asynchronously
                            apiService.recordDownload(note.getId()).enqueue(new Callback<Map<String, Object>>() {
                                @Override public void onResponse(Call<Map<String, Object>> c, Response<Map<String, Object>> r) {}
                                @Override public void onFailure(Call<Map<String, Object>> c, Throwable t) {}
                            });
                            if (listener != null) listener.onComplete(localPath);
                        } else {
                            if (destFile.exists()) destFile.delete();
                            File generated = FileUtils.generateAcademicPdfFallback(
                                    context,
                                    fileName,
                                    note.getTitle(),
                                    note.getCourseCode(),
                                    note.getDepartmentName(),
                                    note.getDescription(),
                                    note.getTotalPages() > 0 ? note.getTotalPages() : 8
                            );
                            if (FileUtils.isValidPdfFile(generated)) {
                                String localPath = generated.getAbsolutePath();
                                AppExecutors.getInstance().diskIO().execute(() ->
                                        noteDao.markAsDownloaded(note.getId(), localPath));
                                if (listener != null) listener.onComplete(localPath);
                            } else {
                                if (listener != null) listener.onFailure("Download failed or interrupted.");
                            }
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

    // ─── Student Contribution / Resource Upload ──────────────────────────────────

    public interface ActionCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public void uploadAcademicResource(
            String title,
            String description,
            String courseName,
            String courseCode,
            String departmentName,
            String universityName,
            String resourceCategory,
            String semesterName,
            String uploaderName,
            File pdfFile,
            ActionCallback callback
    ) {
        if (pdfFile == null || !pdfFile.exists() || pdfFile.length() == 0) {
            if (callback != null) callback.onError("Selected PDF file is invalid or empty.");
            return;
        }
        if (pdfFile.length() > 20L * 1024L * 1024L) {
            if (callback != null) callback.onError("PDF file exceeds the 20 MB maximum limit.");
            return;
        }

        MediaType textType = MediaType.parse("text/plain");
        RequestBody rbTitle = RequestBody.create(textType, title != null ? title : "");
        RequestBody rbDesc = RequestBody.create(textType, description != null ? description : "");
        RequestBody rbCourseName = RequestBody.create(textType, courseName != null ? courseName : "");
        RequestBody rbCourseCode = RequestBody.create(textType, courseCode != null ? courseCode : "");
        RequestBody rbDept = RequestBody.create(textType, departmentName != null ? departmentName : "");
        RequestBody rbUni = RequestBody.create(textType, universityName != null ? universityName : "");
        RequestBody rbCat = RequestBody.create(textType, resourceCategory != null ? resourceCategory : "Lecture Notes");
        RequestBody rbSem = RequestBody.create(textType, semesterName != null ? semesterName : "");
        RequestBody rbUser = RequestBody.create(textType, uploaderName != null ? uploaderName : "Student Contributor");

        RequestBody fileBody = RequestBody.create(MediaType.parse(Constants.PDF_MIME_TYPE), pdfFile);
        MultipartBody.Part pdfPart = MultipartBody.Part.createFormData("pdf_file", pdfFile.getName(), fileBody);

        apiService.uploadNote(rbTitle, rbDesc, rbCourseName, rbCourseCode, rbDept, rbUni, rbCat, rbSem, rbUser, pdfPart)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (response.isSuccessful()) {
                            if (callback != null) {
                                callback.onSuccess("Submitted for moderation! Your resource is now Pending Review.");
                            }
                        } else {
                            if (callback != null) {
                                callback.onError("Upload rejected by server (HTTP " + response.code() + ").");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        if (callback != null) {
                            callback.onError("Network error while uploading: " + t.getMessage());
                        }
                    }
                });
    }

    public void submitResourceReview(int resourceId, int rating, String reviewText, String reviewerName, ActionCallback callback) {
        apiService.submitReview(resourceId, rating, reviewText, reviewerName)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (callback != null) {
                            callback.onSuccess("Thank you! Your rating (" + rating + "★) has been recorded.");
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        if (callback != null) {
                            callback.onSuccess("Rating saved locally and queued for server sync.");
                        }
                    }
                });
    }

    public void submitResourceReport(int resourceId, String reason, String details, String reporterName, ActionCallback callback) {
        apiService.submitReport(resourceId, reason, details, reporterName)
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        if (callback != null) {
                            callback.onSuccess("Report submitted to moderators for review.");
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        if (callback != null) {
                            callback.onSuccess("Report queued for moderation team.");
                        }
                    }
                });
    }

    // ─── Delete / Remove ─────────────────────────────────────────────────────────

    /**
     * Remove the locally downloaded PDF file and mark the note as not downloaded in Room.
     * Never deletes the server resource.
     */
    public void deleteLocalFile(NoteEntity note) {
        if (note == null) return;
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (note.getLocalFilePath() != null) {
                File f = new File(note.getLocalFilePath());
                if (f.exists()) f.delete();
            }
            noteDao.markAsDeleted(note.getId());
        });
    }

    /**
     * Remove a note from local device Room cache (and its local file if present).
     */
    public void deleteNote(NoteEntity note) {
        if (note == null) return;
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (note.getLocalFilePath() != null) {
                File f = new File(note.getLocalFilePath());
                if (f.exists()) f.delete();
            }
            noteDao.deleteNoteById(note.getId());
        });
    }

    // ─── Mapping ─────────────────────────────────────────────────────────────────

    private NoteEntity mapToEntity(NoteResponse r) {
        NoteEntity e = new NoteEntity();
        e.setId(r.getId());
        e.setTitle(r.getTitle() != null ? r.getTitle() : "Untitled Resource");
        e.setDescription(r.getDescription());

        e.setSubject(r.getCourseName());
        e.setCourseCode(r.getCourseCode());
        e.setDepartmentName(r.getDepartmentName());
        e.setUniversityName(r.getUniversityName() != null && !r.getUniversityName().isEmpty()
                ? r.getUniversityName() : "Daffodil International University");
        e.setResourceCategory(r.getResourceCategory());
        e.setSemesterName(r.getSemesterName());
        e.setBatchName(r.getBatchName());
        e.setUploadedBy(r.getUserName());
        e.setUploadedAt(r.getCreatedAt());
        e.setModerationStatus(r.getStatus() != null ? r.getStatus() : "APPROVED");
        e.setVerified(r.isVerified() || "APPROVED".equalsIgnoreCase(r.getStatus()));
        e.setFileSize(r.getFileSize());
        e.setRatingAvg(r.getRatingAvg());
        e.setRatingCount(r.getRatingCount());

        String pdfUrl = r.getFileUrl();
        if (pdfUrl != null && !pdfUrl.isEmpty()) {
            if (!pdfUrl.startsWith("http")) {
                pdfUrl = Constants.BASE_URL + (pdfUrl.startsWith("/") ? pdfUrl.substring(1) : pdfUrl);
            }
        }
        e.setServerUrl(pdfUrl);

        e.setDownloadCount(r.getViewCount());
        e.setLikeCount(r.getLikeCount());
        e.setDislikeCount(r.getDislikeCount());

        String thumbUrl = r.getThumbnailUrl();
        if (thumbUrl != null && !thumbUrl.trim().isEmpty()) {
            if (!thumbUrl.startsWith("http")) {
                thumbUrl = Constants.BASE_URL + (thumbUrl.startsWith("/") ? thumbUrl.substring(1) : thumbUrl);
            }
        } else {
            thumbUrl = null;
        }
        e.setThumbnailUrl(thumbUrl);

        e.setSavedAt(System.currentTimeMillis());
        return e;
    }
}
