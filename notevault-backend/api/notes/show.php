<?php
// ─────────────────────────────────────────────────────────────
// api/notes/show.php
// GET /api/notes/{id} — একটা specific note return করে
// ─────────────────────────────────────────────────────────────

function handleShow(int $id): void {
    $db = getDB();

    $stmt = $db->prepare(
        "SELECT * FROM notes WHERE id = :id AND is_active = 1"
    );
    $stmt->execute([':id' => $id]);
    $note = $stmt->fetch();

    if (!$note) {
        sendError('Note not found.', 404);
    }

    $note = castNoteTypes($note);
    sendSuccess($note);
}

// castNoteTypes index.php তে define হয়েছে
// কিন্তু এই file directly call হলে দরকার হবে
if (!function_exists('castNoteTypes')) {
    require_once __DIR__ . '/index.php';
}
