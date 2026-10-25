<?php
// ─────────────────────────────────────────────────────────────
// api/notes/store.php
// POST /api/notes & POST /api/v1/resources
// Validates PDF magic bytes, enforces size/MIME limits, uses cryptographic filenames,
// and places submissions into PENDING moderation state.
// ─────────────────────────────────────────────────────────────

function handleStore(): void {
    requireMethod('POST');

    $db = getDB();
    $authUser = getAuthenticatedUser($db);

    $title            = trim($_POST['title']             ?? '');
    $description      = trim($_POST['description']       ?? '');
    $courseName       = trim($_POST['course_name']       ?? ($_POST['subject'] ?? ''));
    $courseCode       = trim($_POST['course_code']       ?? '');
    $departmentName   = trim($_POST['department_name']   ?? '');
    $universityName   = trim($_POST['university_name']   ?? 'Daffodil International University');
    $resourceCategory = trim($_POST['resource_category'] ?? 'Lecture Notes');
    $semesterName     = trim($_POST['semester_name']     ?? '');
    $batchName        = trim($_POST['batch_name']        ?? '');

    // Never trust client-supplied user_id; derive from server session if authenticated
    $userId   = $authUser ? (int)$authUser['id'] : 0;
    $userName = $authUser ? $authUser['name'] : trim($_POST['user_name'] ?? 'Student Contributor');

    $courseId     = (int)($_POST['course_id']     ?? 0);
    $departmentId = (int)($_POST['department_id'] ?? 0);

    if ($title === '' || mb_strlen($title) > 255) {
        sendError('A valid resource title (1–255 characters) is required.', 422);
    }
    if (empty($_FILES['pdf_file'])) {
        sendError('PDF document file is required.', 422);
    }

    // Validate and store PDF with %PDF- signature verification
    $pdfResult = uploadFile(
        $_FILES['pdf_file'],
        'uploads/notes',
        ['application/pdf'],
        20 * 1024 * 1024,
        true
    );

    if (!$pdfResult['success']) {
        sendError($pdfResult['message'], 422);
    }

    // Optional thumbnail upload
    $thumbnailUrl = '';
    if (!empty($_FILES['thumbnail']['name'])) {
        $thumbResult = uploadFile(
            $_FILES['thumbnail'],
            'uploads/thumbnails',
            ['image/jpeg', 'image/png', 'image/webp'],
            2 * 1024 * 1024,
            false
        );
        if ($thumbResult['success']) {
            $thumbnailUrl = $thumbResult['url'];
        }
    }

    try {
        $stmt = $db->prepare("
            INSERT INTO notes
                (title, description, course_name, course_code, course_id,
                 department_name, department_id, university_name, resource_category,
                 semester_name, batch_name, user_name, user_id,
                 file_url, file_size, thumbnail_url, status)
            VALUES
                (:title, :description, :course_name, :course_code, :course_id,
                 :department_name, :department_id, :university_name, :resource_category,
                 :semester_name, :batch_name, :user_name, :user_id,
                 :file_url, :file_size, :thumbnail_url, 'PENDING')
        ");

        $stmt->execute([
            ':title'             => $title,
            ':description'       => $description,
            ':course_name'       => $courseName,
            ':course_code'       => $courseCode,
            ':course_id'         => $courseId,
            ':department_name'   => $departmentName,
            ':department_id'     => $departmentId,
            ':university_name'   => $universityName,
            ':resource_category' => $resourceCategory,
            ':semester_name'     => $semesterName,
            ':batch_name'        => $batchName,
            ':user_name'         => $userName,
            ':user_id'           => $userId,
            ':file_url'          => $pdfResult['url'],
            ':file_size'         => (int)$pdfResult['size'],
            ':thumbnail_url'     => $thumbnailUrl,
        ]);
    } catch (PDOException $e) {
        // Fallback if database has not yet run 001_academic_platform_upgrade.sql
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
    }

    $newId = (int)$db->lastInsertId();

    sendSuccess([
        'success' => true,
        'message' => 'Resource uploaded and placed in Pending Review queue.',
        'id'      => $newId,
        'status'  => 'PENDING',
        'file_url'=> $pdfResult['url'],
    ], 201);
}

/**
 * Hardened file upload helper with PDF magic header verification and CSPRNG filenames.
 */
function uploadFile(array $file, string $dir, array $allowedMimes, int $maxSize, bool $verifyPdfMagic): array {
    if (($file['error'] ?? UPLOAD_ERR_NO_FILE) !== UPLOAD_ERR_OK) {
        return ['success' => false, 'message' => 'File upload error code: ' . ($file['error'] ?? -1)];
    }

    if ($file['size'] <= 0 || $file['size'] > $maxSize) {
        return ['success' => false, 'message' => 'File size is invalid or exceeds maximum allowed limit.'];
    }

    $finfo    = finfo_open(FILEINFO_MIME_TYPE);
    $mimeType = finfo_file($finfo, $file['tmp_name']);
    finfo_close($finfo);

    if (!in_array($mimeType, $allowedMimes, true)) {
        return ['success' => false, 'message' => 'Disallowed MIME type: ' . $mimeType];
    }

    if ($verifyPdfMagic) {
        $fp = fopen($file['tmp_name'], 'rb');
        $header = $fp ? fread($fp, 5) : '';
        if ($fp) fclose($fp);
        if ($header !== '%PDF-') {
            return ['success' => false, 'message' => 'Invalid PDF file signature.'];
        }
    }

    $allowedExts = $verifyPdfMagic ? ['pdf'] : ['jpg', 'jpeg', 'png', 'webp'];
    $extension = strtolower(pathinfo($file['name'], PATHINFO_EXTENSION));
    if (!in_array($extension, $allowedExts, true)) {
        $extension = $allowedExts[0];
    }

    $filename  = 'nv_' . bin2hex(random_bytes(16)) . '.' . $extension;
    $uploadDir = __DIR__ . '/../../' . $dir . '/';
    $fullPath  = $uploadDir . $filename;

    if (!is_dir($uploadDir)) {
        mkdir($uploadDir, 0755, true);
    }

    if (!move_uploaded_file($file['tmp_name'], $fullPath)) {
        return ['success' => false, 'message' => 'Failed to store uploaded file.'];
    }

    $baseUrl = getBaseUrl();
    $url     = $baseUrl . '/' . $dir . '/' . $filename;

    return [
        'success'  => true,
        'url'      => $url,
        'size'     => (int)$file['size'],
        'filename' => $filename,
    ];
}

function getBaseUrl(): string {
    $protocol = (!empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off') ? 'https' : 'http';
    $host     = $_SERVER['HTTP_HOST'] ?? 'localhost';
    return $protocol . '://' . $host;
}
