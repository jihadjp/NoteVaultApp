package com.jptechgenius.notevault.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.jptechgenius.notevault.R;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.data.remote.model.QuestionResponse;
import com.jptechgenius.notevault.databinding.ActivityHomeBinding;
import com.jptechgenius.notevault.ui.detail.NoteDetailActivity;
import com.jptechgenius.notevault.utils.Constants;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NoteAdapter.NoteClickListener, QuestionAdapter.QuestionClickListener {

    private ActivityHomeBinding binding;
    private HomeViewModel viewModel;
    private NoteAdapter noteAdapter;
    private QuestionAdapter questionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        setupViewModel();
        setupTabs();
        setupSearch();
        setupFilters();

        // Initialize first tab state
        binding.layoutFilterNotes.getRoot().setVisibility(View.VISIBLE);
        binding.layoutFilterQuestions.getRoot().setVisibility(View.GONE);
    }

    private void initViews() {
        noteAdapter = new NoteAdapter(this);
        questionAdapter = new QuestionAdapter(this);
        
        binding.recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerNotes.setAdapter(noteAdapter);

        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (viewModel.currentTab.getValue() == HomeViewModel.Tab.QUESTIONS) {
                applyQuestionFilters();
            } else {
                viewModel.loadFromServer();
            }
        });
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Notes"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Questions"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Saved"));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Clear search when switching tabs
                binding.etSearch.setText("");

                switch (tab.getPosition()) {
                    case 0:
                        viewModel.setTab(HomeViewModel.Tab.NOTES);
                        binding.recyclerNotes.setAdapter(noteAdapter);
                        binding.layoutFilterNotes.getRoot().setVisibility(View.VISIBLE);
                        binding.layoutFilterQuestions.getRoot().setVisibility(View.GONE);
                        observeAllNotes();
                        break;
                    case 1:
                        viewModel.setTab(HomeViewModel.Tab.QUESTIONS);
                        binding.recyclerNotes.setAdapter(questionAdapter);
                        binding.layoutFilterNotes.getRoot().setVisibility(View.GONE);
                        binding.layoutFilterQuestions.getRoot().setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        viewModel.setTab(HomeViewModel.Tab.DOWNLOADED);
                        binding.recyclerNotes.setAdapter(noteAdapter);
                        binding.layoutFilterNotes.getRoot().setVisibility(View.GONE);
                        binding.layoutFilterQuestions.getRoot().setVisibility(View.GONE);
                        observeDownloadedNotes();
                        break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.isLoading.observe(this, loading -> {
            binding.swipeRefresh.setRefreshing(loading);
            binding.shimmerLayout.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.questions.observe(this, questions -> {
            if (viewModel.currentTab.getValue() == HomeViewModel.Tab.QUESTIONS) {
                questionAdapter.submitList(questions);
                updateEmptyState(questions.isEmpty());
            }
        });

        viewModel.toastMessage.observe(this, msg -> {
            if (msg != null) Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
        });

        observeAllNotes();
        viewModel.loadFromServer();
    }

    private void setupFilters() {
        // Sample Data for Dropdowns
        String[] depts = {"CSE", "EEE", "BBA", "English", "Civil"};
        String[] semesters = {"L1-T1", "L1-T2", "L2-T1", "L2-T2", "L3-T1", "L3-T2", "L4-T1", "L4-T2"};
        String[] examTypes = {"MID TERM", "FINAL TERM", "QUIZ", "LAB"};

        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, depts);
        ArrayAdapter<String> semAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, semesters);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, examTypes);

        // Questions Filter
        binding.layoutFilterQuestions.spinDept.setAdapter(deptAdapter);
        binding.layoutFilterQuestions.spinSemester.setAdapter(semAdapter);
        binding.layoutFilterQuestions.spinExamType.setAdapter(typeAdapter);

        binding.layoutFilterQuestions.spinDept.setOnItemClickListener((p, v, pos, id) -> applyQuestionFilters());
        binding.layoutFilterQuestions.spinSemester.setOnItemClickListener((p, v, pos, id) -> applyQuestionFilters());
        binding.layoutFilterQuestions.spinExamType.setOnItemClickListener((p, v, pos, id) -> applyQuestionFilters());
        binding.layoutFilterQuestions.spinCourse.setOnItemClickListener((p, v, pos, id) -> applyQuestionFilters());

        binding.layoutFilterQuestions.btnResetQuestions.setOnClickListener(v -> {
            binding.layoutFilterQuestions.spinDept.setText("", false);
            binding.layoutFilterQuestions.spinCourse.setText("", false);
            binding.layoutFilterQuestions.spinSemester.setText("", false);
            binding.layoutFilterQuestions.spinExamType.setText("", false);
            applyQuestionFilters();
        });

        // Notes Filter
        binding.layoutFilterNotes.filterDepartment.setAdapter(deptAdapter);
        binding.layoutFilterNotes.btnSearch.setOnClickListener(v -> {
            String title = binding.layoutFilterNotes.etFilterTitle.getText().toString();
            viewModel.searchNotes(title).observe(this, noteAdapter::submitList);
        });
        
        binding.layoutFilterNotes.btnReset.setOnClickListener(v -> {
            binding.layoutFilterNotes.etFilterTitle.setText("");
            binding.layoutFilterNotes.filterDepartment.setText("", false);
            observeAllNotes();
        });
    }

    private void applyQuestionFilters() {
        String dept = binding.layoutFilterQuestions.spinDept.getText().toString();
        String sem = binding.layoutFilterQuestions.spinSemester.getText().toString();
        String type = binding.layoutFilterQuestions.spinExamType.getText().toString();
        String course = binding.layoutFilterQuestions.spinCourse.getText().toString();
        
        viewModel.loadQuestions(dept, course, sem, type);
    }

    private void observeAllNotes() {
        viewModel.getAllNotes().removeObservers(this);
        viewModel.getAllNotes().observe(this, notes -> {
            if (viewModel.currentTab.getValue() == HomeViewModel.Tab.NOTES) {
                noteAdapter.submitList(notes);
                updateEmptyState(notes.isEmpty());
            }
        });
    }

    private void observeDownloadedNotes() {
        viewModel.getDownloadedNotes().removeObservers(this);
        viewModel.getDownloadedNotes().observe(this, notes -> {
            if (viewModel.currentTab.getValue() == HomeViewModel.Tab.DOWNLOADED) {
                noteAdapter.submitList(notes);
                updateEmptyState(notes.isEmpty());
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerNotes.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (viewModel.currentTab.getValue() == HomeViewModel.Tab.NOTES) {
                    viewModel.searchNotes(query).observe(HomeActivity.this, noteAdapter::submitList);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

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
        viewModel.deleteLocalFile(note);
    }

    @Override
    public void onDeleteNote(NoteEntity note) {}

    @Override
    public void onMoreOptions(NoteEntity note, View anchor) {}

    @Override
    public void onQuestionClick(QuestionResponse question) {
        if (question.getPdfLink() != null && !question.getPdfLink().isEmpty()) {
            String url = question.getPdfLink();
            if (!url.startsWith("http")) {
                url = Constants.BASE_URL + (url.startsWith("/") ? url.substring(1) : url);
            }
            
            Intent intent = new Intent(this, NoteDetailActivity.class);
            intent.putExtra(Constants.EXTRA_NOTE_TITLE, question.getCourseName());
            intent.putExtra(Constants.EXTRA_PDF_URL, url);
            startActivity(intent);
        } else {
            Snackbar.make(binding.getRoot(), "PDF link not available", Snackbar.LENGTH_SHORT).show();
        }
    }
}
