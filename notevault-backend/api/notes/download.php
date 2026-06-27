<?php
// ─────────────────────────────────────────────────────────────
// api/notes/download.php
// POST /api/notes/{id}/download
//
// Android app যখন PDF download করে, এই endpoint call করবে।
// download_count এক এক করে বাড়বে।
// ─────────────────────────────────────────────────────────────

function handleDownload(int $id): void {
    $db = getDB();

    // Note আছে কিনা check করো
    $check = $db->prepare("SELECT id FROM notes WHERE id = :id AND is_active = 1");
    $check->execute([':id' => $id]);

    if (!$check->fetch()) {
        sendError('Note not found.', 404);
    }

    // download_count বাড়াও
    $stmt = $db->prepare(
        "UPDATE notes SET download_count = download_count + 1 WHERE id = :id"
    );
    $stmt->execute([':id' => $id]);

    // Updated count return করো
    $countStmt = $db->prepare("SELECT download_count FROM notes WHERE id = :id");
    $countStmt->execute([':id' => $id]);
    $row = $countStmt->fetch();

    sendSuccess([
        'success'        => true,
        'message'        => 'Download count updated.',
        'download_count' => (int)$row['download_count'],
    ]);
}
