package com.jptechgenius.notevault.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jptechgenius.notevault.R;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.utils.FileUtils;

/**
 * RecyclerView adapter for note cards on the home screen.
 * Uses ListAdapter + DiffUtil for efficient incremental updates.
 */
public class NoteAdapter extends ListAdapter<NoteEntity, NoteAdapter.NoteViewHolder> {

    private final NoteClickListener listener;
    private int downloadingNoteId = -1;

    public interface NoteClickListener {
        void onViewPDF(NoteEntity note);
        void onDownload(NoteEntity note);
        void onDeleteFile(NoteEntity note);
        void onDeleteNote(NoteEntity note);
        void onMoreOptions(NoteEntity note, View anchor);
    }

    public NoteAdapter(NoteClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<NoteEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<NoteEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull NoteEntity oldItem,
                                               @NonNull NoteEntity newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull NoteEntity oldItem,
                                                  @NonNull NoteEntity newItem) {
                    if (oldItem.isDownloaded() != newItem.isDownloaded()) return false;
                    String oldPath = oldItem.getLocalFilePath();
                    String newPath = newItem.getLocalFilePath();
                    if (oldPath == null && newPath == null) return true;
                    if (oldPath == null || newPath == null) return false;
                    return oldPath.equals(newPath);
                }
            };

    public void setDownloadingNoteId(int noteId) {
        this.downloadingNoteId = noteId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note_card, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteEntity note = getItem(position);
        holder.bind(note, listener, downloadingNoteId);
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardView;
        private final ImageView ivThumbnail;
        private final TextView tvTitle;
        private final TextView tvSubject;
        private final TextView tvUploader;
        private final TextView tvFileSize;
        private final TextView tvDownloadCount;
        private final TextView tvDownloadedBadge;
        private final Button btnViewPdf;
        private final Button btnDownload;
        private final Button btnDeleteFile;
        private final ImageButton btnMore;
        private final ProgressBar progressDownload;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardNote);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvUploader = itemView.findViewById(R.id.tvUploader);
            tvFileSize = itemView.findViewById(R.id.tvFileSize);
            tvDownloadCount = itemView.findViewById(R.id.tvDownloadCount);
            tvDownloadedBadge = itemView.findViewById(R.id.tvDownloadedBadge);
            btnViewPdf = itemView.findViewById(R.id.btnViewPdf);
            btnDownload = itemView.findViewById(R.id.btnDownload);
            btnDeleteFile = itemView.findViewById(R.id.btnDeleteFile);
            btnMore = itemView.findViewById(R.id.btnMore);
            progressDownload = itemView.findViewById(R.id.progressDownload);
        }

        void bind(NoteEntity note, NoteClickListener listener, int downloadingNoteId) {
            tvTitle.setText(note.getTitle());
            tvSubject.setText(note.getSubject() != null ? note.getSubject() : "");
            tvUploader.setText(note.getUploadedBy() != null
                    ? "By " + note.getUploadedBy() : "");
            tvFileSize.setText(FileUtils.formatSize(note.getFileSize()));
            tvDownloadCount.setText(
                    FileUtils.formatDownloadCount(note.getDownloadCount()) + " downloads");

            // Load thumbnail with Glide
            Glide.with(itemView.getContext())
                    .load(note.getThumbnailUrl())
                    .placeholder(R.drawable.ic_pdf_placeholder)
                    .error(R.drawable.ic_pdf_placeholder)
                    .centerCrop()
                    .into(ivThumbnail);

            // Downloaded state
            boolean isDownloaded = note.isDownloaded()
                    && FileUtils.fileExists(note.getLocalFilePath());
            boolean isDownloading = note.getId() == downloadingNoteId;

            if (isDownloaded) {
                cardView.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.colorDownloaded));
                tvDownloadedBadge.setVisibility(View.VISIBLE);
                btnDownload.setVisibility(View.GONE);
                btnDeleteFile.setVisibility(View.VISIBLE);
                progressDownload.setVisibility(View.GONE);
            } else if (isDownloading) {
                cardView.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.colorSurface));
                tvDownloadedBadge.setVisibility(View.GONE);
                btnDownload.setVisibility(View.GONE);
                btnDeleteFile.setVisibility(View.GONE);
                progressDownload.setVisibility(View.VISIBLE);
            } else {
                cardView.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.colorSurface));
                tvDownloadedBadge.setVisibility(View.GONE);
                btnDownload.setVisibility(View.VISIBLE);
                btnDeleteFile.setVisibility(View.GONE);
                progressDownload.setVisibility(View.GONE);
            }

            // Click listeners
            btnViewPdf.setOnClickListener(v -> listener.onViewPDF(note));
            btnDownload.setOnClickListener(v -> listener.onDownload(note));
            btnDeleteFile.setOnClickListener(v -> listener.onDeleteFile(note));
            btnMore.setOnClickListener(v -> listener.onMoreOptions(note, v));
            itemView.setOnClickListener(v -> listener.onViewPDF(note));
        }
    }
}
