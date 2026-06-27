package com.jptechgenius.notevault.data.remote.api;

import com.jptechgenius.notevault.data.remote.model.NoteResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API interface for NoteVault backend.
 */
public interface ApiService {

    /**
     * Fetch all notes from the server.
     */
    @GET("api/notes")
    Call<List<NoteResponse>> getAllNotes();

    /**
     * Fetch a single note by ID.
     */
    @GET("api/notes/{id}")
    Call<NoteResponse> getNoteById(@Path("id") int id);

    /**
     * Search notes by keyword.
     */
    @GET("api/notes/search")
    Call<List<NoteResponse>> searchNotes(@Query("q") String query);

    /**
     * Fetch notes by subject/category.
     */
    @GET("api/notes")
    Call<List<NoteResponse>> getNotesBySubject(@Query("subject") String subject);
}
