<?php
// ─────────────────────────────────────────────────────────────
// api/notes/index.php
// GET /api/notes — সব active notes return করে
//
// Query params (optional):
//   ?subject=CSE2101    → subject filter
//   ?limit=20           → কতটা দেখাবে (default 50)
//   ?offset=0           → pagination
// ─────────────────────────────────────────────────────────────

function handleIndex(): void {
    $db = getDB();

    // Optional filters
    $subject = $_GET['subject'] ?? null;
    $limit   = min((int)($_GET['limit']  ?? 50), 100); // max 100
    $offset  = max((int)($_GET['offset'] ?? 0),  0);

    // Base query
    $sql    = "SELECT * FROM notes WHERE is_active = 1";
    $params = [];

    // Subject filter
    if ($subject) {
        $sql     .= " AND subject = :subject";
        $params[':subject'] = $subject;
    }

    $sql .= " ORDER BY created_at DESC LIMIT :limit OFFSET :offset";

    $stmt = $db->prepare($sql);

    // Named params bind করো
    foreach ($params as $key => $value) {
        $stmt->bindValue($key, $value);
    }
    $stmt->bindValue(':limit',  $limit,  PDO::PARAM_INT);
    $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);

    $stmt->execute();
    $notes = $stmt->fetchAll();

    // int/float cast করো — JSON তে string হিসেবে না যায়
    $notes = array_map('castNoteTypes', $notes);

    sendSuccess($notes);
}

/**
 * DB থেকে আসা string values গুলো সঠিক type এ cast করো।
 * Android এর NoteResponse POJO এর সাথে match করতে হবে।
 */
function castNoteTypes(array $note): array {
    $note['id']             = (int)   $note['id'];
    $note['file_size']      = (int)   $note['file_size'];
    $note['download_count'] = (int)   $note['download_count'];
    $note['is_active']      = (bool)  $note['is_active'];

    // Android এ null safe করো
    $note['description']    = $note['description']    ?? '';
    $note['subject']        = $note['subject']        ?? '';
    $note['uploaded_by']    = $note['uploaded_by']    ?? '';
    $note['thumbnail_url']  = $note['thumbnail_url']  ?? '';

    // Android এর NoteResponse এ দরকার নেই এগুলো
    unset($note['created_at'], $note['updated_at'], $note['is_active']);

    return $note;
}
