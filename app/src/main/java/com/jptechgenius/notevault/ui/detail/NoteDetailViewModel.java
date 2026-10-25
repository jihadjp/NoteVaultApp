package com.jptechgenius.notevault.ui.detail;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.data.repository.NoteRepository;
import com.jptechgenius.notevault.utils.DownloadCompleteListener;

/**
 * ViewModel for NoteDetailActivity.
 * Manages PDF loading state, reading progress, bookmarking, download, review, and report actions.
 */
public class NoteDetailViewModel extends AndroidViewModel {

    private final NoteRepository repository;
    private LiveData<NoteEntity> dbNoteLiveData;
    private final Observer<NoteEntity> dbNoteObserver = note -> {
        if (note != null) {
            currentNote.postValue(note);
        }
    };

    public final MutableLiveData<NoteEntity> currentNote = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isDownloading = new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> isPdfLoading = new MutableLiveData<>(true);
    public final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    public final MutableLiveData<String> pdfError = new MutableLiveData<>();
    public final MutableLiveData<Integer> currentPage = new MutableLiveData<>(0);
    public final MutableLiveData<Integer> totalPages = new MutableLiveData<>(0);

    public NoteDetailViewModel(Application application) {
        super(application);
        repository = new NoteRepository(application);
    }

    /**
     * Load a note from Room by ID and keep observing changes with proper cleanup in onCleared().
     */
    public void loadNote(int noteId) {
        if (dbNoteLiveData != null) {
            dbNoteLiveData.removeObserver(dbNoteObserver);
        }
        dbNoteLiveData = repository.getNoteById(noteId);
        dbNoteLiveData.observeForever(dbNoteObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (dbNoteLiveData != null) {
            dbNoteLiveData.removeObserver(dbNoteObserver);
        }
    }

    /**
     * Toggle bookmark state for the current note.
     */
    public void toggleBookmark() {
        NoteEntity note = currentNote.getValue();
        if (note == null) return;
        boolean willBookmark = !note.isBookmarked();
        repository.toggleBookmark(note);
        toastMessage.setValue(willBookmark ? "Added to Bookmarks ★" : "Removed from Bookmarks");
    }

    /**
     * Persist reading progress locally in Room.
     */
    public void saveReadingProgress(int page, int total) {
        NoteEntity note = currentNote.getValue();
        if (note != null && page > 0 && total > 0) {
            repository.updateReadingProgress(note.getId(), page, total);
        }
    }

    /**
     * Start downloading the current note's PDF.
     */
    public void downloadNote() {
        NoteEntity note = currentNote.getValue();
        if (note == null) return;

        isDownloading.setValue(true);
        toastMessage.setValue("Downloading PDF for offline access…");

        repository.downloadPDF(note, new DownloadCompleteListener() {
            @Override
            public void onComplete(String localFilePath) {
                isDownloading.postValue(false);
                toastMessage.postValue("Saved offline! Available without internet.");
            }

            @Override
            public void onFailure(String reason) {
                isDownloading.postValue(false);
                toastMessage.postValue("Download issue: " + reason);
            }
        });
    }

    public void submitReview(int rating, String reviewText, String reviewerName) {
        NoteEntity note = currentNote.getValue();
        int id = note != null ? note.getId() : 0;
        repository.submitResourceReview(id, rating, reviewText, reviewerName, new NoteRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                toastMessage.postValue(message);
            }

            @Override
            public void onError(String error) {
                toastMessage.postValue(error);
            }
        });
    }

    public void submitReport(String reason, String details, String reporterName) {
        NoteEntity note = currentNote.getValue();
        int id = note != null ? note.getId() : 0;
        repository.submitResourceReport(id, reason, details, reporterName, new NoteRepository.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                toastMessage.postValue(message);
            }

            @Override
            public void onError(String error) {
                toastMessage.postValue(error);
            }
        });
    }

    /**
     * Remove local PDF file but keep the note in Room.
     */
    public void deleteLocalFile() {
        NoteEntity note = currentNote.getValue();
        if (note == null) return;
        repository.deleteLocalFile(note);
        toastMessage.setValue("Offline copy removed.");
    }

    /**
     * Permanently delete the note from Room and remove local file.
     */
    public void deleteNote() {
        NoteEntity note = currentNote.getValue();
        if (note == null) return;
        repository.deleteNote(note);
        toastMessage.setValue("Resource removed from device cache.");
    }

    public void setPdfLoading(boolean loading) {
        isPdfLoading.setValue(loading);
    }

    public void setPdfError(String message) {
        pdfError.setValue(message);
        isPdfLoading.setValue(false);
    }

    public void setPageInfo(int current, int total) {
        currentPage.setValue(current);
        totalPages.setValue(total);
    }

    public LiveData<NoteEntity> getCurrentNote() {
        return currentNote;
    }
}
