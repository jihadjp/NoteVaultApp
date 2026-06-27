<?php
// ─────────────────────────────────────────────────────────────
// index.php — Front Controller / Router
// সব request এখানে আসে, তারপর সঠিক handler এ যায়।
//
// Routes:
//   GET  /api/notes              → সব notes
//   GET  /api/notes/search?q=    → search
//   GET  /api/notes/{id}         → একটা note
//   POST /api/notes              → নতুন note তৈরি
//   POST /api/notes/{id}/download → download count বাড়াও
//   DELETE /api/notes/{id}       → note delete
// ─────────────────────────────────────────────────────────────
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/config/helpers.php';

// CORS + JSON headers
setCorsHeaders();

// URL parse করো
$requestUri    = $_SERVER['REQUEST_URI'];
$requestMethod = $_SERVER['REQUEST_METHOD'];

// Query string সরাও: /api/notes?q=test → /api/notes
$path = parse_url($requestUri, PHP_URL_PATH);

// Leading slash সরাও, lowercase করো
$path = trim(strtolower($path), '/');

// ─── Routing ──────────────────────────────────────────────────

// GET /api/notes/search?q=keyword
if ($path === 'api/notes/search' && $requestMethod === 'GET') {
    require_once __DIR__ . '/api/notes/search.php';
    exit;
}

// /api/notes বা /api/notes/{id} বা /api/notes/{id}/download
if (preg_match('#^api/notes(/(\d+)(/(\w+))?)?$#', $path, $matches)) {
    $id     = $matches[2] ?? null;       // null = সব notes
    $action = $matches[4] ?? null;       // e.g. "download"

    if ($id && $action === 'download' && $requestMethod === 'POST') {
        // POST /api/notes/{id}/download
        require_once __DIR__ . '/api/notes/download.php';
        handleDownload((int)$id);
        exit;
    }

    if ($id && $requestMethod === 'DELETE') {
        // DELETE /api/notes/{id}
        require_once __DIR__ . '/api/notes/delete.php';
        handleDelete((int)$id);
        exit;
    }

    if ($id && $requestMethod === 'GET') {
        // GET /api/notes/{id}
        require_once __DIR__ . '/api/notes/show.php';
        handleShow((int)$id);
        exit;
    }

    if (!$id && $requestMethod === 'GET') {
        // GET /api/notes
        require_once __DIR__ . '/api/notes/index.php';
        handleIndex();
        exit;
    }

    if (!$id && $requestMethod === 'POST') {
        // POST /api/notes  (upload)
        require_once __DIR__ . '/api/notes/store.php';
        handleStore();
        exit;
    }
}

// কোনো route match না হলে
sendError('Endpoint not found.', 404);
