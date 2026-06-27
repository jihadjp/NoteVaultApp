package com.jptechgenius.notevault.ui.detail;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.data.repository.NoteRepository;
import com.jptechgenius.notevault.utils.DownloadCompleteListener;

/**
 * ViewModel for NoteDetailActivity.
 * Manages PDF loading state, download, and delete actions for a single note.
 */
public class NoteDetailViewModel extends AndroidViewModel {

    private final NoteRepository repository;

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
     * Load a note from Room by ID and keep observing changes.
     */
    public void loadNote(int noteId) {
        repository.getNoteById(noteId).observeForever(note -> {
            if (note != null) {
                currentNote.postValue(note);
            }
        });
    }

    /**
     * Start downloading the current note's PDF.
     */
    public void downloadNote() {
        NoteEntity note = currentNote.getValue();
        if (note == null) return;

        isDownloading.setValue(true);
        toastMessage.setValue("Downloading…");

        repository.downloadPDF(note, new DownloadCompleteListener() {
            @Override
            public void onComplete(String localFilePath) {
                isDownloading.postValue(false);
                toastMessage.postValue("Saved to device!");
                // currentNote will auto-update via Room LiveData
            }

            @Override
            public void onFailure(String reason) {
                isDownloading.postValue(false);
                toastMessage.postValue("Download failed: " + reason);
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
        toastMessage.setValue("Note deleted.");
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

    /** Returns the LiveData for the note, observed by the Activity. */
    public LiveData<NoteEntity> getCurrentNote() {
        return currentNote;
    }
}
