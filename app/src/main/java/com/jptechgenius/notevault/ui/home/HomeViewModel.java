package com.jptechgenius.notevault.ui.home;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.data.repository.NoteRepository;
import com.jptechgenius.notevault.data.remote.model.QuestionResponse;
import com.jptechgenius.notevault.data.remote.RetrofitClient;
import java.util.List;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends AndroidViewModel {

    private final NoteRepository repository;
    public final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    public final MutableLiveData<Tab> currentTab = new MutableLiveData<>(Tab.NOTES);
    public final MutableLiveData<List<QuestionResponse>> questions = new MutableLiveData<>(new ArrayList<>());
    public final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isOffline = new MutableLiveData<>(false);
    public final MutableLiveData<Integer> downloadingNoteId = new MutableLiveData<>(-1);

    public enum Tab { NOTES, QUESTIONS, DOWNLOADED }

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

    public LiveData<List<NoteEntity>> searchNotes(String query) {
        return repository.searchNotes(query);
    }

    public void setTab(Tab tab) {
        currentTab.setValue(tab);
        if (tab == Tab.QUESTIONS) {
            loadQuestions("", "", "", "");
        }
    }

    public void loadFromServer() {
        isLoading.setValue(true);
        repository.fetchNotesFromServer().observeForever(notes -> {
            isLoading.postValue(false);
            if (notes == null) isOffline.postValue(true);
        });
    }

    public void loadQuestions(String dept, String course, String sem, String type) {
        isLoading.setValue(true);
        RetrofitClient.getApiService().getQuestions(dept, course, sem, type)
                .enqueue(new Callback<List<QuestionResponse>>() {
            @Override
            public void onResponse(Call<List<QuestionResponse>> call, Response<List<QuestionResponse>> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    questions.postValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<QuestionResponse>> call, Throwable t) {
                isLoading.postValue(false);
                toastMessage.postValue("Failed to load questions");
            }
        });
    }

    public void downloadNote(NoteEntity note) {
        downloadingNoteId.setValue(note.getId());
        repository.downloadPDF(note, new com.jptechgenius.notevault.utils.DownloadCompleteListener() {
            @Override
            public void onComplete(String path) {
                downloadingNoteId.postValue(-1);
                toastMessage.postValue("Downloaded!");
            }
            @Override
            public void onFailure(String reason) {
                downloadingNoteId.postValue(-1);
                toastMessage.postValue(reason);
            }
        });
    }

    public void deleteLocalFile(NoteEntity note) {
        repository.deleteLocalFile(note);
        toastMessage.setValue("Offline copy removed.");
    }
}
