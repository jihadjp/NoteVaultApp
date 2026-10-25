package com.jptechgenius.notevault.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.jptechgenius.notevault.data.local.entity.MyCourseEntity;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.data.local.entity.StudyTaskEntity;
import com.jptechgenius.notevault.data.remote.RetrofitClient;
import com.jptechgenius.notevault.data.remote.model.NoteResponse;
import com.jptechgenius.notevault.data.remote.model.QuestionResponse;
import com.jptechgenius.notevault.data.repository.NoteRepository;
import com.jptechgenius.notevault.utils.DownloadCompleteListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel for HomeActivity.
 * Manages state for the 5 main navigation destinations (Home, Explore, Downloads, Bookmarks, Profile),
 * offline-first synchronization, category filtering, personal courses, study planner, and student contributions.
 */
public class HomeViewModel extends AndroidViewModel {

    private final NoteRepository repository;
    private LiveData<List<NoteResponse>> activeSyncLiveData;
    private Observer<List<NoteResponse>> activeSyncObserver;

    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<Tab> currentTab = new MutableLiveData<>(Tab.NOTES);
    public final MutableLiveData<String> selectedCategory = new MutableLiveData<>("ALL");
    public final MutableLiveData<List<QuestionResponse>> questions = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isOffline = new MutableLiveData<>(false);
    public final MutableLiveData<Integer> downloadingNoteId = new MutableLiveData<>(-1);

    public enum Tab {
        NOTES,      // Home
        QUESTIONS,  // Explore
        DOWNLOADED, // Downloads
        BOOKMARKS,  // Bookmarks
        PROFILE     // Profile & Study Workspace
    }

    public HomeViewModel(Application application) {
        super(application);
        repository = new NoteRepository(application);
    }

    public LiveData<List<NoteEntity>> getAllNotes() {
        return repository.getOfflineNotes();
    }

    public LiveData<List<NoteEntity>> getDownloadedNotes() {
        return repository.getDownloadedNotes();
    }

    public LiveData<List<NoteEntity>> getBookmarkedNotes() {
        return repository.getBookmarkedNotes();
    }

    public LiveData<List<NoteEntity>> getContinueStudyingNotes() {
        return repository.getContinueStudyingNotes();
    }

    public LiveData<List<NoteEntity>> searchNotes(String query) {
        return repository.searchNotes(query);
    }

    public LiveData<List<NoteEntity>> filterNotes(String dept, String course, String query) {
        return repository.filterNotes(dept, course, query);
    }

    public LiveData<List<MyCourseEntity>> getMyCourses() {
        return repository.getMyCourses();
    }

    public void addMyCourse(MyCourseEntity course) {
        repository.addMyCourse(course);
        toastMessage.setValue("Course added to My Courses");
    }

    public void removeMyCourse(String courseCode) {
        repository.removeMyCourse(courseCode);
        toastMessage.setValue("Course removed from My Courses");
    }

    public LiveData<List<StudyTaskEntity>> getUpcomingActiveTasks() {
        return repository.getUpcomingActiveTasks();
    }

    public LiveData<List<StudyTaskEntity>> getAllStudyTasks() {
        return repository.getAllStudyTasks();
    }

    public void addStudyTask(StudyTaskEntity task) {
        repository.addStudyTask(task);
        toastMessage.setValue("Added to Study Planner");
    }

    public void toggleStudyTaskCompleted(StudyTaskEntity task) {
        repository.toggleStudyTaskCompleted(task);
    }

    public void deleteStudyTask(int taskId) {
        repository.deleteStudyTask(taskId);
        toastMessage.setValue("Planner item removed");
    }

    public void setTab(Tab tab) {
        currentTab.setValue(tab);
        if (tab == Tab.QUESTIONS) {
            loadQuestions("", "", "", "");
        }
    }

    public void setCategoryFilter(String category) {
        selectedCategory.setValue(category != null ? category : "ALL");
    }

    public void loadFromServer() {
        if (Boolean.TRUE.equals(isLoading.getValue())) {
            return;
        }
        isLoading.setValue(true);
        cleanupSyncObserver();

        activeSyncLiveData = repository.fetchNotesFromServer();
        activeSyncObserver = notes -> {
            isLoading.postValue(false);
            isOffline.postValue(notes == null);
            cleanupSyncObserver();
        };
        activeSyncLiveData.observeForever(activeSyncObserver);
    }

    private void cleanupSyncObserver() {
        if (activeSyncLiveData != null && activeSyncObserver != null) {
            activeSyncLiveData.removeObserver(activeSyncObserver);
            activeSyncLiveData = null;
            activeSyncObserver = null;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cleanupSyncObserver();
    }

    public void loadQuestions(String dept, String course, String sem, String type) {
        isLoading.setValue(true);
        RetrofitClient.getApiService().getQuestions(dept, course, sem, type)
                .enqueue(new Callback<List<QuestionResponse>>() {
                    @Override
                    public void onResponse(Call<List<QuestionResponse>> call, Response<List<QuestionResponse>> response) {
                        isLoading.postValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            isOffline.postValue(false);
                            questions.postValue(response.body());
                        } else {
                            questions.postValue(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<QuestionResponse>> call, Throwable t) {
                        isLoading.postValue(false);
                        isOffline.postValue(true);
                        toastMessage.postValue("Offline: Showing cached academic resources");
                    }
                });
    }

    public void downloadNote(NoteEntity note) {
        if (note == null) return;
        downloadingNoteId.setValue(note.getId());
        repository.downloadPDF(note, new DownloadCompleteListener() {
            @Override
            public void onComplete(String path) {
                downloadingNoteId.postValue(-1);
                toastMessage.postValue("Saved for offline reading!");
            }

            @Override
            public void onFailure(String reason) {
                downloadingNoteId.postValue(-1);
                toastMessage.postValue(reason);
            }
        });
    }

    public void toggleBookmark(NoteEntity note) {
        if (note == null) return;
        boolean next = !note.isBookmarked();
        repository.toggleBookmark(note);
        toastMessage.setValue(next ? "Saved to Bookmarks ★" : "Removed from Bookmarks");
    }

    public void deleteLocalFile(NoteEntity note) {
        repository.deleteLocalFile(note);
        toastMessage.setValue("Offline copy removed.");
    }

    public void deleteNote(NoteEntity note) {
        repository.deleteNote(note);
        toastMessage.setValue("Removed from device cache.");
    }

    public void uploadResource(
            String title,
            String description,
            String courseName,
            String courseCode,
            String departmentName,
            String universityName,
            String resourceCategory,
            String semesterName,
            String uploaderName,
            File pdfFile
    ) {
        isLoading.setValue(true);
        repository.uploadAcademicResource(
                title, description, courseName, courseCode,
                departmentName, universityName, resourceCategory, semesterName,
                uploaderName, pdfFile,
                new NoteRepository.ActionCallback() {
                    @Override
                    public void onSuccess(String message) {
                        isLoading.postValue(false);
                        toastMessage.postValue(message);
                    }

                    @Override
                    public void onError(String error) {
                        isLoading.postValue(false);
                        toastMessage.postValue(error);
                    }
                }
        );
    }
}
