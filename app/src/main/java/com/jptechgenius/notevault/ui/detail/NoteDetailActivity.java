package com.jptechgenius.notevault.ui.detail;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.jptechgenius.notevault.R;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.data.remote.RetrofitClient;
import com.jptechgenius.notevault.databinding.ActivityNoteDetailBinding;
import com.jptechgenius.notevault.databinding.BottomSheetNoteInfoBinding;
import com.jptechgenius.notevault.utils.AcademicPreferenceManager;
import com.jptechgenius.notevault.utils.Constants;
import com.jptechgenius.notevault.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Full-screen PDF viewer for a single academic resource or past exam paper.
 * Supports offline opening, remote streaming with PDF signature validation,
 * reading progress persistence & page resumption, bookmarking, rating/review, and issue reporting.
 */
public class NoteDetailActivity extends AppCompatActivity {

    private ActivityNoteDetailBinding binding;
    private NoteDetailViewModel viewModel;
    private AcademicPreferenceManager preferenceManager;
    private int noteId;
    private String directPdfUrl;
    private String currentlyLoadedSourceKey = null;
    private Call activeStreamCall = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferenceManager = new AcademicPreferenceManager(this);
        super.onCreate(savedInstanceState);
        binding = ActivityNoteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        noteId = getIntent().getIntExtra(Constants.EXTRA_NOTE_ID, -1);
        String noteTitle = getIntent().getStringExtra(Constants.EXTRA_NOTE_TITLE);
        directPdfUrl = getIntent().getStringExtra(Constants.EXTRA_PDF_URL);

        // Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(noteTitle != null ? noteTitle : "Academic Resource");
        }

        initViews();
        setupViewModel();

        // If it's a direct URL (like a Past Exam Question), load it directly
        if (directPdfUrl != null && !directPdfUrl.isEmpty() && noteId == -1) {
            updateActionBar(null);
            downloadAndDisplayPdf(directPdfUrl, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (activeStreamCall != null && !activeStreamCall.isCanceled()) {
            activeStreamCall.cancel();
        }
    }

    private void initViews() {
        binding.btnRetry.setOnClickListener(v -> {
            currentlyLoadedSourceKey = null;
            loadPdfForCurrentNote();
        });
        binding.btnBookmark.setOnClickListener(v -> viewModel.toggleBookmark());
        binding.btnShare.setOnClickListener(v -> shareNote());
        binding.btnInfo.setOnClickListener(v -> showInfoBottomSheet());
        binding.btnDownload.setOnClickListener(v -> viewModel.downloadNote());
        binding.btnDeleteFile.setOnClickListener(v -> confirmDeleteFile());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(NoteDetailViewModel.class);

        viewModel.getCurrentNote().observe(this, note -> {
            if (note != null) {
                updateActionBar(note);
                loadPdfIfNeeded(note);
            }
        });

        viewModel.isDownloading.observe(this, downloading -> {
            binding.progressDownloading.setVisibility(downloading ? View.VISIBLE : View.GONE);
            binding.btnDownload.setEnabled(!downloading);
        });

        viewModel.toastMessage.observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
            }
        });

        viewModel.isPdfLoading.observe(this, loading -> {
            binding.loadingLayout.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.pdfError.observe(this, error -> {
            if (error != null) {
                binding.tvErrorMessage.setText(error);
                binding.errorLayout.setVisibility(View.VISIBLE);
                binding.pdfView.setVisibility(View.GONE);
                binding.loadingLayout.setVisibility(View.GONE);
            }
        });

        viewModel.currentPage.observe(this, page -> updatePageIndicator());
        viewModel.totalPages.observe(this, total -> updatePageIndicator());

        if (noteId != -1) {
            viewModel.loadNote(noteId);
        }
    }

    private void updateActionBar(NoteEntity note) {
        if (note == null) {
            binding.btnDownload.setVisibility(View.GONE);
            binding.btnBookmark.setVisibility(View.GONE);
            binding.btnDeleteFile.setVisibility(View.GONE);
            binding.btnInfo.setVisibility(View.GONE);
            binding.btnShare.setVisibility(View.VISIBLE);
            return;
        }

        if (getSupportActionBar() != null && note.getTitle() != null) {
            getSupportActionBar().setTitle(note.getTitle());
        }

        boolean downloaded = note.isDownloaded() && FileUtils.fileExists(note.getLocalFilePath());
        binding.btnDownload.setVisibility(downloaded ? View.GONE : View.VISIBLE);
        binding.btnDeleteFile.setVisibility(downloaded ? View.VISIBLE : View.GONE);
        binding.btnBookmark.setVisibility(View.VISIBLE);
        binding.btnBookmark.setIconResource(note.isBookmarked()
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);
    }

    private void loadPdfIfNeeded(NoteEntity note) {
        boolean hasLocalFile = note.isDownloaded() && FileUtils.fileExists(note.getLocalFilePath());
        String sourceKey = hasLocalFile ? ("LOCAL:" + note.getLocalFilePath()) : ("REMOTE:" + note.getServerUrl());

        // Avoid reloading the entire PDF when bookmark or metadata updates in Room
        if (sourceKey.equals(currentlyLoadedSourceKey)) {
            return;
        }
        currentlyLoadedSourceKey = sourceKey;
        loadPdf(note);
    }

    private void loadPdf(NoteEntity note) {
        viewModel.setPdfLoading(true);
        binding.errorLayout.setVisibility(View.GONE);
        binding.pdfView.setVisibility(View.VISIBLE);

        int resumePageZeroIndexed = Math.max(0, note.getLastReadPage() - 1);
        boolean hasLocalFile = note.isDownloaded() && FileUtils.fileExists(note.getLocalFilePath());

        if (hasLocalFile) {
            displayPdfFromFile(new File(note.getLocalFilePath()), resumePageZeroIndexed);
        } else if (note.getServerUrl() != null && !note.getServerUrl().isEmpty()) {
            downloadAndDisplayPdf(note.getServerUrl(), resumePageZeroIndexed);
        } else {
            viewModel.setPdfError("No PDF source URL available for this resource.");
        }
    }

    private void downloadAndDisplayPdf(String url, int defaultPageZeroIndexed) {
        if (activeStreamCall != null && !activeStreamCall.isCanceled()) {
            activeStreamCall.cancel();
        }

        Request request;
        try {
            request = new Request.Builder().url(url).build();
        } catch (IllegalArgumentException e) {
            viewModel.setPdfError("Invalid PDF URL: " + url);
            return;
        }

        activeStreamCall = RetrofitClient.getHttpClient().newCall(request);
        activeStreamCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (call.isCanceled()) return;
                runOnUiThread(() -> viewModel.setPdfError(
                        "Offline or network unavailable. If you have downloaded this PDF previously, open it from the Downloads tab."));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> viewModel.setPdfError("Server returned HTTP " + response.code()));
                    return;
                }

                try (ResponseBody body = response.body()) {
                    if (body == null) {
                        runOnUiThread(() -> viewModel.setPdfError("Empty PDF response from server."));
                        return;
                    }

                    File tempFile = new File(getCacheDir(), "temp_view_" + (noteId != -1 ? noteId : "q") + ".pdf");
                    try (InputStream inputStream = body.byteStream();
                         FileOutputStream outputStream = new FileOutputStream(tempFile)) {

                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }
                        outputStream.flush();
                    }

                    if (!FileUtils.isValidPdfFile(tempFile)) {
                        tempFile.delete();
                        runOnUiThread(() -> viewModel.setPdfError("The remote file is not a valid PDF document."));
                        return;
                    }

                    runOnUiThread(() -> displayPdfFromFile(tempFile, defaultPageZeroIndexed));
                } catch (Exception e) {
                    if (!call.isCanceled()) {
                        runOnUiThread(() -> viewModel.setPdfError("Error reading PDF stream: " + e.getMessage()));
                    }
                }
            }
        });
    }

    private void displayPdfFromFile(File file, int defaultPageZeroIndexed) {
        if (!FileUtils.fileExists(file.getAbsolutePath())) {
            viewModel.setPdfError("Local PDF file is missing or incomplete.");
            return;
        }

        binding.pdfView.fromFile(file)
                .defaultPage(defaultPageZeroIndexed)
                .onLoad(nbPages -> {
                    int initialPage = Math.min(defaultPageZeroIndexed + 1, Math.max(1, nbPages));
                    viewModel.setPageInfo(initialPage, nbPages);
                    viewModel.setPdfLoading(false);
                    viewModel.saveReadingProgress(initialPage, nbPages);
                })
                .onPageChange((page, pageCount) -> {
                    viewModel.setPageInfo(page + 1, pageCount);
                    viewModel.saveReadingProgress(page + 1, pageCount);
                })
                .onError(t -> viewModel.setPdfError("Could not render PDF. The file may be corrupted."))
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(4)
                .load();
    }

    private void loadPdfForCurrentNote() {
        NoteEntity note = viewModel.getCurrentNote().getValue();
        if (note != null) {
            loadPdf(note);
        } else if (directPdfUrl != null && !directPdfUrl.isEmpty()) {
            downloadAndDisplayPdf(directPdfUrl, 0);
        }
    }

    private void updatePageIndicator() {
        Integer page = viewModel.currentPage.getValue();
        Integer total = viewModel.totalPages.getValue();
        if (page != null && total != null && total > 0) {
            binding.tvPageIndicator.setVisibility(View.VISIBLE);
            binding.tvPageIndicator.setText("Page " + page + " / " + total);
        } else {
            binding.tvPageIndicator.setVisibility(View.GONE);
        }
    }

    private void shareNote() {
        NoteEntity note = viewModel.getCurrentNote().getValue();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        if (note != null && note.isDownloaded() && FileUtils.fileExists(note.getLocalFilePath())) {
            File file = new File(note.getLocalFilePath());
            Uri uri = FileUtils.getShareableUri(this, file);
            shareIntent.setType(Constants.PDF_MIME_TYPE);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else if (note != null) {
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    note.getTitle() + " (" + (note.getCourseCode() != null ? note.getCourseCode() : "DIU") + ")\n"
                            + note.getServerUrl());
        } else if (directPdfUrl != null) {
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, directPdfUrl);
        } else {
            return;
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.btn_share)));
    }

    private void showInfoBottomSheet() {
        NoteEntity note = viewModel.getCurrentNote().getValue();
        if (note == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        BottomSheetNoteInfoBinding sheetBinding = BottomSheetNoteInfoBinding.inflate(getLayoutInflater());

        sheetBinding.tvInfoTitle.setText(note.getTitle() + " • [" + note.getResolvedCategory() + "]");
        sheetBinding.tvInfoSubject.setText(
                note.getSubject() != null && !note.getSubject().isEmpty() ? note.getSubject() : "—");
        sheetBinding.tvInfoCourseCode.setText(
                note.getCourseCode() != null && !note.getCourseCode().isEmpty() ? note.getCourseCode() : "—");
        String deptDisplay = (note.getDepartmentName() != null && !note.getDepartmentName().isEmpty()
                ? note.getDepartmentName() : "DIU")
                + (note.getUniversityName() != null ? " (" + note.getUniversityName() + ")" : "");
        sheetBinding.tvInfoDepartment.setText(deptDisplay);
        sheetBinding.tvInfoUploader.setText(
                (note.getUploadedBy() != null ? note.getUploadedBy() : "Anonymous")
                        + (note.isVerified() ? " ✓ Verified" : ""));
        sheetBinding.tvInfoDate.setText(
                note.getUploadedAt() != null ? note.getUploadedAt() : "—");
        sheetBinding.tvInfoSize.setText(
                FileUtils.formatSize(note.getFileSize()));
        sheetBinding.tvInfoDownloads.setText(
                FileUtils.formatDownloadCount(note.getDownloadCount()) + " views/downloads");

        String progressInfo = note.getTotalPages() > 0
                ? "Reading Progress: Page " + note.getLastReadPage() + " of " + note.getTotalPages() + "\n"
                : "";
        long totalOfflineBytes = FileUtils.getTotalDownloadedBytes(this);
        int totalOfflineFiles = FileUtils.getDownloadedFileCount(this);
        String storageSummary = "Device Offline Library: " + totalOfflineFiles + " PDFs ("
                + FileUtils.formatSize(totalOfflineBytes) + ")\n";

        if (note.isDownloaded() && FileUtils.fileExists(note.getLocalFilePath())) {
            sheetBinding.tvInfoLocalPath.setText(progressInfo + storageSummary + "Path: " + note.getLocalFilePath());
        } else {
            sheetBinding.tvInfoLocalPath.setText(progressInfo + storageSummary + "Status: Streaming online (Not saved offline)");
        }

        sheetBinding.btnCopyPath.setOnClickListener(v -> {
            String textToCopy = note.isDownloaded() && note.getLocalFilePath() != null
                    ? note.getLocalFilePath() : note.getServerUrl();
            if (textToCopy != null) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("Resource Path", textToCopy));
                    Snackbar.make(binding.getRoot(), "Copied to clipboard", Snackbar.LENGTH_SHORT).show();
                }
            }
            dialog.dismiss();
        });

        dialog.setContentView(sheetBinding.getRoot());
        dialog.show();
    }

    private void showRateReviewDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad / 2, pad, 0);

        RatingBar ratingBar = new RatingBar(this, null, android.R.attr.ratingBarStyleIndicator);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setRating(5.0f);
        ratingBar.setIsIndicator(false);
        container.addView(ratingBar);

        EditText etReview = new EditText(this);
        etReview.setHint("Write a helpful academic review (optional)…");
        etReview.setMinLines(2);
        container.addView(etReview);

        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_rate_review)
                .setView(container)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_submit, (d, w) -> {
                    int stars = Math.max(1, Math.round(ratingBar.getRating()));
                    String comment = etReview.getText().toString().trim();
                    viewModel.submitReview(stars, comment, preferenceManager.getStudentName());
                })
                .show();
    }

    private void showReportResourceDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad / 2, pad, 0);

        String[] reasons = {
                "Inaccurate course code / department mapping",
                "Corrupted or unreadable PDF pages",
                "Duplicate academic resource",
                "Unauthorized / copyrighted material",
                "Misleading title or content"
        };
        Spinner spinnerReason = new Spinner(this);
        spinnerReason.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, reasons));
        container.addView(spinnerReason);

        EditText etDetails = new EditText(this);
        etDetails.setHint("Provide additional details for moderators…");
        etDetails.setMinLines(2);
        container.addView(etDetails);

        new AlertDialog.Builder(this)
                .setTitle(R.string.btn_report_resource)
                .setView(container)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_submit, (d, w) -> {
                    String reason = reasons[spinnerReason.getSelectedItemPosition()];
                    String details = etDetails.getText().toString().trim();
                    viewModel.submitReport(reason, details, preferenceManager.getStudentName());
                })
                .show();
    }

    private void confirmDeleteFile() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_file_title)
                .setMessage(R.string.dialog_delete_file_message)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_remove_file, (d, w) -> viewModel.deleteLocalFile())
                .show();
    }

    private void confirmDeleteNote() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_note_title)
                .setMessage(R.string.dialog_delete_note_message)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_delete_note, (d, w) -> {
                    viewModel.deleteNote();
                    finish();
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_rate_review) {
            showRateReviewDialog();
            return true;
        } else if (id == R.id.action_report_resource) {
            showReportResourceDialog();
            return true;
        } else if (id == R.id.action_delete_note) {
            confirmDeleteNote();
            return true;
        } else if (id == R.id.action_copy_url) {
            NoteEntity note = viewModel.getCurrentNote().getValue();
            String url = note != null ? note.getServerUrl() : directPdfUrl;
            if (url != null) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("PDF URL", url);
                if (clipboard != null) clipboard.setPrimaryClip(clip);
                Snackbar.make(binding.getRoot(), "URL copied to clipboard", Snackbar.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
