package com.jptechgenius.notevault.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.jptechgenius.notevault.R;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.databinding.ActivityNoteDetailBinding;
import com.jptechgenius.notevault.databinding.BottomSheetNoteInfoBinding;
import com.jptechgenius.notevault.utils.Constants;
import com.jptechgenius.notevault.utils.FileUtils;

import java.io.File;

/**
 * Full-screen PDF viewer for a single note.
 * Loads from local file if downloaded, otherwise streams from server URL.
 */
public class NoteDetailActivity extends AppCompatActivity {

    private ActivityNoteDetailBinding binding;
    private NoteDetailViewModel viewModel;
    private int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        noteId = getIntent().getIntExtra(Constants.EXTRA_NOTE_ID, -1);
        String noteTitle = getIntent().getStringExtra(Constants.EXTRA_NOTE_TITLE);

        // Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(noteTitle != null ? noteTitle : "Note");
        }

        initViews();
        setupViewModel();
    }

    private void initViews() {
        binding.btnRetry.setOnClickListener(v -> loadPdfForCurrentNote());
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
                loadPdf(note);
            }
        });

        viewModel.isDownloading.observe(this, downloading -> {
            binding.progressDownloading.setVisibility(downloading ? View.VISIBLE : View.GONE);
            binding.btnDownload.setEnabled(!downloading);
        });

        viewModel.toastMessage.observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.pdfView, msg, Snackbar.LENGTH_SHORT).show();
            }
        });

        viewModel.isPdfLoading.observe(this, loading -> {
            binding.loadingLayout.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.pdfError.observe(this, error -> {
            if (error != null) {
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
        if (getSupportActionBar() != null && note.getTitle() != null) {
            getSupportActionBar().setTitle(note.getTitle());
        }
        // Show/hide action buttons based on download state
        boolean downloaded = note.isDownloaded() && FileUtils.fileExists(note.getLocalFilePath());
        binding.btnDownload.setVisibility(downloaded ? View.GONE : View.VISIBLE);
        binding.btnDeleteFile.setVisibility(downloaded ? View.VISIBLE : View.GONE);
    }

    private void loadPdf(NoteEntity note) {
        viewModel.setPdfLoading(true);
        binding.errorLayout.setVisibility(View.GONE);
        binding.pdfView.setVisibility(View.VISIBLE);

        boolean hasLocalFile = note.isDownloaded()
                && FileUtils.fileExists(note.getLocalFilePath());

        if (hasLocalFile) {
            // Load from local file
            displayPdfFromFile(new File(note.getLocalFilePath()));
        } else if (note.getServerUrl() != null) {
            // Download to cache and show
            downloadAndDisplayPdf(note.getServerUrl());
        } else {
            viewModel.setPdfError("No PDF source available.");
        }
    }

    private void downloadAndDisplayPdf(String url) {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> viewModel.setPdfError("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (!response.isSuccessful()) {
                    runOnUiThread(() -> viewModel.setPdfError("Server error: " + response.code()));
                    return;
                }

                try (okhttp3.ResponseBody body = response.body()) {
                    if (body == null) {
                        runOnUiThread(() -> viewModel.setPdfError("Empty response from server"));
                        return;
                    }

                    File tempFile = new File(getCacheDir(), "temp_view.pdf");
                    try (java.io.InputStream inputStream = body.byteStream();
                         java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile)) {
                        
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, read);
                        }
                        outputStream.flush();
                    }

                    runOnUiThread(() -> displayPdfFromFile(tempFile));
                } catch (Exception e) {
                    runOnUiThread(() -> viewModel.setPdfError("Error saving PDF: " + e.getMessage()));
                }
            }
        });
    }

    private void displayPdfFromFile(File file) {
        binding.pdfView.fromFile(file)
                .onLoad(nbPages -> {
                    viewModel.setPageInfo(1, nbPages);
                    viewModel.setPdfLoading(false);
                })
                .onPageChange((page, pageCount) ->
                        viewModel.setPageInfo(page + 1, pageCount))
                .onError(t -> viewModel.setPdfError(
                        "Could not load PDF. File may be corrupted."))
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(4)
                .load();
    }

    private void loadPdfForCurrentNote() {
        NoteEntity note = viewModel.getCurrentNote().getValue();
        if (note != null) loadPdf(note);
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
        if (note == null) return;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);

        if (note.isDownloaded() && FileUtils.fileExists(note.getLocalFilePath())) {
            File file = new File(note.getLocalFilePath());
            Uri uri = FileUtils.getShareableUri(this, file);
            shareIntent.setType(Constants.PDF_MIME_TYPE);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    note.getTitle() + "\n" + note.getServerUrl());
        }

        startActivity(Intent.createChooser(shareIntent, "Share PDF via"));
    }

    private void showInfoBottomSheet() {
        NoteEntity note = viewModel.getCurrentNote().getValue();
        if (note == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        BottomSheetNoteInfoBinding sheetBinding = BottomSheetNoteInfoBinding.inflate(getLayoutInflater());

        sheetBinding.tvInfoTitle.setText(note.getTitle());
        sheetBinding.tvInfoSubject.setText(
                note.getSubject() != null ? note.getSubject() : "—");
        sheetBinding.tvInfoUploader.setText(
                note.getUploadedBy() != null ? note.getUploadedBy() : "—");
        sheetBinding.tvInfoDate.setText(
                note.getUploadedAt() != null ? note.getUploadedAt() : "—");
        sheetBinding.tvInfoSize.setText(
                FileUtils.formatSize(note.getFileSize()));
        sheetBinding.tvInfoDownloads.setText(
                FileUtils.formatDownloadCount(note.getDownloadCount()) + " downloads");

        if (note.isDownloaded() && note.getLocalFilePath() != null) {
            sheetBinding.tvInfoLocalPath.setText(note.getLocalFilePath());
        } else {
            sheetBinding.tvInfoLocalPath.setText("Not downloaded");
        }

        dialog.setContentView(sheetBinding.getRoot());
        dialog.show();
    }

    private void confirmDeleteFile() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Offline File?")
                .setMessage("This will remove the PDF from your device.\n"
                        + "The note will still be available online.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove File", (d, w) -> viewModel.deleteLocalFile())
                .show();
    }

    private void confirmDeleteNote() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note?")
                .setMessage("This will permanently remove this note from your saved list.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete Note", (d, w) -> {
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
        } else if (id == R.id.action_delete_note) {
            confirmDeleteNote();
            return true;
        } else if (id == R.id.action_copy_url) {
            NoteEntity note = viewModel.getCurrentNote().getValue();
            if (note != null && note.getServerUrl() != null) {
                android.content.ClipboardManager clipboard =
                        (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip =
                        android.content.ClipData.newPlainText("PDF URL", note.getServerUrl());
                if (clipboard != null) clipboard.setPrimaryClip(clip);
                Snackbar.make(
                        binding.pdfView, "URL copied to clipboard", Snackbar.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
