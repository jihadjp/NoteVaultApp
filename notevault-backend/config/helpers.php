<?php
// ─────────────────────────────────────────────────────────────
// config/helpers.php — CORS headers + JSON response helpers
// ─────────────────────────────────────────────────────────────

/**
 * CORS headers set করো।
 * Android app যেকোনো IP থেকে call করতে পারবে।
 */
function setCorsHeaders(): void {
    header('Access-Control-Allow-Origin: *');
    header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
    header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');
    header('Content-Type: application/json; charset=UTF-8');

    // OPTIONS preflight request handle করো
    if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
        http_response_code(200);
        exit;
    }
}

/**
 * Success response পাঠাও।
 */
function sendSuccess($data, int $code = 200): void { // 'mixed' সরিয়ে শুধু '$data' দিন
    http_response_code($code);
    echo json_encode($data, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
    exit;
}

/**
 * Error response পাঠাও।
 */
function sendError(string $message, int $code = 400): void {
    http_response_code($code);
    echo json_encode([
        'success' => false,
        'message' => $message
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

/**
 * Request method check করো।
 */
function requireMethod(string $method): void {
    if ($_SERVER['REQUEST_METHOD'] !== strtoupper($method)) {
        sendError('Method not allowed.', 405);
    }
}

/**
 * File size কে human readable করো (debug এর জন্য)।
 */
function formatBytes(int $bytes): string {
    if ($bytes >= 1073741824) return round($bytes / 1073741824, 2) . ' GB';
    if ($bytes >= 1048576)    return round($bytes / 1048576, 2)    . ' MB';
    if ($bytes >= 1024)       return round($bytes / 1024, 2)       . ' KB';
    return $bytes . ' B';
}
