<?php
// ─────────────────────────────────────────────────────────────
// api/notes/index.php
// GET /api/notes and GET /api/v1/resources
// Returns approved academic resources in NoteResponse format.
// ─────────────────────────────────────────────────────────────

function handleIndex(): void {
    $db = getDB();

    $courseCode = trim($_GET['courseCode'] ?? ($_GET['course_code'] ?? ''));
    $department = trim($_GET['department'] ?? ($_GET['department_name'] ?? ''));
    $category   = trim($_GET['type']       ?? ($_GET['category'] ?? ''));
    $page       = max((int)($_GET['page']   ?? 1), 1);
    $limit      = min(max((int)($_GET['limit'] ?? 50), 1), 100);
    $offset     = isset($_GET['offset']) ? max((int)$_GET['offset'], 0) : (($page - 1) * $limit);

    // Public listing strictly returns APPROVED resources
    $sql    = "SELECT * FROM notes WHERE status = 'APPROVED'";
    $params = [];

    if ($courseCode !== '') {
        $sql .= " AND course_code = :courseCode";
        $params[':courseCode'] = $courseCode;
    }
    if ($department !== '') {
        $sql .= " AND department_name = :department";
        $params[':department'] = $department;
    }
    if ($category !== '') {
        $sql .= " AND resource_category = :category";
        $params[':category'] = $category;
    }

    $sql .= " ORDER BY created_at DESC LIMIT :limit OFFSET :offset";

    $stmt = $db->prepare($sql);
    foreach ($params as $key => $value) {
        $stmt->bindValue($key, $value);
    }
    $stmt->bindValue(':limit',  $limit,  PDO::PARAM_INT);
    $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);

    $stmt->execute();
    $notes = $stmt->fetchAll();

    $notes = array_map('formatNoteForResponse', $notes);
    sendSuccess($notes);
}

/**
 * Formats a database row to match the Android NoteResponse POJO (camelCase).
 * Maintains full backward compatibility with legacy clients and adds v1 academic hierarchy fields.
 */
function formatNoteForResponse(array $row): array {
    return [
        'id'                    => (int) $row['id'],
        'title'                 => $row['title'],
        'fileUrl'               => $row['file_url'],
        'description'           => $row['description'] ?? '',
        'courseId'              => (int) ($row['course_id'] ?? 0),
        'courseCode'            => $row['course_code'] ?? '',
        'courseName'            => $row['course_name'] ?? '',
        'departmentId'          => (int) ($row['department_id'] ?? 0),
        'departmentName'        => $row['department_name'] ?? '',
        'universityName'        => $row['university_name'] ?? 'Daffodil International University',
        'resourceCategory'      => $row['resource_category'] ?? 'Lecture Notes',
        'semesterName'          => $row['semester_name'] ?? '',
        'batchName'             => $row['batch_name'] ?? '',
        'userName'              => $row['user_name'] ?? 'Contributor',
        'createdAt'             => $row['created_at'],
        'updatedAt'             => $row['updated_at'],
        'userId'                => (int) ($row['user_id'] ?? 0),
        'likeCount'             => (int) ($row['like_count'] ?? 0),
        'dislikeCount'          => (int) ($row['dislike_count'] ?? 0),
        'likedByCurrentUser'    => false,
        'dislikedByCurrentUser' => false,
        'savedByCurrentUser'    => false,
        'viewCount'             => (int) ($row['view_count'] ?? 0),
        'fileSize'              => (int) ($row['file_size'] ?? 0),
        'status'                => $row['status'] ?? 'APPROVED',
        'isVerified'            => (bool) ($row['is_verified'] ?? ($row['status'] === 'APPROVED')),
        'ratingAvg'             => (float) ($row['rating_avg'] ?? 0.0),
        'ratingCount'           => (int) ($row['rating_count'] ?? 0),
        'approvedAt'            => $row['approved_at'] ?? null,
        'thumbnail_url'         => $row['thumbnail_url'] ?? ''
    ];
}
