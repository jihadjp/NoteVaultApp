<?php
// ─────────────────────────────────────────────────────────────
// api/v1/reviews.php
// Endpoints:
//   GET  /api/v1/resources/{id}/reviews
//   POST /api/v1/resources/{id}/reviews
// Enforces 1-5 rating bounds, one active review per user/reviewer per resource,
// and server-side aggregate rating recalculation.
// ─────────────────────────────────────────────────────────────

function handleReviews(int $resourceId): void {
    $db = getDB();
    $method = $_SERVER['REQUEST_METHOD'];

    if ($method === 'GET') {
        $page   = max((int)($_GET['page'] ?? 1), 1);
        $limit  = min(max((int)($_GET['limit'] ?? 20), 1), 50);
        $offset = ($page - 1) * $limit;

        $stmt = $db->prepare("
            SELECT id, resource_id, reviewer_name, rating, review_text, created_at, updated_at
            FROM resource_reviews
            WHERE resource_id = :rid AND status = 'ACTIVE'
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
        ");
        $stmt->bindValue(':rid', $resourceId, PDO::PARAM_INT);
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();

        sendSuccess($stmt->fetchAll());
    }

    if ($method === 'POST') {
        $authUser = getAuthenticatedUser($db);
        $rating = (int)($_POST['rating'] ?? 0);
        $reviewText = trim($_POST['review_text'] ?? '');
        $reviewerName = $authUser ? $authUser['name'] : trim($_POST['reviewer_name'] ?? 'Student');
        $userId = $authUser ? (int)$authUser['id'] : 0;

        if ($rating < 1 || $rating > 5) {
            sendError('Rating must be an integer between 1 and 5.', 422);
        }
        if (mb_strlen($reviewText) > 1000) {
            sendError('Review text cannot exceed 1000 characters.', 422);
        }

        // Check resource exists
        $check = $db->prepare("SELECT id FROM notes WHERE id = :id LIMIT 1");
        $check->execute([':id' => $resourceId]);
        if (!$check->fetch()) {
            sendError('Resource not found.', 404);
        }

        // Upsert review (one active review per reviewer per resource)
        $stmt = $db->prepare("
            INSERT INTO resource_reviews (resource_id, user_id, reviewer_name, rating, review_text, status)
            VALUES (:rid, :uid, :rname, :rating, :rtext, 'ACTIVE')
            ON DUPLICATE KEY UPDATE
                rating = VALUES(rating),
                review_text = VALUES(review_text),
                updated_at = CURRENT_TIMESTAMP
        ");
        $stmt->execute([
            ':rid'    => $resourceId,
            ':uid'    => $userId,
            ':rname'  => $reviewerName !== '' ? $reviewerName : 'Student',
            ':rating' => $rating,
            ':rtext'  => $reviewText
        ]);

        // Recalculate aggregate rating on the server
        $agg = $db->prepare("
            SELECT COALESCE(AVG(rating), 0) AS avg_rating, COUNT(*) AS cnt
            FROM resource_reviews
            WHERE resource_id = :rid AND status = 'ACTIVE'
        ");
        $agg->execute([':rid' => $resourceId]);
        $stats = $agg->fetch();

        $upd = $db->prepare("
            UPDATE notes
            SET rating_avg = :avg, rating_count = :cnt
            WHERE id = :rid
        ");
        $upd->execute([
            ':avg' => round((float)$stats['avg_rating'], 2),
            ':cnt' => (int)$stats['cnt'],
            ':rid' => $resourceId
        ]);

        sendSuccess([
            'success'     => true,
            'message'     => 'Review recorded.',
            'ratingAvg'   => round((float)$stats['avg_rating'], 2),
            'ratingCount' => (int)$stats['cnt']
        ], 201);
    }

    sendError('Method not allowed.', 405);
}
