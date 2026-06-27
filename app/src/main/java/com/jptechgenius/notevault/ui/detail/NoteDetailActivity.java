package com.jptechgenius.notevault.ui.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.jptechgenius.notevault.R;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.utils.Constants;
import com.jptechgenius.notevault.utils.FileUtils;

import java.io.File;

/**
 * Full-screen PDF viewer for a single note.
 * Loads from local file if downloaded, otherwise streams from server URL.
 */
public class NoteDetailActivity extends AppCompatActivity {

    private NoteDetailViewModel viewModel;
    private PDFView pdfView;
    private ProgressBar progressBar;
    private TextView tvLoadingText;
    private View errorLayout;
    private TextView tvError;
    private Button btnRetry;
    private Button btnDownload;
    private Button btnDeleteFile;
    private Button btnShare;
    private Button btnInfo;
    private TextView tvPageIndicator;
    private ProgressBar progressDownloading;

    private int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        noteId = getIntent().getIntExtra(Constants.EXTRA_NOTE_ID, -1);
        String noteTitle = getIntent().getStringExtra(Constants.EXTRA_NOTE_TITLE);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(noteTitle != null ? noteTitle : "Note");
        }

        initViews();
        setupViewModel();
    }

    private void initViews() {
        pdfView = findViewById(R.id.pdfView);
        progressBar = findViewById(R.id.progressBar);
        tvLoadingText = findViewById(R.id.tvLoadingText);
        errorLayout = findViewById(R.id.errorLayout);
        tvError = findViewById(R.id.tvError);
        btnRetry = findViewById(R.id.btnRetry);
        btnDownload = findViewById(R.id.btnDownload);
        btnDeleteFile = findViewById(R.id.btnDeleteFile);
        btnShare = findViewById(R.id.btnShare);
        btnInfo = findViewById(R.id.btnInfo);
        tvPageIndicator = findViewById(R.id.tvPageIndicator);
        progressDownloading = findViewById(R.id.progressDownloading);

        btnRetry.setOnClickListener(v -> loadPdfForCurrentNote());
        btnShare.setOnClickListener(v -> shareNote());
        btnInfo.setOnClickListener(v -> showInfoBottomSheet());
        btnDownload.setOnClickListener(v -> viewModel.downloadNote());
        btnDeleteFile.setOnClickListener(v -> confirmDeleteFile());
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
            progressDownloading.setVisibility(downloading ? View.VISIBLE : View.GONE);
            btnDownload.setEnabled(!downloading);
        });

        viewModel.toastMessage.observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(pdfView, msg, Snackbar.LENGTH_SHORT).show();
            }
        });

        viewModel.isPdfLoading.observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            tvLoadingText.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.pdfError.observe(this, error -> {
            if (error != null) {
                errorLayout.setVisibility(View.VISIBLE);
                tvError.setText(error);
                pdfView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                tvLoadingText.setVisibility(View.GONE);
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
        btnDownload.setVisibility(downloaded ? View.GONE : View.VISIBLE);
        btnDeleteFile.setVisibility(downloaded ? View.VISIBLE : View.GONE);
    }

    private void loadPdf(NoteEntity note) {
        viewModel.setPdfLoading(true);
        errorLayout.setVisibility(View.GONE);
        pdfView.setVisibility(View.VISIBLE);

        boolean hasLocalFile = note.isDownloaded()
                && FileUtils.fileExists(note.getLocalFilePath());

        if (hasLocalFile) {
            // Load from local file
            pdfView.fromFile(new File(note.getLocalFilePath()))
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
        } else if (note.getServerUrl() != null) {
            // Stream from server URL via URI
            pdfView.fromUri(Uri.parse(note.getServerUrl()))
                    .onLoad(nbPages -> {
                        viewModel.setPageInfo(1, nbPages);
                        viewModel.setPdfLoading(false);
                    })
                    .onPageChange((page, pageCount) ->
                            viewModel.setPageInfo(page + 1, pageCount))
                    .onError(t -> viewModel.setPdfError(
                            "Could not load PDF. Check your connection."))
                    .scrollHandle(new DefaultScrollHandle(this))
                    .spacing(4)
                    .load();
        } else {
            viewModel.setPdfError("No PDF source available.");
        }
    }

    private void loadPdfForCurrentNote() {
        NoteEntity note = viewModel.getCurrentNote().getValue();
        if (note != null) loadPdf(note);
    }

    private void updatePageIndicator() {
        Integer page = viewModel.currentPage.getValue();
        Integer total = viewModel.totalPages.getValue();
        if (page != null && total != null && total > 0) {
            tvPageIndicator.setVisibility(View.VISIBLE);
            tvPageIndicator.setText("Page " + page + " / " + total);
        } else {
            tvPageIndicator.setVisibility(View.GONE);
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
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_note_info, null);

        ((TextView) sheetView.findViewById(R.id.tvInfoTitle)).setText(note.getTitle());
        ((TextView) sheetView.findViewById(R.id.tvInfoSubject)).setText(
                note.getSubject() != null ? note.getSubject() : "—");
        ((TextView) sheetView.findViewById(R.id.tvInfoUploader)).setText(
                note.getUploadedBy() != null ? note.getUploadedBy() : "—");
        ((TextView) sheetView.findViewById(R.id.tvInfoDate)).setText(
                note.getUploadedAt() != null ? note.getUploadedAt() : "—");
        ((TextView) sheetView.findViewById(R.id.tvInfoSize)).setText(
                FileUtils.formatSize(note.getFileSize()));
        ((TextView) sheetView.findViewById(R.id.tvInfoDownloads)).setText(
                FileUtils.formatDownloadCount(note.getDownloadCount()) + " downloads");
        ((TextView) sheetView.findViewById(R.id.tvInfoServerUrl)).setText(
                note.getServerUrl() != null ? note.getServerUrl() : "—");

        TextView tvLocalPath = sheetView.findViewById(R.id.tvInfoLocalPath);
        if (note.isDownloaded() && note.getLocalFilePath() != null) {
            tvLocalPath.setText(note.getLocalFilePath());
        } else {
            tvLocalPath.setText("Not downloaded");
        }

        dialog.setContentView(sheetView);
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
                        pdfView, "URL copied to clipboard", Snackbar.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
