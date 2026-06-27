<?php
// ─────────────────────────────────────────────────────────────
// api/notes/download.php
// POST /api/notes/{id}/download
// ─────────────────────────────────────────────────────────────

function handleDownload(int $id): void {
    $db = getDB();

    $check = $db->prepare("SELECT id FROM notes WHERE id = :id");
    $check->execute([':id' => $id]);

    if (!$check->fetch()) {
        sendError('Note not found.', 404);
    }

    // view_count used for tracking downloads in diunotes format
    $stmt = $db->prepare(
        "UPDATE notes SET view_count = view_count + 1 WHERE id = :id"
    );
    $stmt->execute([':id' => $id]);

    $countStmt = $db->prepare("SELECT view_count FROM notes WHERE id = :id");
    $countStmt->execute([':id' => $id]);
    $row = $countStmt->fetch();

    sendSuccess([
        'success'   => true,
        'message'   => 'View count updated.',
        'viewCount' => (int)$row['view_count'],
    ]);
}
