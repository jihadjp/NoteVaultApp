package com.jptechgenius.notevault;

import com.jptechgenius.notevault.data.local.entity.MyCourseEntity;
import com.jptechgenius.notevault.data.local.entity.NoteEntity;
import com.jptechgenius.notevault.data.local.entity.StudyTaskEntity;
import com.jptechgenius.notevault.data.remote.model.NoteResponse;
import com.jptechgenius.notevault.utils.FileUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for NoteVault Academic Platform upgrade models, category resolution,
 * exam countdown calculations, and PDF signature verification.
 */
public class NoteVaultUpgradeUnitTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void noteEntity_resolvesCategoryFromExplicitAndKeywordMetadata() {
        NoteEntity explicit = new NoteEntity();
        explicit.setTitle("Chapter 1 Notes");
        explicit.setResourceCategory("Official Syllabus");
        assertEquals("Official Syllabus", explicit.getResolvedCategory());

        NoteEntity slides = new NoteEntity();
        slides.setTitle("SWE211 Lecture Slides Week 3");
        assertEquals("Lecture Slides", slides.getResolvedCategory());

        NoteEntity syllabus = new NoteEntity();
        syllabus.setTitle("CSE1102 Course Outline & Syllabus");
        assertEquals("Official Syllabus", syllabus.getResolvedCategory());

        NoteEntity questions = new NoteEntity();
        questions.setTitle("Midterm Question Paper Spring 2025");
        assertEquals("Question Papers", questions.getResolvedCategory());

        NoteEntity suggestions = new NoteEntity();
        suggestions.setTitle("Final Exam Shortlist & Suggestion");
        assertEquals("Suggestions", suggestions.getResolvedCategory());

        NoteEntity lab = new NoteEntity();
        lab.setTitle("Database Lab Manual Experiment 4");
        assertEquals("Lab Manuals", lab.getResolvedCategory());

        NoteEntity defaultNote = new NoteEntity();
        defaultNote.setTitle("Numerical Methods Chapter 4");
        assertEquals("Lecture Notes", defaultNote.getResolvedCategory());
    }

    @Test
    public void studyTaskEntity_calculatesRemainingDaysAccurately() {
        long now = 1_700_000_000_000L;
        long fiveDaysLater = now + (5L * 24L * 60L * 60L * 1000L);

        StudyTaskEntity examTask = new StudyTaskEntity(
                "SWE211 Midterm Exam", "SWE211", "EXAM",
                fiveDaysLater, "Chapters 1-5", false, now
        );
        assertEquals(5L, examTask.getDaysRemaining(now));
        assertEquals(0L, examTask.getDaysRemaining(fiveDaysLater + 1000L));
    }

    @Test
    public void fileUtils_validatesPdfSignatureAndFormatsSizes() throws Exception {
        File validPdf = tempFolder.newFile("valid.pdf");
        try (FileOutputStream fos = new FileOutputStream(validPdf)) {
            fos.write("%PDF-1.7\nSample PDF content".getBytes(StandardCharsets.UTF_8));
        }
        assertTrue(FileUtils.isValidPdfFile(validPdf));

        File invalidFile = tempFolder.newFile("fake.pdf");
        try (FileOutputStream fos = new FileOutputStream(invalidFile)) {
            fos.write("<html>Not a PDF</html>".getBytes(StandardCharsets.UTF_8));
        }
        assertFalse(FileUtils.isValidPdfFile(invalidFile));

        assertEquals("PDF", FileUtils.formatSize(0));
        assertEquals("500 B", FileUtils.formatSize(500));
        assertEquals("1.2k", FileUtils.formatDownloadCount(1200));
        assertEquals("pdf", FileUtils.getFileExtension("notes_swe211.PDF"));
    }

    @Test
    public void myCourseAndNoteResponse_preserveAcademicHierarchyMetadata() {
        MyCourseEntity course = new MyCourseEntity(
                "SWE211", "Database Management Systems", "SWE",
                "Daffodil International University", "L2-T1", 3.0f, 123456L
        );
        assertEquals("SWE211", course.getCourseCode());
        assertEquals("SWE", course.getDepartmentName());

        NoteResponse response = new NoteResponse();
        response.setId(42);
        response.setTitle("DBMS Normalization Notes");
        response.setCourseCode("SWE211");
        response.setUniversityName("Daffodil International University");
        response.setResourceCategory("Lecture Notes");
        response.setVerified(true);
        response.setRatingAvg(4.8f);
        response.setRatingCount(15);

        assertEquals(42, response.getId());
        assertEquals("SWE211", response.getCourseCode());
        assertTrue(response.isVerified());
        assertEquals(4.8f, response.getRatingAvg(), 0.01f);
        assertEquals(15, response.getRatingCount());
    }
}
