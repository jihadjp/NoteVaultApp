<?php
// ─────────────────────────────────────────────────────────────
// api/notes/store.php
// POST /api/notes — নতুন note তৈরি + PDF file upload
//
// multipart/form-data দিয়ে call করতে হবে:
//   Fields: title, description, subject, uploaded_by
//   Files:  pdf_file (required), thumbnail (optional)
// ─────────────────────────────────────────────────────────────

function handleStore(): void {
    requireMethod('POST');

    // ─── Validation ───────────────────────────────────────────
    $title      = trim($_POST['title']       ?? '');
    $description= trim($_POST['description'] ?? '');
    $subject    = trim($_POST['subject']     ?? '');
    $uploadedBy = trim($_POST['uploaded_by'] ?? '');

    if (!$title) {
        sendError('Title is required.');
    }
    if (empty($_FILES['pdf_file'])) {
        sendError('PDF file is required.');
    }

    // ─── PDF Upload ───────────────────────────────────────────
    $pdfResult = uploadFile(
        $_FILES['pdf_file'],
        'uploads/pdfs',
        ['application/pdf'],
        20 * 1024 * 1024 // 20 MB max
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
            2 * 1024 * 1024 // 2 MB max
        );
        if ($thumbResult['success']) {
            $thumbnailUrl = $thumbResult['url'];
        }
    }

    // ─── Database Insert ──────────────────────────────────────
    $db   = getDB();
    $stmt = $db->prepare("
        INSERT INTO notes
            (title, description, subject, uploaded_by, pdf_url, file_size, thumbnail_url, uploaded_at)
        VALUES
            (:title, :description, :subject, :uploaded_by, :pdf_url, :file_size, :thumbnail_url, CURDATE())
    ");

    $stmt->execute([
        ':title'         => $title,
        ':description'   => $description,
        ':subject'       => $subject,
        ':uploaded_by'   => $uploadedBy,
        ':pdf_url'       => $pdfResult['url'],
        ':file_size'     => $pdfResult['size'],
        ':thumbnail_url' => $thumbnailUrl,
    ]);

    $newId = (int)$db->lastInsertId();

    sendSuccess([
        'success' => true,
        'message' => 'Note uploaded successfully.',
        'id'      => $newId,
        'pdf_url' => $pdfResult['url'],
    ], 201);
}

/**
 * File upload helper।
 * File validate করে নির্দিষ্ট folder এ save করে।
 */
function uploadFile(array $file, string $dir, array $allowedMimes, int $maxSize): array {
    // Upload error check
    if ($file['error'] !== UPLOAD_ERR_OK) {
        return ['success' => false, 'message' => 'File upload error: ' . $file['error']];
    }

    // Size check
    if ($file['size'] > $maxSize) {
        return ['success' => false, 'message' => 'File too large. Max: ' . formatBytes($maxSize)];
    }

    // MIME type check (extension নয়, actual MIME check করো)
    $finfo    = finfo_open(FILEINFO_MIME_TYPE);
    $mimeType = finfo_file($finfo, $file['tmp_name']);
    finfo_close($finfo);

    if (!in_array($mimeType, $allowedMimes)) {
        return ['success' => false, 'message' => 'Invalid file type: ' . $mimeType];
    }

    // Unique filename তৈরি করো
    $extension = pathinfo($file['name'], PATHINFO_EXTENSION);
    $filename  = uniqid('nvd_', true) . '.' . strtolower($extension);
    $uploadDir = __DIR__ . '/../../' . $dir . '/';
    $fullPath  = $uploadDir . $filename;

    // Directory না থাকলে তৈরি করো
    if (!is_dir($uploadDir)) {
        mkdir($uploadDir, 0755, true);
    }

    // Move করো
    if (!move_uploaded_file($file['tmp_name'], $fullPath)) {
        return ['success' => false, 'message' => 'Failed to save file.'];
    }

    // Public URL তৈরি করো
    $baseUrl = getBaseUrl();
    $url     = $baseUrl . '/' . $dir . '/' . $filename;

    return [
        'success' => true,
        'url'     => $url,
        'size'    => $file['size'],
        'filename'=> $filename,
    ];
}

/**
 * Server এর base URL বের করো।
 */
function getBaseUrl(): string {
    $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
    $host     = $_SERVER['HTTP_HOST'];
    return $protocol . '://' . $host;
}
