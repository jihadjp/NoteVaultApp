<?php
// ─────────────────────────────────────────────────────────────
// api/questions/index.php
// GET /api/questions — returns filtered questions
// ─────────────────────────────────────────────────────────────

function handleQuestions(): void {
    $db = getDB();

    $dept     = $_GET['department'] ?? null;
    $course   = $_GET['course']     ?? null;
    $semester = $_GET['semester']   ?? null;
    $type     = $_GET['type']       ?? null;

    $sql = "SELECT * FROM all_questions WHERE 1=1";
    $params = [];

    if ($dept) {
        $sql .= " AND (department_name = :dept OR department_short = :dept)";
        $params[':dept'] = $dept;
    }
    if ($course) {
        $sql .= " AND course_name = :course";
        $params[':course'] = $course;
    }
    if ($semester) {
        $sql .= " AND semester_name = :semester";
        $params[':semester'] = $semester;
    }
    if ($type) {
        $sql .= " AND exam_type = :type";
        $params[':type'] = $type;
    }

    $sql .= " ORDER BY created_at DESC";

    $stmt = $db->prepare($sql);
    $stmt->execute($params);
    $results = $stmt->fetchAll();

    sendSuccess($results);
}
