<?php
// ─────────────────────────────────────────────────────────────
// api/v1/moderation.php
// Endpoints:
//   GET  /api/v1/moderation/queue          (Requires MODERATOR or ADMIN role)
//   POST /api/v1/moderation/{id}/decision  (Requires MODERATOR or ADMIN role)
// ─────────────────────────────────────────────────────────────

function handleModerationQueue(): void {
    requireMethod('GET');
    $db = getDB();
    requireRole($db, ['MODERATOR', 'ADMIN']);

    $status = strtoupper(trim($_GET['status'] ?? 'PENDING'));
    $allowedStatuses = ['PENDING', 'APPROVED', 'REJECTED', 'CHANGES_REQUESTED', 'REMOVED'];
    if (!in_array($status, $allowedStatuses, true)) {
        $status = 'PENDING';
    }

    $stmt = $db->prepare("SELECT * FROM notes WHERE status = :status ORDER BY created_at ASC LIMIT 100");
    $stmt->execute([':status' => $status]);
    $items = $stmt->fetchAll();

    $reportsStmt = $db->query("SELECT * FROM resource_reports WHERE status = 'OPEN' ORDER BY created_at DESC LIMIT 100");
    $reports = $reportsStmt ? $reportsStmt->fetchAll() : [];

    sendSuccess([
        'submissions' => $items,
        'openReports' => $reports
    ]);
}

function handleModerationDecision(int $resourceId): void {
    requireMethod('POST');
    $db = getDB();
    $moderator = requireRole($db, ['MODERATOR', 'ADMIN']);

    $decision = strtoupper(trim($_POST['decision'] ?? ''));
    $notes    = trim($_POST['moderator_notes'] ?? '');
    $verified = (int)($_POST['is_verified'] ?? ($decision === 'APPROVED' ? 1 : 0));

    $allowedDecisions = ['APPROVED', 'REJECTED', 'CHANGES_REQUESTED', 'REMOVED'];
    if (!in_array($decision, $allowedDecisions, true)) {
        sendError('Invalid moderation decision. Allowed: APPROVED, REJECTED, CHANGES_REQUESTED, REMOVED.', 422);
    }

    $check = $db->prepare("SELECT id, status FROM notes WHERE id = :id LIMIT 1");
    $check->execute([':id' => $resourceId]);
    $existing = $check->fetch();
    if (!$existing) {
        sendError('Resource not found.', 404);
    }

    $approvedAt = ($decision === 'APPROVED') ? date('Y-m-d H:i:s') : null;
    $upd = $db->prepare("
        UPDATE notes
        SET status = :status, is_verified = :verified, approved_at = :approved_at
        WHERE id = :id
    ");
    $upd->execute([
        ':status'      => $decision,
        ':verified'    => $verified,
        ':approved_at' => $approvedAt,
        ':id'          => $resourceId
    ]);

    // Record audit log
    $log = $db->prepare("
        INSERT INTO moderation_logs (resource_id, moderator_id, previous_status, new_status, notes)
        VALUES (:rid, :mid, :prev, :next, :notes)
    ");
    $log->execute([
        ':rid'   => $resourceId,
        ':mid'   => (int)$moderator['id'],
        ':prev'  => $existing['status'],
        ':next'  => $decision,
        ':notes' => $notes
    ]);

    sendSuccess([
        'success'     => true,
        'message'     => 'Moderation decision applied and logged.',
        'resource_id' => $resourceId,
        'new_status'  => $decision
    ]);
}
