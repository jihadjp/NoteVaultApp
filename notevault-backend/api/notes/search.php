<?php
// ─────────────────────────────────────────────────────────────
// api/notes/search.php
// GET /api/notes/search?q=keyword
// ─────────────────────────────────────────────────────────────

if (!function_exists('formatNoteForResponse')) {
    require_once __DIR__ . '/index.php';
}

requireMethod('GET');

$query = trim($_GET['q'] ?? '');

if (strlen($query) < 1) {
    sendSuccess([]); // Return empty list if no query
    exit;
}

$db = getDB();

// Updated for diunotes format columns
$sql = "SELECT * FROM notes
        WHERE status = 'APPROVED'
          AND (
              title           LIKE :q
           OR course_code     LIKE :q
           OR course_name     LIKE :q
           OR department_name LIKE :q
           OR description     LIKE :q
           OR user_name       LIKE :q
          )
        ORDER BY created_at DESC
        LIMIT 50";

$likeQuery = '%' . $query . '%';

$stmt = $db->prepare($sql);
$stmt->execute([':q' => $likeQuery]);

$notes = $stmt->fetchAll();
$notes = array_map('formatNoteForResponse', $notes);

// NoteVault app expects a plain JSON array for List<NoteResponse>
sendSuccess($notes);
