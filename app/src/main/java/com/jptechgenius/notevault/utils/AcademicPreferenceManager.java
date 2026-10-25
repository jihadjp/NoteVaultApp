package com.jptechgenius.notevault.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Manages persistent student academic context (University, Department, Batch, Semester),
 * UI preferences (Language: English/Bengali, Theme: System/Light/Dark),
 * recent search history, and student session identity.
 */
public final class AcademicPreferenceManager {

    private static final String KEY_UNIVERSITY = "pref_university";
    private static final String KEY_DEPARTMENT = "pref_department";
    private static final String KEY_BATCH = "pref_batch";
    private static final String KEY_SEMESTER = "pref_semester";
    private static final String KEY_STUDENT_NAME = "pref_student_name";
    private static final String KEY_STUDENT_ROLE = "pref_student_role";
    private static final String KEY_AUTH_TOKEN = "pref_auth_token";
    private static final String KEY_LANGUAGE = "pref_language";
    private static final String KEY_THEME_MODE = "pref_theme_mode";
    private static final String KEY_RECENT_SEARCHES = "pref_recent_searches";

    public static final String DEFAULT_UNIVERSITY = "Daffodil International University (DIU)";
    public static final String DEFAULT_DEPARTMENT = "All Departments";
    public static final String DEFAULT_BATCH = "SWE-43 / Regular";
    public static final String DEFAULT_SEMESTER = "All Semesters";

    private final SharedPreferences prefs;

    public AcademicPreferenceManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getUniversity() {
        return prefs.getString(KEY_UNIVERSITY, DEFAULT_UNIVERSITY);
    }

    public void setUniversity(String university) {
        prefs.edit().putString(KEY_UNIVERSITY,
                university != null && !university.trim().isEmpty() ? university.trim() : DEFAULT_UNIVERSITY).apply();
    }

    public String getDepartment() {
        return prefs.getString(KEY_DEPARTMENT, DEFAULT_DEPARTMENT);
    }

    public void setDepartment(String department) {
        prefs.edit().putString(KEY_DEPARTMENT,
                department != null && !department.trim().isEmpty() ? department.trim() : DEFAULT_DEPARTMENT).apply();
    }

    public String getBatch() {
        return prefs.getString(KEY_BATCH, DEFAULT_BATCH);
    }

    public void setBatch(String batch) {
        prefs.edit().putString(KEY_BATCH,
                batch != null && !batch.trim().isEmpty() ? batch.trim() : DEFAULT_BATCH).apply();
    }

    public String getSemester() {
        return prefs.getString(KEY_SEMESTER, DEFAULT_SEMESTER);
    }

    public void setSemester(String semester) {
        prefs.edit().putString(KEY_SEMESTER,
                semester != null && !semester.trim().isEmpty() ? semester.trim() : DEFAULT_SEMESTER).apply();
    }

    public String getStudentName() {
        return prefs.getString(KEY_STUDENT_NAME, "DIU Scholar");
    }

    public void setStudentName(String name) {
        prefs.edit().putString(KEY_STUDENT_NAME,
                name != null && !name.trim().isEmpty() ? name.trim() : "DIU Scholar").apply();
    }

    public String getStudentRole() {
        return prefs.getString(KEY_STUDENT_ROLE, "STUDENT");
    }

    public void setStudentRole(String role) {
        prefs.edit().putString(KEY_STUDENT_ROLE, role != null ? role : "STUDENT").apply();
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, "");
    }

    public void setAuthToken(String token) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token != null ? token : "").apply();
    }

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, "en");
    }

    public void setLanguage(Context context, String langCode) {
        String code = ("bn".equalsIgnoreCase(langCode)) ? "bn" : "en";
        prefs.edit().putString(KEY_LANGUAGE, code).apply();
        applyLocale(context, code);
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public void applySavedThemeAndLocale(Context context) {
        AppCompatDelegate.setDefaultNightMode(getThemeMode());
        applyLocale(context, getLanguage());
    }

    public static void applyLocale(Context context, String langCode) {
        LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(langCode);
        AppCompatDelegate.setApplicationLocales(appLocale);
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    public List<String> getRecentSearches() {
        String raw = prefs.getString(KEY_RECENT_SEARCHES, "");
        if (raw == null || raw.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(raw.split("\\|\\|")));
    }

    public void addRecentSearch(String query) {
        if (query == null || query.trim().length() < 2) return;
        String clean = query.trim().replace("||", " ");
        List<String> list = getRecentSearches();
        list.remove(clean);
        list.add(0, clean);
        if (list.size() > 8) {
            list = list.subList(0, 8);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append("||");
            sb.append(list.get(i));
        }
        prefs.edit().putString(KEY_RECENT_SEARCHES, sb.toString()).apply();
    }

    public void clearRecentSearches() {
        prefs.edit().remove(KEY_RECENT_SEARCHES).apply();
    }

    public String getContextSummaryLabel() {
        String uniShort = getUniversity().contains("DIU") || getUniversity().contains("Daffodil")
                ? "DIU" : getUniversity();
        String dept = getDepartment();
        String batch = getBatch();
        return uniShort + " • " + dept + " • " + batch;
    }
}
