<?php
// ─────────────────────────────────────────────────────────────
// api/v1/reports.php
// POST /api/v1/resources/{id}/reports
// Records reports for broken PDFs, inaccurate course mappings, duplicates, or copyright issues.
// ─────────────────────────────────────────────────────────────

function handleReports(int $resourceId): void {
    requireMethod('POST');
    $db = getDB();

    $authUser = getAuthenticatedUser($db);
    $reason = trim($_POST['reason'] ?? '');
    $details = trim($_POST['details'] ?? '');
    $reporterName = $authUser ? $authUser['name'] : trim($_POST['reporter_name'] ?? 'Student');
    $userId = $authUser ? (int)$authUser['id'] : 0;

    if ($reason === '' || mb_strlen($reason) > 255) {
        sendError('A valid report reason is required.', 422);
    }

    $check = $db->prepare("SELECT id FROM notes WHERE id = :id LIMIT 1");
    $check->execute([':id' => $resourceId]);
    if (!$check->fetch()) {
        sendError('Resource not found.', 404);
    }

    $stmt = $db->prepare("
        INSERT INTO resource_reports (resource_id, user_id, reporter_name, reason, details, status)
        VALUES (:rid, :uid, :rname, :reason, :details, 'OPEN')
    ");
    $stmt->execute([
        ':rid'     => $resourceId,
        ':uid'     => $userId,
        ':rname'   => $reporterName !== '' ? $reporterName : 'Student',
        ':reason'  => $reason,
        ':details' => mb_substr($details, 0, 1000)
    ]);

    sendSuccess([
        'success'   => true,
        'message'   => 'Report submitted to the moderation queue.',
        'report_id' => (int)$db->lastInsertId()
    ], 201);
}
