<?php
// ─────────────────────────────────────────────────────────────
// api/notes/delete.php
// DELETE /api/notes/{id}
// ─────────────────────────────────────────────────────────────

function handleDelete(int $id): void {
    $db = getDB();

    $check = $db->prepare("SELECT id FROM notes WHERE id = :id");
    $check->execute([':id' => $id]);

    if (!$check->fetch()) {
        sendError('Note not found.', 404);
    }

    // Set status to REJECTED (Soft delete)
    $stmt = $db->prepare("UPDATE notes SET status = 'REJECTED' WHERE id = :id");
    $stmt->execute([':id' => $id]);

    sendSuccess([
        'success' => true,
        'message' => 'Note removed successfully.',
        'id'      => $id,
    ]);
}
