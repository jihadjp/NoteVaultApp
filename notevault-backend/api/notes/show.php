<?php
// ─────────────────────────────────────────────────────────────
// api/notes/show.php
// GET /api/notes/{id} — returns a specific note
// ─────────────────────────────────────────────────────────────

function handleShow(int $id): void {
    $db = getDB();

    $stmt = $db->prepare(
        "SELECT * FROM notes WHERE id = :id"
    );
    $stmt->execute([':id' => $id]);
    $note = $stmt->fetch();

    if (!$note) {
        sendError('Note not found.', 404);
    }

    if (!function_exists('formatNoteForResponse')) {
        require_once __DIR__ . '/index.php';
    }

    sendSuccess(formatNoteForResponse($note));
}
