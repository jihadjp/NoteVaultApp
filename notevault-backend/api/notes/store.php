<?php
// ─────────────────────────────────────────────────────────────
// api/notes/store.php
// POST /api/notes — Create new note + PDF file upload
// ─────────────────────────────────────────────────────────────

function handleStore(): void {
    requireMethod('POST');

    // ─── Validation ───────────────────────────────────────────
    $title          = trim($_POST['title']           ?? '');
    $description    = trim($_POST['description']     ?? '');
    $courseName     = trim($_POST['course_name']     ?? ''); // maps to subject
    $courseCode     = trim($_POST['course_code']     ?? '');
    $departmentName = trim($_POST['department_name'] ?? '');
    $userName       = trim($_POST['user_name']       ?? '');
    $courseId       = (int)($_POST['course_id']      ?? 0);
    $departmentId   = (int)($_POST['department_id']  ?? 0);
    $userId         = (int)($_POST['user_id']        ?? 0);

    if (!$title) {
        sendError('Title is required.');
    }
    if (empty($_FILES['pdf_file'])) {
        sendError('PDF file is required.');
    }

    // ─── PDF Upload ───────────────────────────────────────────
    $pdfResult = uploadFile(
        $_FILES['pdf_file'],
        'uploads/notes',
        ['application/pdf'],
        20 * 1024 * 1024
    );

    if (!$pdfResult['success']) {
        sendError($pdfResult['message']);
    }

    // ─── Thumbnail Upload (optional) ──────────────────────────
    $thumbnailUrl = '';
    if (!empty($_FILES['thumbnail']['name'])) {
        $thumbResult = uploadFile(
            $_FILES['thumbnail'],
            'uploads/thumbnails',
            ['image/jpeg', 'image/png', 'image/webp'],
            2 * 1024 * 1024
        );
        if ($thumbResult['success']) {
            $thumbnailUrl = $thumbResult['url'];
        }
    }

    // ─── Database Insert ──────────────────────────────────────
    $db   = getDB();
    $stmt = $db->prepare("
        INSERT INTO notes
            (title, description, course_name, course_code, course_id, department_name, department_id, user_name, user_id, file_url, thumbnail_url, status)
        VALUES
            (:title, :description, :course_name, :course_code, :course_id, :department_name, :department_id, :user_name, :user_id, :file_url, :thumbnail_url, 'PENDING')
    ");

    $stmt->execute([
        ':title'           => $title,
        ':description'     => $description,
        ':course_name'     => $courseName,
        ':course_code'     => $courseCode,
        ':course_id'       => $courseId,
        ':department_name' => $departmentName,
        ':department_id'   => $departmentId,
        ':user_name'       => $userName,
        ':user_id'         => $userId,
        ':file_url'        => $pdfResult['url'],
        ':thumbnail_url'   => $thumbnailUrl,
    ]);

    $newId = (int)$db->lastInsertId();

    sendSuccess([
        'success' => true,
        'message' => 'Note uploaded and pending approval.',
        'id'      => $newId,
        'file_url'=> $pdfResult['url'],
    ], 201);
}

/**
 * File upload helper.
 */
function uploadFile(array $file, string $dir, array $allowedMimes, int $maxSize): array {
    if ($file['error'] !== UPLOAD_ERR_OK) {
        return ['success' => false, 'message' => 'File upload error: ' . $file['error']];
    }

    if ($file['size'] > $maxSize) {
        return ['success' => false, 'message' => 'File too large.'];
    }

    $finfo    = finfo_open(FILEINFO_MIME_TYPE);
    $mimeType = finfo_file($finfo, $file['tmp_name']);
    finfo_close($finfo);

    if (!in_array($mimeType, $allowedMimes)) {
        return ['success' => false, 'message' => 'Invalid file type: ' . $mimeType];
    }

    $extension = pathinfo($file['name'], PATHINFO_EXTENSION);
    $filename  = uniqid('nv_', true) . '.' . strtolower($extension);
    $uploadDir = __DIR__ . '/../../' . $dir . '/';
    $fullPath  = $uploadDir . $filename;

    if (!is_dir($uploadDir)) {
        mkdir($uploadDir, 0755, true);
    }

    if (!move_uploaded_file($file['tmp_name'], $fullPath)) {
        return ['success' => false, 'message' => 'Failed to save file.'];
    }

    $baseUrl = getBaseUrl();
    $url     = $baseUrl . '/' . $dir . '/' . $filename;

    return [
        'success' => true,
        'url'     => $url,
        'size'    => $file['size'],
        'filename'=> $filename,
    ];
}

function getBaseUrl(): string {
    $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
    $host     = $_SERVER['HTTP_HOST'];
    return $protocol . '://' . $host;
}
