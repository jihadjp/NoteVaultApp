<?php
// ─────────────────────────────────────────────────────────────
// api/notes/delete.php
// DELETE /api/notes/{id}
//
// Soft delete — is_active = 0 করে।
// File physically delete হয় না।
// ─────────────────────────────────────────────────────────────

function handleDelete(int $id): void {
    $db = getDB();

    // Note আছে কিনা check
    $check = $db->prepare("SELECT id FROM notes WHERE id = :id AND is_active = 1");
    $check->execute([':id' => $id]);

    if (!$check->fetch()) {
        sendError('Note not found.', 404);
    }

    // Soft delete
    $stmt = $db->prepare("UPDATE notes SET is_active = 0 WHERE id = :id");
    $stmt->execute([':id' => $id]);

    sendSuccess([
        'success' => true,
        'message' => 'Note deleted successfully.',
        'id'      => $id,
    ]);
}
