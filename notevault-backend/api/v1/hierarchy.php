<?php
// ─────────────────────────────────────────────────────────────
// api/v1/hierarchy.php
// Endpoints:
//   GET /api/v1/universities
//   GET /api/v1/departments?university_id={id}
//   GET /api/v1/courses?department_id={id}&semester={code}
// ─────────────────────────────────────────────────────────────

function handleUniversities(): void {
    requireMethod('GET');
    $db = getDB();
    try {
        $stmt = $db->query("SELECT id, code, name, short_name, country, is_active FROM universities WHERE is_active = 1 ORDER BY id ASC");
        sendSuccess($stmt->fetchAll());
    } catch (PDOException $e) {
        // Fallback if table not migrated yet
        sendSuccess([
            [
                'id' => 1,
                'code' => 'DIU',
                'name' => 'Daffodil International University',
                'short_name' => 'DIU',
                'country' => 'Bangladesh',
                'is_active' => 1
            ]
        ]);
    }
}

function handleDepartments(): void {
    requireMethod('GET');
    $db = getDB();
    $universityId = (int)($_GET['university_id'] ?? 1);
    try {
        $stmt = $db->prepare("SELECT id, university_id, code, name, faculty_name FROM departments WHERE university_id = :uid AND is_active = 1 ORDER BY code ASC");
        $stmt->execute([':uid' => $universityId]);
        sendSuccess($stmt->fetchAll());
    } catch (PDOException $e) {
        sendSuccess([
            ['id' => 1, 'university_id' => 1, 'code' => 'SWE', 'name' => 'Software Engineering', 'faculty_name' => 'FSIT'],
            ['id' => 2, 'university_id' => 1, 'code' => 'CSE', 'name' => 'Computer Science and Engineering', 'faculty_name' => 'FSIT'],
            ['id' => 3, 'university_id' => 1, 'code' => 'EEE', 'name' => 'Electrical and Electronic Engineering', 'faculty_name' => 'FE'],
            ['id' => 4, 'university_id' => 1, 'code' => 'BBA', 'name' => 'Business Administration', 'faculty_name' => 'FBE']
        ]);
    }
}

function handleCourses(): void {
    requireMethod('GET');
    $db = getDB();
    $departmentId = (int)($_GET['department_id'] ?? 0);
    $departmentCode = trim($_GET['department'] ?? '');
    $semester = trim($_GET['semester'] ?? '');

    try {
        $sql = "SELECT id, department_id, department_code, course_code, course_title, credit_hours, semester_name FROM courses WHERE 1=1";
        $params = [];
        if ($departmentId > 0) {
            $sql .= " AND department_id = :deptId";
            $params[':deptId'] = $departmentId;
        }
        if ($departmentCode !== '') {
            $sql .= " AND department_code = :deptCode";
            $params[':deptCode'] = $departmentCode;
        }
        if ($semester !== '') {
            $sql .= " AND semester_name = :sem";
            $params[':sem'] = $semester;
        }
        $sql .= " ORDER BY course_code ASC";
        $stmt = $db->prepare($sql);
        $stmt->execute($params);
        sendSuccess($stmt->fetchAll());
    } catch (PDOException $e) {
        sendSuccess([]);
    }
}
