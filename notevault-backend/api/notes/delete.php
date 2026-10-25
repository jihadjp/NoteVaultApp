<?php
// ─────────────────────────────────────────────────────────────
// api/notes/delete.php
// DELETE /api/notes/{id}
// Enforces server-side authentication and ownership / moderator authorization.
// Prevents arbitrary unauthenticated users from deleting other students' resources.
// ─────────────────────────────────────────────────────────────

function handleDelete(int $id): void {
    requireMethod('DELETE');
    $db = getDB();

    $user = getAuthenticatedUser($db);
    if (!$user) {
        sendError('Authentication required to remove or withdraw a resource.', 401);
    }

    $check = $db->prepare("SELECT id, user_id, status FROM notes WHERE id = :id LIMIT 1");
    $check->execute([':id' => $id]);
    $note = $check->fetch();

    if (!$note) {
        sendError('Resource not found.', 404);
    }

    $role = strtoupper($user['role'] ?? 'STUDENT');
    $isOwner = ((int)$note['user_id'] > 0) && ((int)$note['user_id'] === (int)$user['id']);
    $isModeratorOrAdmin = in_array($role, ['MODERATOR', 'ADMIN'], true);

    if (!$isOwner && !$isModeratorOrAdmin) {
        sendError('You are not authorized to delete another user\'s resource.', 403);
    }

    $stmt = $db->prepare("UPDATE notes SET status = 'REJECTED' WHERE id = :id");
    $stmt->execute([':id' => $id]);

    sendSuccess([
        'success' => true,
        'message' => 'Resource withdrawn/removed.',
        'id'      => $id,
    ]);
}
