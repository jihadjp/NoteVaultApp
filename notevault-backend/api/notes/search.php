<?php
// ─────────────────────────────────────────────────────────────
// api/notes/search.php
// GET /api/notes/search?q=keyword
//
// Title, subject, description, uploaded_by তে search করে।
// Android এর Room searchNotes() এর server-side version।
// ─────────────────────────────────────────────────────────────

require_once __DIR__ . '/index.php'; // castNoteTypes এর জন্য

requireMethod('GET');

$query = trim($_GET['q'] ?? '');

if (strlen($query) < 1) {
    sendError('Search query "q" is required.', 400);
}

$db = getDB();

// LIKE search — title, subject, description, uploaded_by
$sql = "SELECT * FROM notes
        WHERE is_active = 1
          AND (
              title       LIKE :q
           OR subject     LIKE :q
           OR description LIKE :q
           OR uploaded_by LIKE :q
          )
        ORDER BY
            CASE
                WHEN title LIKE :q_exact THEN 0   -- exact match আগে
                WHEN title LIKE :q       THEN 1
                ELSE 2
            END,
            download_count DESC
        LIMIT 50";

$likeQuery = '%' . $query . '%';

$stmt = $db->prepare($sql);
$stmt->bindValue(':q',       $likeQuery);
$stmt->bindValue(':q_exact', $query . '%');
$stmt->execute();

$notes = $stmt->fetchAll();
$notes = array_map('castNoteTypes', $notes);

sendSuccess([
    'query'   => $query,
    'count'   => count($notes),
    'results' => $notes
]);
