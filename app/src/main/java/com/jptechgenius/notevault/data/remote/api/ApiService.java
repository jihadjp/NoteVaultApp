package com.jptechgenius.notevault.data.remote.api;

import com.jptechgenius.notevault.data.remote.model.NoteResponse;
import com.jptechgenius.notevault.data.remote.model.QuestionResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit API interface for NoteVault backend.
 * Preserves all existing endpoints while adding upload, download-counter, review, and report endpoints.
 */
public interface ApiService {

    /**
     * Fetch all approved notes from the server.
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

    /**
     * Fetch questions with advanced filtering.
     */
    @GET("api/questions")
    Call<List<QuestionResponse>> getQuestions(
            @Query("department") String department,
            @Query("course") String course,
            @Query("semester") String semester,
            @Query("type") String type
    );

    /**
     * Increment server download/view count for a note.
     */
    @POST("api/notes/{id}/download")
    Call<Map<String, Object>> recordDownload(@Path("id") int id);

    /**
     * Upload a new academic PDF resource for moderation (status = PENDING).
     */
    @Multipart
    @POST("api/notes")
    Call<Map<String, Object>> uploadNote(
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("course_name") RequestBody courseName,
            @Part("course_code") RequestBody courseCode,
            @Part("department_name") RequestBody departmentName,
            @Part("university_name") RequestBody universityName,
            @Part("resource_category") RequestBody resourceCategory,
            @Part("semester_name") RequestBody semesterName,
            @Part("user_name") RequestBody userName,
            @Part MultipartBody.Part pdfFile
    );

    /**
     * Submit a rating and review for an academic resource.
     */
    @FormUrlEncoded
    @POST("api/v1/resources/{id}/reviews")
    Call<Map<String, Object>> submitReview(
            @Path("id") int resourceId,
            @Field("rating") int rating,
            @Field("review_text") String reviewText,
            @Field("reviewer_name") String reviewerName
    );

    /**
     * Submit a content report (broken PDF, duplicate, inaccurate course mapping, copyright).
     */
    @FormUrlEncoded
    @POST("api/v1/resources/{id}/reports")
    Call<Map<String, Object>> submitReport(
            @Path("id") int resourceId,
            @Field("reason") String reason,
            @Field("details") String details,
            @Field("reporter_name") String reporterName
    );
}
