<?php
// ─────────────────────────────────────────────────────────────
// index.php — Front Controller / Router for NoteVault Academic Platform
// Supports both legacy /api/notes routes and versioned /api/v1/* routes.
// ─────────────────────────────────────────────────────────────

$appDebug = getenv('APP_DEBUG') === 'true';
ini_set('display_errors', $appDebug ? '1' : '0');
ini_set('display_startup_errors', $appDebug ? '1' : '0');
error_reporting($appDebug ? E_ALL : 0);

require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/config/helpers.php';

setCorsHeaders();

$requestUri    = $_SERVER['REQUEST_URI'] ?? '/';
$requestMethod = $_SERVER['REQUEST_METHOD'] ?? 'GET';
$path          = trim(strtolower(parse_url($requestUri, PHP_URL_PATH) ?: ''), '/');

// ─── API v1 Academic Hierarchy & Moderation Routes ──────────────────────────

if ($path === 'api/v1/universities') {
    require_once __DIR__ . '/api/v1/hierarchy.php';
    handleUniversities();
    exit;
}

if ($path === 'api/v1/departments') {
    require_once __DIR__ . '/api/v1/hierarchy.php';
    handleDepartments();
    exit;
}

if ($path === 'api/v1/courses') {
    require_once __DIR__ . '/api/v1/hierarchy.php';
    handleCourses();
    exit;
}

if ($path === 'api/v1/moderation/queue') {
    require_once __DIR__ . '/api/v1/moderation.php';
    handleModerationQueue();
    exit;
}

if (preg_match('#^api/v1/moderation/(\d+)/decision$#', $path, $m)) {
    require_once __DIR__ . '/api/v1/moderation.php';
    handleModerationDecision((int)$m[1]);
    exit;
}

// /api/v1/resources/{id}/reviews or /api/v1/resources/{id}/reports
if (preg_match('#^api/v1/resources/(\d+)/(reviews|reports)$#', $path, $m)) {
    $resId  = (int)$m[1];
    $subAct = $m[2];
    if ($subAct === 'reviews') {
        require_once __DIR__ . '/api/v1/reviews.php';
        handleReviews($resId);
        exit;
    }
    if ($subAct === 'reports') {
        require_once __DIR__ . '/api/v1/reports.php';
        handleReports($resId);
        exit;
    }
}

// Normalize /api/v1/search to /api/notes/search
if (($path === 'api/notes/search' || $path === 'api/v1/search') && $requestMethod === 'GET') {
    require_once __DIR__ . '/api/notes/search.php';
    exit;
}

// GET /api/questions
if ($path === 'api/questions' && $requestMethod === 'GET') {
    require_once __DIR__ . '/api/questions/index.php';
    handleQuestions();
    exit;
}

// /api/notes or /api/v1/resources
if (preg_match('#^api/(notes|v1/resources)(/(\d+)(/(\w+))?)?$#', $path, $matches)) {
    $id     = $matches[3] ?? null;
    $action = $matches[5] ?? null;

    if ($id && $action === 'download' && $requestMethod === 'POST') {
        require_once __DIR__ . '/api/notes/download.php';
        handleDownload((int)$id);
        exit;
    }

    if ($id && $requestMethod === 'DELETE') {
        require_once __DIR__ . '/api/notes/delete.php';
        handleDelete((int)$id);
        exit;
    }

    if ($id && $requestMethod === 'GET') {
        require_once __DIR__ . '/api/notes/show.php';
        handleShow((int)$id);
        exit;
    }

    if (!$id && $requestMethod === 'GET') {
        require_once __DIR__ . '/api/notes/index.php';
        handleIndex();
        exit;
    }

    if (!$id && $requestMethod === 'POST') {
        require_once __DIR__ . '/api/notes/store.php';
        handleStore();
        exit;
    }
}

sendError('Endpoint not found.', 404);
