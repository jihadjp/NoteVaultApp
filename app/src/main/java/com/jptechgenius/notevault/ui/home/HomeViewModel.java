package com.jptechgenius.notevault.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.data.remote.model.NoteResponse;
import com.jptechgenius.notevault.data.repository.NoteRepository;

import java.util.List;

/**
 * ViewModel for HomeActivity.
 * Manages note list state, tab selection, search, downloads, and offline fallback.
 */
public class HomeViewModel extends AndroidViewModel {

    public enum Tab { ALL, DOWNLOADED }

    private final NoteRepository repository;

    // ── Observable state ──────────────────────────────────────────────────────
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    public final MutableLiveData<Tab> currentTab = new MutableLiveData<>(Tab.ALL);
    public final MutableLiveData<Boolean> isOffline = new MutableLiveData<>(false);

    // ── Note data ─────────────────────────────────────────────────────────────
    private final LiveData<List<NoteEntity>> allNotes;
    private final LiveData<List<NoteEntity>> downloadedNotes;
    private final MutableLiveData<List<NoteResponse>> serverNotes = new MutableLiveData<>();

    // ── Download tracking (noteId → in-progress) ──────────────────────────────
    public final MutableLiveData<Integer> downloadingNoteId = new MutableLiveData<>(-1);

    public HomeViewModel(Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getOfflineNotes();
        downloadedNotes = repository.getDownloadedNotes();
    }

    // ─── Data loading ─────────────────────────────────────────────────────────

    /**
     * Trigger a refresh from the remote server.
     * Falls back to cached Room data on failure.
     */
    public void loadFromServer() {
        isLoading.setValue(true);
        repository.fetchNotesFromServer().observeForever(notes -> {
            isLoading.postValue(false);
            if (notes != null) {
                serverNotes.postValue(notes);
                isOffline.postValue(false);
            } else {
                isOffline.postValue(true);
                errorMessage.postValue("No internet. Showing offline data.");
            }
        });
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public LiveData<List<NoteEntity>> getAllNotes() {
        return allNotes;
    }

    public LiveData<List<NoteEntity>> getDownloadedNotes() {
        return downloadedNotes;
    }

    public LiveData<List<NoteResponse>> getServerNotes() {
        return serverNotes;
    }

    public LiveData<List<NoteEntity>> searchNotes(String query) {
        return repository.searchNotes(query);
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    public void setCurrentTab(Tab tab) {
        currentTab.setValue(tab);
    }

    public void downloadNote(NoteEntity note) {
        downloadingNoteId.setValue(note.getId());
        toastMessage.setValue("Starting download…");

        repository.downloadPDF(note, new com.jptechgenius.notevault.utils.DownloadCompleteListener() {
            @Override
            public void onComplete(String localFilePath) {
                downloadingNoteId.postValue(-1);
                toastMessage.postValue("Downloaded: " + note.getTitle());
            }

            @Override
            public void onFailure(String reason) {
                downloadingNoteId.postValue(-1);
                toastMessage.postValue("Download failed: " + reason);
            }
        });
    }

    public void deleteLocalFile(NoteEntity note) {
        repository.deleteLocalFile(note);
        toastMessage.setValue("Offline file removed.");
    }

    public void deleteNote(NoteEntity note) {
        repository.deleteNote(note);
        toastMessage.setValue("Note deleted.");
    }
}
