<?php
// ─────────────────────────────────────────────────────────────
// config/database.php — MySQL connection config
// ─────────────────────────────────────────────────────────────

define('DB_HOST',     'localhost');
define('DB_NAME',     'jihadjp1_notevault_diu');
define('DB_USER',     'jihadjp1_notevault_diu');        // তোমার MySQL username
define('DB_PASS',     'notevault_diu#jihadjp');            // তোমার MySQL password
define('DB_CHARSET',  'utf8mb4');

/**
 * PDO connection — singleton pattern।
 * একবার connect হলে বারবার নতুন connection হয় না।
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
            http_response_code(500);
            echo json_encode([
                'success' => false,
                'message' => 'Database connection failed.',
                // Production এ এই line টা remove করো:
                'debug'   => $e->getMessage()
            ]);
            exit;
        }
    }

    return $pdo;
}
