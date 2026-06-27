package com.jptechgenius.notevault.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.jptechgenius.notevault.R;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.databinding.ActivityHomeBinding;
import com.jptechgenius.notevault.ui.detail.NoteDetailActivity;
import com.jptechgenius.notevault.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Main home screen.
 * Shows all notes (cached from server) and a "Downloaded" tab from Room.
 * Supports pull-to-refresh, search, shimmer loading, and offline banner.
 */
public class HomeActivity extends AppCompatActivity implements NoteAdapter.NoteClickListener {

    private ActivityHomeBinding binding;
    private HomeViewModel viewModel;
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("NoteVault DIU");
        }

        initViews();
        setupRecyclerView();
        setupTabs();
        setupSearch();
        setupSwipeRefresh();
        setupViewModel();
    }

    private void initViews() {
        binding.fabUpload.setOnClickListener(v -> {
            Snackbar.make(v, "Upload feature coming soon!", Snackbar.LENGTH_LONG).show();
        });
    }

    private void setupRecyclerView() {
        adapter = new NoteAdapter(this);
        binding.recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerNotes.setAdapter(adapter);
        binding.recyclerNotes.setHasFixedSize(true);
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("All Notes"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Downloaded"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    viewModel.setCurrentTab(HomeViewModel.Tab.ALL);
                    observeAllNotes();
                } else {
                    viewModel.setCurrentTab(HomeViewModel.Tab.DOWNLOADED);
                    observeDownloadedNotes();
                }
                binding.etSearch.setText(""); // Clear search on tab switch
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    // Restore tab data
                    if (viewModel.currentTab.getValue() == HomeViewModel.Tab.DOWNLOADED) {
                        observeDownloadedNotes();
                    } else {
                        observeAllNotes();
                    }
                } else {
                    observeSearchResults(query);
                }
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        binding.swipeRefresh.setOnRefreshListener(() -> viewModel.loadFromServer());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        // Loading state
        viewModel.isLoading.observe(this, loading -> {
            if (loading) {
                showShimmer();
            } else {
                hideShimmer();
                binding.swipeRefresh.setRefreshing(false);
            }
        });

        // Offline banner
        viewModel.isOffline.observe(this, offline ->
                binding.offlineBanner.setVisibility(offline ? View.VISIBLE : View.GONE));

        // Toast messages
        viewModel.toastMessage.observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.recyclerNotes, msg, Snackbar.LENGTH_SHORT).show();
            }
        });

        // Downloading note ID (for progress indicator in cards)
        viewModel.downloadingNoteId.observe(this, id -> {
            if (id != null) adapter.setDownloadingNoteId(id);
        });

        // Initial data load
        observeAllNotes();
        viewModel.loadFromServer();
    }

    // ─── Observation helpers ──────────────────────────────────────────────────

    private void observeAllNotes() {
        viewModel.getAllNotes().observe(this, this::submitNotes);
    }

    private void observeDownloadedNotes() {
        viewModel.getDownloadedNotes().observe(this, this::submitNotes);
    }

    private void observeSearchResults(String query) {
        viewModel.searchNotes(query).observe(this, this::submitNotes);
    }

    private void submitNotes(List<NoteEntity> notes) {
        boolean isEmpty = notes == null || notes.isEmpty();
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerNotes.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        adapter.submitList(notes != null ? notes : new ArrayList<>());
    }

    // ─── Shimmer ─────────────────────────────────────────────────────────────

    private void showShimmer() {
        binding.shimmerLayout.setVisibility(View.VISIBLE);
        binding.shimmerLayout.startShimmer();
        binding.recyclerNotes.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        binding.shimmerLayout.stopShimmer();
        binding.shimmerLayout.setVisibility(View.GONE);
        binding.recyclerNotes.setVisibility(View.VISIBLE);
    }

    // ─── NoteClickListener ────────────────────────────────────────────────────

    @Override
    public void onViewPDF(NoteEntity note) {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra(Constants.EXTRA_NOTE_ID, note.getId());
        intent.putExtra(Constants.EXTRA_NOTE_TITLE, note.getTitle());
        startActivity(intent);
    }

    @Override
    public void onDownload(NoteEntity note) {
        viewModel.downloadNote(note);
    }

    @Override
    public void onDeleteFile(NoteEntity note) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Offline File?")
                .setMessage("This will remove the PDF from your device.\n"
                        + "The note will still be available online.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Remove File", (d, w) ->
                        viewModel.deleteLocalFile(note))
                .show();
    }

    @Override
    public void onDeleteNote(NoteEntity note) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note?")
                .setMessage("This will permanently remove this note from your saved list.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete Note", (d, w) ->
                        viewModel.deleteNote(note))
                .show();
    }

    @Override
    public void onMoreOptions(NoteEntity note, View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, "View PDF");
        if (note.isDownloaded()) {
            popup.getMenu().add(0, 2, 0, "Remove Offline File");
        } else {
            popup.getMenu().add(0, 3, 0, "Download");
        }
        popup.getMenu().add(0, 4, 0, "Delete Note");

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1: onViewPDF(note); return true;
                case 2: onDeleteFile(note); return true;
                case 3: onDownload(note); return true;
                case 4: onDeleteNote(note); return true;
            }
            return false;
        });
        popup.show();
    }

    // ─── Options menu (bell + profile) ────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_notifications) {
            Snackbar.make(
                    binding.recyclerNotes, "Notifications coming soon", Snackbar.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_profile) {
            Snackbar.make(
                    binding.recyclerNotes, "Profile coming soon", Snackbar.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
