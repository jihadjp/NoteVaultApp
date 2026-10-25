<?php
// ─────────────────────────────────────────────────────────────
// config/helpers.php — CORS, JSON responses, Auth & Security Helpers
// ─────────────────────────────────────────────────────────────

function setCorsHeaders(): void {
    $allowedOrigin = getenv('CORS_ALLOWED_ORIGIN') ?: '*';
    header('Access-Control-Allow-Origin: ' . $allowedOrigin);
    header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');
    header('X-Content-Type-Options: nosniff');
    header('X-Frame-Options: DENY');
    header('Content-Type: application/json; charset=UTF-8');

    if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
        http_response_code(200);
        exit;
    }
}

function sendSuccess($data, int $code = 200): void {
    http_response_code($code);
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
    exit;
}

function sendError(string $message, int $code = 400): void {
    http_response_code($code);
    echo json_encode([
        'success' => false,
        'message' => $message
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

function requireMethod(string $method): void {
    if ($_SERVER['REQUEST_METHOD'] !== strtoupper($method)) {
        sendError('Method not allowed.', 405);
    }
}

/**
 * Extracts Bearer token from Authorization header.
 */
function getBearerToken(): ?string {
    $headers = function_exists('getallheaders') ? getallheaders() : [];
    $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? ($_SERVER['HTTP_AUTHORIZATION'] ?? '');
    if (preg_match('/Bearer\s+(\S+)/i', $authHeader, $matches)) {
        return $matches[1];
    }
    return null;
}

/**
 * Resolves the authenticated user from a Bearer token, or returns null for guest requests.
 */
function getAuthenticatedUser(PDO $db): ?array {
    $token = getBearerToken();
    if (!$token) {
        return null;
    }
    try {
        $stmt = $db->prepare("SELECT id, name, email, role, university_id, department_id FROM users WHERE api_token_hash = :hash AND is_active = 1 LIMIT 1");
        $stmt->execute([':hash' => hash('sha256', $token)]);
        $user = $stmt->fetch();
        return $user ?: null;
    } catch (PDOException $e) {
        return null;
    }
}

/**
 * Requires an authenticated user with one of the specified roles.
 */
function requireRole(PDO $db, array $allowedRoles): array {
    $user = getAuthenticatedUser($db);
    if (!$user) {
        sendError('Authentication required.', 401);
    }
    if (!in_array(strtoupper($user['role']), $allowedRoles, true)) {
        sendError('Insufficient permissions for this operation.', 403);
    }
    return $user;
}

function formatBytes(int $bytes): string {
    if ($bytes >= 1073741824) return round($bytes / 1073741824, 2) . ' GB';
    if ($bytes >= 1048576)    return round($bytes / 1048576, 2)    . ' MB';
    if ($bytes >= 1024)       return round($bytes / 1024, 2)       . ' KB';
    return $bytes . ' B';
}
