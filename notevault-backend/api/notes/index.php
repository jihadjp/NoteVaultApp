<?php
// ─────────────────────────────────────────────────────────────
// api/notes/index.php
// GET /api/notes — returns all approved notes in diunotes format
// ─────────────────────────────────────────────────────────────

function handleIndex(): void {
    $db = getDB();

    // Optional filters
    $courseCode = $_GET['courseCode'] ?? null;
    $limit      = min((int)($_GET['limit']  ?? 50), 100);
    $offset     = max((int)($_GET['offset'] ?? 0),  0);

    // Base query - only show APPROVED notes
    $sql    = "SELECT * FROM notes WHERE status = 'APPROVED'";
    $params = [];

    if ($courseCode) {
        $sql .= " AND course_code = :courseCode";
        $params[':courseCode'] = $courseCode;
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
 * Formats database row to match the NoteResponse POJO (camelCase).
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
        'userName'              => $row['user_name'] ?? 'Anonymous',
        'createdAt'             => $row['created_at'],
        'updatedAt'             => $row['updated_at'],
        'userId'                => (int) ($row['user_id'] ?? 0),
        'likeCount'             => (int) $row['like_count'],
        'dislikeCount'          => (int) $row['dislike_count'],
        'likedByCurrentUser'    => false,
        'dislikedByCurrentUser' => false,
        'savedByCurrentUser'    => false,
        'viewCount'             => (int) $row['view_count'],
        'status'                => $row['status'],
        'approvedAt'            => $row['approved_at'],
        'thumbnail_url'         => $row['thumbnail_url'] ?? ''
    ];
}
