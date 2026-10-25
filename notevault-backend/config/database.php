<?php
// ─────────────────────────────────────────────────────────────
// config/database.php — Secure MySQL PDO Connection
// Reads credentials from environment variables (.env / server config)
// Never exposes internal PDO exception messages in production.
// ─────────────────────────────────────────────────────────────

define('DB_HOST',    getenv('DB_HOST') ?: 'localhost');
define('DB_NAME',    getenv('DB_NAME') ?: 'notevault_diu');
define('DB_USER',    getenv('DB_USER') ?: 'root');
define('DB_PASS',    getenv('DB_PASS') !== false ? getenv('DB_PASS') : '');
define('DB_CHARSET', 'utf8mb4');

/**
 * PDO singleton connection with prepared statements enforced.
 */
function getDB(): PDO {
    static $pdo = null;

    if ($pdo === null) {
        $dsn = "mysql:host=" . DB_HOST
             . ";dbname=" . DB_NAME
             . ";charset=" . DB_CHARSET;

        $options = [
            PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES   => false,
        ];

        try {
            $pdo = new PDO($dsn, DB_USER, DB_PASS, $options);
        } catch (PDOException $e) {
            error_log('[NoteVault DB Error] ' . $e->getMessage());
            http_response_code(500);
            $response = [
                'success' => false,
                'message' => 'Database service unavailable.'
            ];
            if (getenv('APP_DEBUG') === 'true') {
                $response['debug'] = $e->getMessage();
            }
            echo json_encode($response, JSON_UNESCAPED_UNICODE);
            exit;
        }
    }

    return $pdo;
}
