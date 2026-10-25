package com.jptechgenius.notevault.ui.home;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.jptechgenius.notevault.R;
import com.jptechgenius.notevault.data.local.entity.MyCourseEntity;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.data.local.entity.StudyTaskEntity;
import com.jptechgenius.notevault.data.remote.model.QuestionResponse;
import com.jptechgenius.notevault.databinding.ActivityHomeBinding;
import com.jptechgenius.notevault.ui.detail.NoteDetailActivity;
import com.jptechgenius.notevault.utils.AcademicPreferenceManager;
import com.jptechgenius.notevault.utils.Constants;
import com.jptechgenius.notevault.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main academic dashboard and discovery activity for NoteVault DIU.
 * Implements 5 navigation destinations (Home, Explore, Downloads, Bookmarks, Profile),
 * University/Department context management, Continue Studying, Study Planner & Exam Countdown,
 * My Courses, smart debounced search, category filtering, and moderated student PDF contributions.
 */
public class HomeActivity extends AppCompatActivity
        implements NoteAdapter.NoteClickListener, QuestionAdapter.QuestionClickListener {

    private ActivityHomeBinding binding;
    private HomeViewModel viewModel;
    private NoteAdapter noteAdapter;
    private QuestionAdapter questionAdapter;
    private AcademicPreferenceManager preferenceManager;

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearchRunnable;

    private LiveData<List<NoteEntity>> activeNotesLiveData;
    private final Observer<List<NoteEntity>> activeNotesObserver = this::applyCategoryFilterAndSubmit;
    private List<NoteEntity> latestRawNotes = new ArrayList<>();

    private List<MyCourseEntity> cachedMyCourses = new ArrayList<>();
    private List<StudyTaskEntity> cachedTasks = new ArrayList<>();

    // Document picker state for student resource contribution
    private File selectedUploadPdfFile = null;
    private TextView tvSelectedPdfLabel = null;
    private final ActivityResultLauncher<String[]> pdfPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onPdfDocumentPicked);

    private static final String[] VERIFIED_UNIVERSITIES = {
            "Daffodil International University (DIU)",
            "Bangladesh University of Engineering and Technology (BUET)",
            "University of Dhaka (DU)",
            "North South University (NSU)",
            "BRAC University (BRACU)"
    };

    private static final String[] VERIFIED_DEPARTMENTS = {
            "All Departments", "SWE", "CSE", "EEE", "BBA", "NFE", "Pharmacy", "English", "Civil", "MCT"
    };

    private static final String[] VERIFIED_SEMESTERS = {
            "All Semesters", "L1-T1", "L1-T2", "L1-T3", "L2-T1", "L2-T2", "L2-T3",
            "L3-T1", "L3-T2", "L3-T3", "L4-T1", "L4-T2", "L4-T3"
    };

    private static final String[] VERIFIED_COURSES = {
            "SWE112 — Programming & Problem Solving",
            "SWE122 — Data Structures & Algorithms",
            "SWE132 — Object Oriented Programming",
            "SWE211 — Database Management Systems",
            "SWE223 — Software Engineering & System Analysis",
            "SWE231 — Operating Systems & System Programming",
            "SWE312 — Computer Networks & Security",
            "SWE322 — Artificial Intelligence & Machine Learning",
            "CSE1102 — Object Oriented Programming",
            "CSE2101 — Data Structures",
            "CSE226 — Numerical Methods",
            "CSE3101 — Database Systems"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferenceManager = new AcademicPreferenceManager(this);
        preferenceManager.applySavedThemeAndLocale(this);
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        initViews();
        setupViewModel();
        setupTabs();
        setupCategoryChips();
        setupSearch();
        setupFilters();
        refreshAcademicHeader();

        // Initialize first tab state
        binding.layoutFilterNotes.getRoot().setVisibility(View.VISIBLE);
        binding.layoutFilterQuestions.getRoot().setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pendingSearchRunnable != null) {
            searchHandler.removeCallbacks(pendingSearchRunnable);
        }
        detachActiveNotesObserver();
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

        binding.btnChangeContext.setOnClickListener(v -> showAcademicContextDialog());
        binding.tvAcademicContext.setOnClickListener(v -> showAcademicContextDialog());
        binding.cardProfileAvatar.setOnClickListener(v -> showAcademicContextDialog());

        binding.btnAddStudyTask.setOnClickListener(v -> showStudyPlannerDialog());
        binding.btnManageMyCourses.setOnClickListener(v -> showMyCoursesDialog());
        binding.fabUpload.setOnClickListener(v -> showContributeResourceDialog());
    }

    private void refreshAcademicHeader() {
        String studentName = preferenceManager.getStudentName();
        binding.tvGreetingName.setText(studentName);
        binding.tvAcademicContext.setText(preferenceManager.getContextSummaryLabel());

        String initials = "DS";
        if (studentName != null && !studentName.trim().isEmpty()) {
            String[] parts = studentName.trim().split("\\s+");
            if (parts.length >= 2) {
                initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.ROOT);
            } else if (parts[0].length() >= 2) {
                initials = parts[0].substring(0, 2).toUpperCase(Locale.ROOT);
            } else {
                initials = parts[0].toUpperCase(Locale.ROOT);
            }
        }
        binding.tvProfileInitials.setText(initials);
    }

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.nav_home));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.nav_explore));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.nav_downloads));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.nav_bookmarks));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.nav_profile));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.etSearch.setText("");

                switch (tab.getPosition()) {
                    case 0: // Home
                        viewModel.setTab(HomeViewModel.Tab.NOTES);
                        binding.recyclerNotes.setAdapter(noteAdapter);
                        binding.layoutFilterNotes.getRoot().setVisibility(View.VISIBLE);
                        binding.layoutFilterQuestions.getRoot().setVisibility(View.GONE);
                        binding.scrollCategories.setVisibility(View.VISIBLE);
                        binding.cardStudyWorkspace.setVisibility(View.VISIBLE);
                        observeNotesSource(viewModel.getAllNotes());
                        break;

                    case 1: // Explore (Past Questions & Course Explorer)
                        viewModel.setTab(HomeViewModel.Tab.QUESTIONS);
                        binding.recyclerNotes.setAdapter(questionAdapter);
                        binding.layoutFilterNotes.getRoot().setVisibility(View.GONE);
                        binding.layoutFilterQuestions.getRoot().setVisibility(View.VISIBLE);
                        binding.scrollCategories.setVisibility(View.GONE);
                        binding.cardStudyWorkspace.setVisibility(View.GONE);
                        break;

                    case 2: // Downloads (Offline PDFs)
                        viewModel.setTab(HomeViewModel.Tab.DOWNLOADED);
                        binding.recyclerNotes.setAdapter(noteAdapter);
                        binding.layoutFilterNotes.getRoot().setVisibility(View.GONE);
                        binding.layoutFilterQuestions.getRoot().setVisibility(View.GONE);
                        binding.scrollCategories.setVisibility(View.VISIBLE);
                        binding.cardStudyWorkspace.setVisibility(View.GONE);
                        observeNotesSource(viewModel.getDownloadedNotes());
                        break;

                    case 3: // Bookmarks
                        viewModel.setTab(HomeViewModel.Tab.BOOKMARKS);
                        binding.recyclerNotes.setAdapter(noteAdapter);
                        binding.layoutFilterNotes.getRoot().setVisibility(View.GONE);
                        binding.layoutFilterQuestions.getRoot().setVisibility(View.GONE);
                        binding.scrollCategories.setVisibility(View.VISIBLE);
                        binding.cardStudyWorkspace.setVisibility(View.VISIBLE);
                        observeNotesSource(viewModel.getBookmarkedNotes());
                        break;

                    case 4: // Profile & Academic Workspace
                        viewModel.setTab(HomeViewModel.Tab.PROFILE);
                        binding.recyclerNotes.setAdapter(noteAdapter);
                        binding.layoutFilterNotes.getRoot().setVisibility(View.GONE);
                        binding.layoutFilterQuestions.getRoot().setVisibility(View.GONE);
                        binding.scrollCategories.setVisibility(View.GONE);
                        binding.cardStudyWorkspace.setVisibility(View.VISIBLE);
                        observeNotesSource(viewModel.getBookmarkedNotes());
                        showAcademicContextDialog();
                        break;
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 4) {
                    showAcademicContextDialog();
                }
            }
        });
    }

    private void setupCategoryChips() {
        binding.chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                viewModel.setCategoryFilter("ALL");
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chipCatSyllabus) {
                viewModel.setCategoryFilter("Official Syllabus");
            } else if (id == R.id.chipCatLectureNotes) {
                viewModel.setCategoryFilter("Lecture Notes");
            } else if (id == R.id.chipCatSlides) {
                viewModel.setCategoryFilter("Lecture Slides");
            } else if (id == R.id.chipCatQuestions) {
                viewModel.setCategoryFilter("Question Papers");
            } else if (id == R.id.chipCatSuggestions) {
                viewModel.setCategoryFilter("Suggestions");
            } else if (id == R.id.chipCatLab) {
                viewModel.setCategoryFilter("Lab Manuals");
            } else if (id == R.id.chipCatAssignments) {
                viewModel.setCategoryFilter("Assignments");
            } else {
                viewModel.setCategoryFilter("ALL");
            }
        });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.isLoading.observe(this, loading -> {
            binding.swipeRefresh.setRefreshing(loading);
            binding.shimmerLayout.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        viewModel.isOffline.observe(this, offline -> {
            binding.offlineBanner.setVisibility(Boolean.TRUE.equals(offline) ? View.VISIBLE : View.GONE);
        });

        viewModel.downloadingNoteId.observe(this, id -> {
            noteAdapter.setDownloadingNoteId(id != null ? id : -1);
        });

        viewModel.selectedCategory.observe(this, category -> {
            applyCategoryFilterAndSubmit(latestRawNotes);
        });

        viewModel.questions.observe(this, questions -> {
            if (viewModel.currentTab.getValue() == HomeViewModel.Tab.QUESTIONS) {
                questionAdapter.submitList(questions);
                updateEmptyState(questions == null || questions.isEmpty());
            }
        });

        viewModel.getContinueStudyingNotes().observe(this, recentNotes -> {
            if (recentNotes != null && !recentNotes.isEmpty()) {
                NoteEntity recent = recentNotes.get(0);
                binding.cardContinueStudying.setVisibility(View.VISIBLE);
                binding.tvContinueTitle.setText(recent.getTitle());
                int page = Math.max(1, recent.getLastReadPage());
                int total = Math.max(page, recent.getTotalPages());
                binding.tvContinueSubtitle.setText(getString(R.string.continue_studying_resume, page, total));
                binding.btnResumeStudying.setOnClickListener(v -> onViewPDF(recent));
                binding.cardContinueStudying.setOnClickListener(v -> onViewPDF(recent));
            } else {
                binding.cardContinueStudying.setVisibility(View.GONE);
            }
        });

        viewModel.getUpcomingActiveTasks().observe(this, tasks -> {
            cachedTasks = tasks != null ? tasks : new ArrayList<>();
            updateWorkspaceSummary();
        });

        viewModel.getMyCourses().observe(this, courses -> {
            cachedMyCourses = courses != null ? courses : new ArrayList<>();
            updateWorkspaceSummary();
        });

        viewModel.toastMessage.observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
            }
        });

        observeNotesSource(viewModel.getAllNotes());
        viewModel.loadFromServer();
    }

    private void updateWorkspaceSummary() {
        StringBuilder sb = new StringBuilder();
        if (!cachedTasks.isEmpty()) {
            StudyTaskEntity next = cachedTasks.get(0);
            long days = next.getDaysRemaining(System.currentTimeMillis());
            String dateStr = DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(next.getTargetDateMillis()));
            sb.append("Next: ").append(next.getTitle());
            if (next.getCourseCode() != null && !next.getCourseCode().isEmpty()) {
                sb.append(" (").append(next.getCourseCode()).append(")");
            }
            sb.append(" — ").append(days).append(" days left (").append(dateStr).append(")");
            if (cachedTasks.size() > 1) {
                sb.append(" [+" + (cachedTasks.size() - 1) + " more]");
            }
        } else {
            sb.append(getString(R.string.exam_countdown_empty));
        }

        if (!cachedMyCourses.isEmpty()) {
            sb.append("\nMy Courses (").append(cachedMyCourses.size()).append("): ");
            for (int i = 0; i < Math.min(3, cachedMyCourses.size()); i++) {
                if (i > 0) sb.append(", ");
                sb.append(cachedMyCourses.get(i).getCourseCode());
            }
        }
        binding.tvExamCountdownSummary.setText(sb.toString());
    }

    private void observeNotesSource(LiveData<List<NoteEntity>> source) {
        detachActiveNotesObserver();
        activeNotesLiveData = source;
        activeNotesLiveData.observe(this, activeNotesObserver);
    }

    private void detachActiveNotesObserver() {
        if (activeNotesLiveData != null) {
            activeNotesLiveData.removeObserver(activeNotesObserver);
            activeNotesLiveData = null;
        }
    }

    private void applyCategoryFilterAndSubmit(List<NoteEntity> notes) {
        latestRawNotes = notes != null ? notes : new ArrayList<>();
        if (viewModel.currentTab.getValue() == HomeViewModel.Tab.QUESTIONS) {
            return;
        }

        String category = viewModel.selectedCategory.getValue();
        String savedDept = preferenceManager.getDepartment();
        boolean filterByContextDept = savedDept != null
                && !savedDept.isEmpty()
                && !AcademicPreferenceManager.DEFAULT_DEPARTMENT.equalsIgnoreCase(savedDept)
                && binding.layoutFilterNotes.filterDepartment.getText().toString().trim().isEmpty();

        List<NoteEntity> filtered = new ArrayList<>();
        for (NoteEntity note : latestRawNotes) {
            if (category != null && !"ALL".equalsIgnoreCase(category)) {
                if (!category.equalsIgnoreCase(note.getResolvedCategory())) {
                    continue;
                }
            }
            if (filterByContextDept && note.getDepartmentName() != null && !note.getDepartmentName().isEmpty()) {
                if (!note.getDepartmentName().toLowerCase(Locale.ROOT).contains(savedDept.toLowerCase(Locale.ROOT))) {
                    continue;
                }
            }
            filtered.add(note);
        }

        noteAdapter.submitList(filtered);
        updateEmptyState(filtered.isEmpty());
    }

    private void setupFilters() {
        String[] depts = {"SWE", "CSE", "EEE", "BBA", "NFE", "English", "Civil"};
        String[] semesters = {"L1-T1", "L1-T2", "L1-T3", "L2-T1", "L2-T2", "L2-T3", "L3-T1", "L3-T2", "L4-T1", "L4-T2"};
        String[] examTypes = {"MID TERM", "FINAL TERM", "QUIZ", "LAB"};

        ArrayAdapter<String> deptAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, depts);
        ArrayAdapter<String> semAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, semesters);
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, examTypes);
        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, VERIFIED_COURSES);

        // Questions Filter
        binding.layoutFilterQuestions.spinDept.setAdapter(deptAdapter);
        binding.layoutFilterQuestions.spinSemester.setAdapter(semAdapter);
        binding.layoutFilterQuestions.spinExamType.setAdapter(typeAdapter);
        binding.layoutFilterQuestions.spinCourse.setAdapter(courseAdapter);

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

        // Notes / Academic Explorer Filter
        binding.layoutFilterNotes.filterDepartment.setAdapter(deptAdapter);
        binding.layoutFilterNotes.filterCourse.setAdapter(courseAdapter);

        binding.layoutFilterNotes.btnSearch.setOnClickListener(v -> applyNoteFilters());
        binding.layoutFilterNotes.filterDepartment.setOnItemClickListener((p, view, pos, id) -> applyNoteFilters());
        binding.layoutFilterNotes.filterCourse.setOnItemClickListener((p, view, pos, id) -> applyNoteFilters());

        binding.layoutFilterNotes.btnReset.setOnClickListener(v -> {
            binding.layoutFilterNotes.etFilterTitle.setText("");
            binding.layoutFilterNotes.filterDepartment.setText("", false);
            binding.layoutFilterNotes.filterCourse.setText("", false);
            observeNotesSource(viewModel.getAllNotes());
        });
    }

    private void applyNoteFilters() {
        String dept = binding.layoutFilterNotes.filterDepartment.getText().toString().trim();
        String rawCourse = binding.layoutFilterNotes.filterCourse.getText().toString().trim();
        String course = rawCourse.contains("—") ? rawCourse.split("—")[0].trim() : rawCourse;
        String title = binding.layoutFilterNotes.etFilterTitle.getText() != null
                ? binding.layoutFilterNotes.etFilterTitle.getText().toString().trim() : "";

        if (!title.isEmpty()) {
            preferenceManager.addRecentSearch(title);
        }
        observeNotesSource(viewModel.filterNotes(dept, course, title));
    }

    private void applyQuestionFilters() {
        String dept = binding.layoutFilterQuestions.spinDept.getText().toString().trim();
        String sem = binding.layoutFilterQuestions.spinSemester.getText().toString().trim();
        String type = binding.layoutFilterQuestions.spinExamType.getText().toString().trim();
        String rawCourse = binding.layoutFilterQuestions.spinCourse.getText().toString().trim();
        String course = rawCourse.contains("—") ? rawCourse.split("—")[0].trim() : rawCourse;

        viewModel.loadQuestions(dept, course, sem, type);
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerNotes.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setupSearch() {
        binding.btnClearSearch.setOnClickListener(v -> {
            binding.etSearch.setText("");
            preferenceManager.clearRecentSearches();
        });

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString().trim() : "";
                binding.btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);

                if (pendingSearchRunnable != null) {
                    searchHandler.removeCallbacks(pendingSearchRunnable);
                }
                pendingSearchRunnable = () -> {
                    if (viewModel.currentTab.getValue() == HomeViewModel.Tab.QUESTIONS) {
                        viewModel.loadQuestions("", query, "", "");
                    } else if (query.isEmpty()) {
                        if (viewModel.currentTab.getValue() == HomeViewModel.Tab.DOWNLOADED) {
                            observeNotesSource(viewModel.getDownloadedNotes());
                        } else if (viewModel.currentTab.getValue() == HomeViewModel.Tab.BOOKMARKS) {
                            observeNotesSource(viewModel.getBookmarkedNotes());
                        } else {
                            observeNotesSource(viewModel.getAllNotes());
                        }
                    } else {
                        preferenceManager.addRecentSearch(query);
                        observeNotesSource(viewModel.searchNotes(query));
                    }
                };
                searchHandler.postDelayed(pendingSearchRunnable, Constants.SEARCH_DEBOUNCE_MS);
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // ─── Dialogs: Academic Context, My Courses, Study Planner, Resource Upload ──

    private void showAcademicContextDialog() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad / 2, pad, pad / 2);
        scrollView.addView(container);

        // Student Name
        EditText etStudentName = new EditText(this);
        etStudentName.setHint("Student Name");
        etStudentName.setText(preferenceManager.getStudentName());
        container.addView(createFieldLabel("Student Profile Name"));
        container.addView(etStudentName);

        // University Spinner
        Spinner spinUniversity = new Spinner(this);
        spinUniversity.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, VERIFIED_UNIVERSITIES));
        setSpinnerToValue(spinUniversity, VERIFIED_UNIVERSITIES, preferenceManager.getUniversity());
        container.addView(createFieldLabel(getString(R.string.label_university)));
        container.addView(spinUniversity);

        // Department Spinner
        Spinner spinDepartment = new Spinner(this);
        spinDepartment.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, VERIFIED_DEPARTMENTS));
        setSpinnerToValue(spinDepartment, VERIFIED_DEPARTMENTS, preferenceManager.getDepartment());
        container.addView(createFieldLabel(getString(R.string.label_department)));
        container.addView(spinDepartment);

        // Batch / Curriculum
        EditText etBatch = new EditText(this);
        etBatch.setHint("e.g. SWE-43 / CSE-64");
        etBatch.setText(preferenceManager.getBatch());
        container.addView(createFieldLabel(getString(R.string.label_batch)));
        container.addView(etBatch);

        // Semester Spinner
        Spinner spinSemester = new Spinner(this);
        spinSemester.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, VERIFIED_SEMESTERS));
        setSpinnerToValue(spinSemester, VERIFIED_SEMESTERS, preferenceManager.getSemester());
        container.addView(createFieldLabel(getString(R.string.label_semester)));
        container.addView(spinSemester);

        // Language Spinner (English / Bengali)
        String[] langs = {"English (EN)", "বাংলা (Bengali)"};
        Spinner spinLang = new Spinner(this);
        spinLang.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, langs));
        spinLang.setSelection("bn".equalsIgnoreCase(preferenceManager.getLanguage()) ? 1 : 0);
        container.addView(createFieldLabel(getString(R.string.label_language)));
        container.addView(spinLang);

        // Theme Spinner (System / Light / Dark)
        String[] themes = {"System Default", "Light Academic Surface", "Dark Emerald Theme"};
        Spinner spinTheme = new Spinner(this);
        spinTheme.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, themes));
        int currentMode = preferenceManager.getThemeMode();
        spinTheme.setSelection(currentMode == AppCompatDelegate.MODE_NIGHT_NO ? 1
                : (currentMode == AppCompatDelegate.MODE_NIGHT_YES ? 2 : 0));
        container.addView(createFieldLabel(getString(R.string.label_theme)));
        container.addView(spinTheme);

        // Storage usage summary
        long totalBytes = FileUtils.getTotalDownloadedBytes(this);
        int totalFiles = FileUtils.getDownloadedFileCount(this);
        TextView tvStorage = new TextView(this);
        tvStorage.setPadding(0, pad / 2, 0, 0);
        tvStorage.setText("Offline Storage: " + totalFiles + " PDFs (" + FileUtils.formatSize(totalBytes) + ")");
        container.addView(tvStorage);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_context_title)
                .setView(scrollView)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_save, (d, w) -> {
                    preferenceManager.setStudentName(etStudentName.getText().toString());
                    preferenceManager.setUniversity(VERIFIED_UNIVERSITIES[spinUniversity.getSelectedItemPosition()]);
                    preferenceManager.setDepartment(VERIFIED_DEPARTMENTS[spinDepartment.getSelectedItemPosition()]);
                    preferenceManager.setBatch(etBatch.getText().toString());
                    preferenceManager.setSemester(VERIFIED_SEMESTERS[spinSemester.getSelectedItemPosition()]);

                    int themeIndex = spinTheme.getSelectedItemPosition();
                    int targetTheme = themeIndex == 1 ? AppCompatDelegate.MODE_NIGHT_NO
                            : (themeIndex == 2 ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    preferenceManager.setThemeMode(targetTheme);

                    String newLang = spinLang.getSelectedItemPosition() == 1 ? "bn" : "en";
                    String oldLang = preferenceManager.getLanguage();
                    preferenceManager.setLanguage(this, newLang);

                    refreshAcademicHeader();
                    applyCategoryFilterAndSubmit(latestRawNotes);
                    if (!newLang.equalsIgnoreCase(oldLang)) {
                        recreate();
                    }
                })
                .show();
    }

    private void showMyCoursesDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad / 2, pad, 0);

        Spinner spinCourse = new Spinner(this);
        spinCourse.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, VERIFIED_COURSES));
        container.addView(createFieldLabel("Add Verified DIU Course to My Courses"));
        container.addView(spinCourse);

        StringBuilder currentList = new StringBuilder();
        if (cachedMyCourses.isEmpty()) {
            currentList.append("No personal courses saved yet.");
        } else {
            for (MyCourseEntity c : cachedMyCourses) {
                currentList.append("• ").append(c.getCourseCode()).append(" — ")
                        .append(c.getCourseTitle() != null ? c.getCourseTitle() : "").append("\n");
            }
        }
        TextView tvSaved = new TextView(this);
        tvSaved.setPadding(0, pad / 2, 0, 0);
        tvSaved.setText(currentList.toString().trim());
        container.addView(tvSaved);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.btn_manage_my_courses)
                .setView(container)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton("Add Course", (d, w) -> {
                    String selected = VERIFIED_COURSES[spinCourse.getSelectedItemPosition()];
                    String[] parts = selected.split("—");
                    String code = parts[0].trim();
                    String title = parts.length > 1 ? parts[1].trim() : selected;
                    MyCourseEntity entity = new MyCourseEntity(
                            code,
                            title,
                            preferenceManager.getDepartment(),
                            preferenceManager.getUniversity(),
                            preferenceManager.getSemester(),
                            3.0f,
                            System.currentTimeMillis()
                    );
                    viewModel.addMyCourse(entity);
                });

        if (!cachedMyCourses.isEmpty()) {
            builder.setNeutralButton("Clear Last Course", (d, w) -> {
                String code = cachedMyCourses.get(0).getCourseCode();
                viewModel.removeMyCourse(code);
            });
        }
        builder.show();
    }

    private void showStudyPlannerDialog() {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad / 2, pad, 0);

        EditText etTitle = new EditText(this);
        etTitle.setHint("e.g. Midterm Exam / Lab Report Submission");
        container.addView(createFieldLabel("Exam or Study Task Title"));
        container.addView(etTitle);

        EditText etCourse = new EditText(this);
        etCourse.setHint("Course Code (e.g. SWE211, CSE226)");
        container.addView(createFieldLabel("Course Code"));
        container.addView(etCourse);

        String[] taskTypes = {"EXAM", "ASSIGNMENT", "STUDY_TASK"};
        Spinner spinType = new Spinner(this);
        spinType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, taskTypes));
        container.addView(createFieldLabel("Category"));
        container.addView(spinType);

        final Calendar targetCal = Calendar.getInstance();
        targetCal.add(Calendar.DAY_OF_YEAR, 7);
        TextView tvDateBtn = new TextView(this);
        tvDateBtn.setPadding(0, pad / 2, 0, pad / 4);
        tvDateBtn.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        tvDateBtn.setText("Target Date: " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(targetCal.getTime()) + " (Tap to change)");
        tvDateBtn.setOnClickListener(v -> {
            new DatePickerDialog(this, (picker, y, m, d) -> {
                targetCal.set(Calendar.YEAR, y);
                targetCal.set(Calendar.MONTH, m);
                targetCal.set(Calendar.DAY_OF_MONTH, d);
                tvDateBtn.setText("Target Date: " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(targetCal.getTime()) + " (Tap to change)");
            }, targetCal.get(Calendar.YEAR), targetCal.get(Calendar.MONTH), targetCal.get(Calendar.DAY_OF_MONTH)).show();
        });
        container.addView(tvDateBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.exam_countdown_title)
                .setView(container)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_save, (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) {
                        Snackbar.make(binding.getRoot(), "Please enter an exam or task title", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    String course = etCourse.getText().toString().trim();
                    String type = taskTypes[spinType.getSelectedItemPosition()];
                    StudyTaskEntity task = new StudyTaskEntity(
                            title, course, type, targetCal.getTimeInMillis(),
                            "", false, System.currentTimeMillis()
                    );
                    viewModel.addStudyTask(task);
                });

        if (!cachedTasks.isEmpty()) {
            builder.setNeutralButton("Complete Next Task", (d, w) -> {
                viewModel.deleteStudyTask(cachedTasks.get(0).getId());
            });
        }
        builder.show();
    }

    private void showContributeResourceDialog() {
        selectedUploadPdfFile = null;

        ScrollView scrollView = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(pad, pad / 2, pad, pad / 2);
        scrollView.addView(container);

        EditText etTitle = new EditText(this);
        etTitle.setHint("Resource Title (required)");
        container.addView(createFieldLabel("Title"));
        container.addView(etTitle);

        Spinner spinCourse = new Spinner(this);
        spinCourse.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, VERIFIED_COURSES));
        container.addView(createFieldLabel("Course"));
        container.addView(spinCourse);

        String[] categories = {
                "Lecture Notes", "PDF Notes", "Lecture Slides", "Official Syllabus",
                "Question Papers", "Suggestions", "Lab Manuals", "Assignments"
        };
        Spinner spinCategory = new Spinner(this);
        spinCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories));
        container.addView(createFieldLabel("Resource Category"));
        container.addView(spinCategory);

        EditText etDesc = new EditText(this);
        etDesc.setHint("Short description, topics covered, or batch applicability");
        etDesc.setMinLines(2);
        container.addView(createFieldLabel("Description"));
        container.addView(etDesc);

        tvSelectedPdfLabel = new TextView(this);
        tvSelectedPdfLabel.setPadding(0, pad / 2, 0, pad / 4);
        tvSelectedPdfLabel.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
        tvSelectedPdfLabel.setText(getString(R.string.upload_select_pdf) + " [Tap here]");
        tvSelectedPdfLabel.setOnClickListener(v -> pdfPickerLauncher.launch(new String[]{Constants.PDF_MIME_TYPE}));
        container.addView(tvSelectedPdfLabel);

        TextView tvNotice = new TextView(this);
        tvNotice.setTextSize(11f);
        tvNotice.setText(R.string.upload_moderation_notice);
        container.addView(tvNotice);

        new AlertDialog.Builder(this)
                .setTitle(R.string.upload_dialog_title)
                .setView(scrollView)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_submit, (d, w) -> {
                    String title = etTitle.getText().toString().trim();
                    if (title.isEmpty()) {
                        Snackbar.make(binding.getRoot(), "Resource title is required.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    if (selectedUploadPdfFile == null || !selectedUploadPdfFile.exists()) {
                        Snackbar.make(binding.getRoot(), "Please select a valid PDF document first.", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    String selectedCourse = VERIFIED_COURSES[spinCourse.getSelectedItemPosition()];
                    String[] parts = selectedCourse.split("—");
                    String courseCode = parts[0].trim();
                    String courseName = parts.length > 1 ? parts[1].trim() : selectedCourse;
                    String category = categories[spinCategory.getSelectedItemPosition()];
                    String dept = preferenceManager.getDepartment();
                    if (AcademicPreferenceManager.DEFAULT_DEPARTMENT.equalsIgnoreCase(dept)) {
                        dept = courseCode.startsWith("SWE") ? "SWE" : "CSE";
                    }

                    viewModel.uploadResource(
                            title,
                            etDesc.getText().toString().trim(),
                            courseName,
                            courseCode,
                            dept,
                            preferenceManager.getUniversity(),
                            category,
                            preferenceManager.getSemester(),
                            preferenceManager.getStudentName(),
                            selectedUploadPdfFile
                    );
                })
                .show();
    }

    private void onPdfDocumentPicked(Uri uri) {
        if (uri == null) return;
        try {
            File tempUpload = new File(getCacheDir(), "upload_candidate_" + System.currentTimeMillis() + ".pdf");
            try (InputStream in = getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(tempUpload)) {
                if (in == null) return;
                byte[] buf = new byte[8192];
                int read;
                while ((read = in.read(buf)) != -1) {
                    out.write(buf, 0, read);
                }
                out.flush();
            }

            if (!FileUtils.isValidPdfFile(tempUpload)) {
                tempUpload.delete();
                Snackbar.make(binding.getRoot(), "Selected file is not a valid PDF document.", Snackbar.LENGTH_SHORT).show();
                return;
            }
            if (tempUpload.length() > 20L * 1024L * 1024L) {
                tempUpload.delete();
                Snackbar.make(binding.getRoot(), "PDF file exceeds the 20 MB maximum size limit.", Snackbar.LENGTH_SHORT).show();
                return;
            }

            selectedUploadPdfFile = tempUpload;
            if (tvSelectedPdfLabel != null) {
                tvSelectedPdfLabel.setText("Selected PDF (" + FileUtils.formatSize(tempUpload.length()) + ") ✓ Ready to submit");
            }
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(), "Could not read selected PDF: " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }

    private TextView createFieldLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12f);
        int topPad = (int) (10 * getResources().getDisplayMetrics().density);
        tv.setPadding(0, topPad, 0, 4);
        return tv;
    }

    private void setSpinnerToValue(Spinner spinner, String[] values, String target) {
        if (target == null) return;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equalsIgnoreCase(target)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    // ─── NoteAdapter & QuestionAdapter Callbacks ───────────────────────────────

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
                .setTitle(R.string.dialog_delete_file_title)
                .setMessage(R.string.dialog_delete_file_message)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_remove_file, (d, w) -> viewModel.deleteLocalFile(note))
                .show();
    }

    @Override
    public void onDeleteNote(NoteEntity note) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_note_title)
                .setMessage(R.string.dialog_delete_note_message)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_delete_note, (d, w) -> viewModel.deleteNote(note))
                .show();
    }

    @Override
    public void onMoreOptions(NoteEntity note, View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenu().add(0, 1, 0, note.isBookmarked() ? "Remove Bookmark ★" : getString(R.string.btn_bookmark));
        popup.getMenu().add(0, 2, 1, R.string.btn_share);
        if (note.isDownloaded()) {
            popup.getMenu().add(0, 3, 2, R.string.btn_remove_file);
        } else {
            popup.getMenu().add(0, 4, 2, R.string.btn_download);
        }
        popup.getMenu().add(0, 5, 3, R.string.btn_delete_note);

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == 1) {
                viewModel.toggleBookmark(note);
                return true;
            } else if (id == 2) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, note.getTitle() + "\n" + note.getServerUrl());
                startActivity(Intent.createChooser(shareIntent, getString(R.string.btn_share)));
                return true;
            } else if (id == 3) {
                onDeleteFile(note);
                return true;
            } else if (id == 4) {
                onDownload(note);
                return true;
            } else if (id == 5) {
                onDeleteNote(note);
                return true;
            }
            return false;
        });
        popup.show();
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            showAcademicContextDialog();
            return true;
        } else if (id == R.id.action_notifications) {
            showStudyPlannerDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
