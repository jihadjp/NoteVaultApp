package com.jptechgenius.notevault.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jptechgenius.notevault.R;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.databinding.ItemNoteCardBinding;
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
                    return oldItem.isDownloaded() == newItem.isDownloaded()
                            && oldItem.getDownloadCount() == newItem.getDownloadCount()
                            && oldItem.getTitle().equals(newItem.getTitle());
                }
            };

    public void setDownloadingNoteId(int noteId) {
        this.downloadingNoteId = noteId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNoteCardBinding binding = ItemNoteCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NoteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        NoteEntity note = getItem(position);
        holder.bind(note, listener, downloadingNoteId);
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        private final ItemNoteCardBinding binding;

        NoteViewHolder(@NonNull ItemNoteCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(NoteEntity note, NoteClickListener listener, int downloadingNoteId) {
            binding.tvTitle.setText(note.getTitle());
            binding.tvSubject.setText(note.getSubject() != null ? note.getSubject() : "General");
            binding.tvUploader.setText(note.getUploadedBy() != null
                    ? note.getUploadedBy() : "Anonymous");
            binding.tvFileSize.setText(FileUtils.formatSize(note.getFileSize()));
            binding.tvDownloadCount.setText(
                    FileUtils.formatDownloadCount(note.getDownloadCount()) + " downloads");

            // Load thumbnail with Glide
            Glide.with(itemView.getContext())
                    .load(note.getThumbnailUrl())
                    .placeholder(R.drawable.ic_pdf_placeholder)
                    .error(R.drawable.ic_pdf_placeholder)
                    .centerCrop()
                    .into(binding.ivThumbnail);

            // Downloaded state
            boolean isDownloaded = note.isDownloaded()
                    && FileUtils.fileExists(note.getLocalFilePath());
            boolean isDownloading = note.getId() == downloadingNoteId;

            if (isDownloaded) {
                binding.cardNote.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.colorSuccess));
                binding.cardNote.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorDownloaded));
                binding.tvDownloadedBadge.setVisibility(View.VISIBLE);
                binding.btnDownload.setVisibility(View.GONE);
                binding.btnDeleteFile.setVisibility(View.VISIBLE);
                binding.progressDownload.setVisibility(View.GONE);
            } else if (isDownloading) {
                binding.cardNote.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary));
                binding.cardNote.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorSurface));
                binding.tvDownloadedBadge.setVisibility(View.GONE);
                binding.btnDownload.setVisibility(View.GONE);
                binding.btnDeleteFile.setVisibility(View.GONE);
                binding.progressDownload.setVisibility(View.VISIBLE);
            } else {
                binding.cardNote.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.colorDivider));
                binding.cardNote.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.colorSurface));
                binding.tvDownloadedBadge.setVisibility(View.GONE);
                binding.btnDownload.setVisibility(View.VISIBLE);
                binding.btnDeleteFile.setVisibility(View.GONE);
                binding.progressDownload.setVisibility(View.GONE);
            }

            // Click listeners
            binding.btnViewPdf.setOnClickListener(v -> listener.onViewPDF(note));
            binding.btnDownload.setOnClickListener(v -> listener.onDownload(note));
            binding.btnDeleteFile.setOnClickListener(v -> listener.onDeleteFile(note));
            binding.btnMore.setOnClickListener(v -> listener.onMoreOptions(note, v));
            itemView.setOnClickListener(v -> listener.onViewPDF(note));
        }
    }
}
