import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const PORT = 3000;

// ─── Initial Verified DIU Academic Dataset ───────────────────────────────────
const UNIVERSITIES = [
  { id: 1, code: 'DIU', name: 'Daffodil International University (DIU)', short_name: 'DIU', country: 'Bangladesh', is_active: 1 },
  { id: 2, code: 'BUET', name: 'Bangladesh University of Engineering and Technology (BUET)', short_name: 'BUET', country: 'Bangladesh', is_active: 1 },
  { id: 3, code: 'DU', name: 'University of Dhaka (DU)', short_name: 'DU', country: 'Bangladesh', is_active: 1 },
  { id: 4, code: 'NSU', name: 'North South University (NSU)', short_name: 'NSU', country: 'Bangladesh', is_active: 1 },
  { id: 5, code: 'BRACU', name: 'BRAC University (BRACU)', short_name: 'BRACU', country: 'Bangladesh', is_active: 1 }
];

const DEPARTMENTS = [
  { id: 1, university_id: 1, code: 'SWE', name: 'Software Engineering', faculty_name: 'Faculty of Science & Information Technology' },
  { id: 2, university_id: 1, code: 'CSE', name: 'Computer Science & Engineering', faculty_name: 'Faculty of Science & Information Technology' },
  { id: 3, university_id: 1, code: 'EEE', name: 'Electrical & Electronic Engineering', faculty_name: 'Faculty of Engineering' },
  { id: 4, university_id: 1, code: 'BBA', name: 'Business Administration', faculty_name: 'Faculty of Business & Entrepreneurship' },
  { id: 5, university_id: 1, code: 'NFE', name: 'Nutrition & Food Engineering', faculty_name: 'Faculty of Allied Health Sciences' }
];

const COURSES = [
  { id: 1, department_id: 1, department_code: 'SWE', course_code: 'SWE112', course_title: 'Programming & Problem Solving', credit_hours: 3.0, semester_name: 'L1-T1' },
  { id: 2, department_id: 1, department_code: 'SWE', course_code: 'SWE122', course_title: 'Data Structures & Algorithms', credit_hours: 3.0, semester_name: 'L1-T2' },
  { id: 3, department_id: 1, department_code: 'SWE', course_code: 'SWE132', course_title: 'Object Oriented Programming', credit_hours: 3.0, semester_name: 'L1-T3' },
  { id: 4, department_id: 1, department_code: 'SWE', course_code: 'SWE211', course_title: 'Database Management Systems', credit_hours: 3.0, semester_name: 'L2-T1' },
  { id: 5, department_id: 1, department_code: 'SWE', course_code: 'SWE223', course_title: 'Software Engineering & System Analysis', credit_hours: 3.0, semester_name: 'L2-T2' },
  { id: 6, department_id: 1, department_code: 'SWE', course_code: 'SWE231', course_title: 'Operating Systems & System Programming', credit_hours: 3.0, semester_name: 'L2-T3' },
  { id: 7, department_id: 1, department_code: 'SWE', course_code: 'SWE312', course_title: 'Computer Networks & Security', credit_hours: 3.0, semester_name: 'L3-T1' },
  { id: 8, department_id: 2, department_code: 'CSE', course_code: 'CSE1102', course_title: 'Object Oriented Programming', credit_hours: 3.0, semester_name: 'L1-T2' },
  { id: 9, department_id: 2, department_code: 'CSE', course_code: 'CSE2101', course_title: 'Data Structures', credit_hours: 3.0, semester_name: 'L2-T1' },
  { id: 10, department_id: 2, department_code: 'CSE', course_code: 'CSE226', course_title: 'Numerical Methods', credit_hours: 3.0, semester_name: 'L2-T2' }
];

let resources = [
  {
    id: 1,
    title: 'Database Management Systems — Relational Algebra & SQL Normalization Notes',
    description: 'Comprehensive lecture notes covering ER modeling, relational algebra, 1NF–BCNF normalization, and indexing for SWE-43 and subsequent intakes.',
    courseId: 4,
    courseCode: 'SWE211',
    courseName: 'Database Management Systems',
    departmentId: 1,
    departmentName: 'SWE',
    universityName: 'Daffodil International University (DIU)',
    resourceCategory: 'Lecture Notes',
    semesterName: 'L2-T1',
    batchName: 'SWE-43',
    userName: 'Md. Tareq Hasan',
    createdAt: '2025-02-10',
    updatedAt: '2025-02-10',
    viewCount: 1420,
    fileSize: 2457600,
    status: 'APPROVED',
    isVerified: true,
    ratingAvg: 4.9,
    ratingCount: 28,
    totalPages: 42,
    fileUrl: '/api/sample-pdf/1'
  },
  {
    id: 2,
    title: 'Numerical Method Part 4 — Complete Chapter 1–10 Reference',
    description: 'Root finding, Newton-Raphson, Gaussian elimination, interpolation, and numerical integration complete course notes.',
    courseId: 10,
    courseCode: 'CSE226',
    courseName: 'Numerical Methods',
    departmentId: 2,
    departmentName: 'CSE',
    universityName: 'Daffodil International University (DIU)',
    resourceCategory: 'Lecture Notes',
    semesterName: 'L2-T2',
    batchName: 'CSE-64',
    userName: 'Dr. Rahim Uddin',
    createdAt: '2025-01-15',
    updatedAt: '2025-01-15',
    viewCount: 1234,
    fileSize: 3145728,
    status: 'APPROVED',
    isVerified: true,
    ratingAvg: 4.8,
    ratingCount: 19,
    totalPages: 36,
    fileUrl: '/api/sample-pdf/2'
  },
  {
    id: 3,
    title: 'OOP Lecture Slides Full Semester — Encapsulation, Inheritance & Polymorphism',
    description: 'Complete Object Oriented Programming lecture deck with Java design patterns, UML class diagrams, and exception handling.',
    courseId: 8,
    courseCode: 'CSE1102',
    courseName: 'Object Oriented Programming',
    departmentId: 2,
    departmentName: 'CSE',
    universityName: 'Daffodil International University (DIU)',
    resourceCategory: 'Lecture Slides',
    semesterName: 'L1-T2',
    batchName: 'SWE-43 / CSE-64',
    userName: 'Prof. Karim Hassan',
    createdAt: '2025-02-20',
    updatedAt: '2025-02-20',
    viewCount: 895,
    fileSize: 4194304,
    status: 'APPROVED',
    isVerified: true,
    ratingAvg: 4.7,
    ratingCount: 14,
    totalPages: 58,
    fileUrl: '/api/sample-pdf/3'
  },
  {
    id: 4,
    title: 'SWE Curriculum Official Syllabus — Outcome-Based Education (OBE)',
    description: 'Official Daffodil International University Department of Software Engineering OBE syllabus, course mapping, and grading policy.',
    courseId: 5,
    courseCode: 'SWE223',
    courseName: 'Software Engineering & System Analysis',
    departmentId: 1,
    departmentName: 'SWE',
    universityName: 'Daffodil International University (DIU)',
    resourceCategory: 'Official Syllabus',
    semesterName: 'L2-T2',
    batchName: 'SWE-43',
    userName: 'DIU SWE Coordination Cell',
    createdAt: '2025-01-05',
    updatedAt: '2025-01-05',
    viewCount: 2150,
    fileSize: 1843200,
    status: 'APPROVED',
    isVerified: true,
    ratingAvg: 5.0,
    ratingCount: 41,
    totalPages: 24,
    fileUrl: '/api/sample-pdf/4'
  },
  {
    id: 5,
    title: 'Data Structures & Algorithms Lab Manual — Trees, Graphs & Dynamic Programming',
    description: 'Verified lab manual covering linked lists, AVL trees, Dijkstra shortest path, BFS/DFS, and complexity analysis.',
    courseId: 2,
    courseCode: 'SWE122',
    courseName: 'Data Structures & Algorithms',
    departmentId: 1,
    departmentName: 'SWE',
    universityName: 'Daffodil International University (DIU)',
    resourceCategory: 'Lab Manuals',
    semesterName: 'L1-T2',
    batchName: 'SWE-43',
    userName: 'Nusrat Jahan',
    createdAt: '2025-03-01',
    updatedAt: '2025-03-01',
    viewCount: 740,
    fileSize: 1572864,
    status: 'APPROVED',
    isVerified: true,
    ratingAvg: 4.9,
    ratingCount: 12,
    totalPages: 30,
    fileUrl: '/api/sample-pdf/5'
  },
  {
    id: 6,
    title: 'SWE211 Midterm & Final Exam Topic Shortlist & Structured Suggestions',
    description: 'Focused study guide and problem sets on SQL joins, transaction ACID properties, serializability, and B+ tree indexing.',
    courseId: 4,
    courseCode: 'SWE211',
    courseName: 'Database Management Systems',
    departmentId: 1,
    departmentName: 'SWE',
    universityName: 'Daffodil International University (DIU)',
    resourceCategory: 'Suggestions',
    semesterName: 'L2-T1',
    batchName: 'SWE-43',
    userName: 'Mahmudul Hasan',
    createdAt: '2025-03-12',
    updatedAt: '2025-03-12',
    viewCount: 1680,
    fileSize: 983040,
    status: 'APPROVED',
    isVerified: true,
    ratingAvg: 4.8,
    ratingCount: 22,
    totalPages: 16,
    fileUrl: '/api/sample-pdf/6'
  }
];

const QUESTIONS = [
  {
    id: 101,
    course_name: 'SWE211 — Database Management Systems',
    department_name: 'Software Engineering',
    department_short: 'SWE',
    exam_type: 'MID TERM',
    semester_name: 'L2-T1',
    pdf_link: '/api/sample-pdf/101',
    submissions_count: 14,
    created_at: '2025-02-18'
  },
  {
    id: 102,
    course_name: 'SWE223 — Software Engineering & System Analysis',
    department_name: 'Software Engineering',
    department_short: 'SWE',
    exam_type: 'FINAL TERM',
    semester_name: 'L2-T2',
    pdf_link: '/api/sample-pdf/102',
    submissions_count: 19,
    created_at: '2025-01-29'
  },
  {
    id: 103,
    course_name: 'CSE226 — Numerical Methods',
    department_name: 'Computer Science & Engineering',
    department_short: 'CSE',
    exam_type: 'FINAL TERM',
    semester_name: 'L2-T2',
    pdf_link: '/api/sample-pdf/103',
    submissions_count: 23,
    created_at: '2025-01-20'
  },
  {
    id: 104,
    course_name: 'SWE122 — Data Structures & Algorithms',
    department_name: 'Software Engineering',
    department_short: 'SWE',
    exam_type: 'LAB',
    semester_name: 'L1-T2',
    pdf_link: '/api/sample-pdf/104',
    submissions_count: 11,
    created_at: '2025-02-05'
  }
];

let reviewsByResource = {
  1: [
    { id: 1, resource_id: 1, reviewer_name: 'Tanvir Ahmed (SWE-43)', rating: 5, review_text: 'Clear BCNF decomposition examples and SQL practice queries.', created_at: '2025-03-01' },
    { id: 2, resource_id: 1, reviewer_name: 'Sadia Islam (SWE-44)', rating: 5, review_text: 'Saved it offline before midterm — super well organized.', created_at: '2025-03-04' }
  ]
};

let reportsQueue = [];

function parseJsonBody(req) {
  return new Promise((resolve) => {
    let body = '';
    req.on('data', (chunk) => {
      body += chunk;
      if (body.length > 5 * 1024 * 1024) req.destroy();
    });
    req.on('end', () => {
      try {
        resolve(body ? JSON.parse(body) : {});
      } catch {
        resolve({});
      }
    });
  });
}

function sendJson(res, data, status = 200) {
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization'
  });
  res.end(JSON.stringify(data, null, 2));
}

function buildMinimalPdfBuffer(title) {
  const safeTitle = String(title || 'NoteVault DIU Academic Resource').replace(/[()\\]/g, '');
  const content = `%PDF-1.4
1 0 obj << /Type /Catalog /Pages 2 0 R >> endobj
2 0 obj << /Type /Pages /Kids [3 0 R] /Count 1 >> endobj
3 0 obj << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >> endobj
4 0 obj << /Length 128 >> stream
BT /F1 18 Tf 50 720 Td (${safeTitle}) Tj /F1 12 Tf 0 -30 Td (Daffodil International University - NoteVault Verified PDF) Tj ET
endstream endobj
5 0 obj << /Type /Font /Subtype /Type1 /BaseFont /Helvetica >> endobj
xref
0 6
0000000000 65535 f 
0000000009 00000 n 
0000000058 00000 n 
0000000115 00000 n 
0000000241 00000 n 
0000000421 00000 n 
trailer << /Size 6 /Root 1 0 R >>
startxref
489
%%EOF`;
  return Buffer.from(content, 'utf-8');
}

const server = http.createServer(async (req, res) => {
  if (req.method === 'OPTIONS') {
    res.writeHead(200, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization'
    });
    res.end();
    return;
  }

  const urlObj = new URL(req.url || '/', `http://${req.headers.host || 'localhost'}`);
  const pathname = urlObj.pathname;

  if (pathname === '/api/health') {
    const apkPath = path.join(__dirname, 'app/build/outputs/apk/debug/app-debug.apk');
    const apkExists = fs.existsSync(apkPath);
    const apkSize = apkExists ? fs.statSync(apkPath).size : 0;
    return sendJson(res, {
      status: 'ok',
      platform: 'NoteVault DIU Native Android App (com.jptechgenius.notevault)',
      packageName: 'com.jptechgenius.notevault',
      apkReady: apkExists,
      apkSize
    });
  }

  if (pathname === '/api/download-apk') {
    const apkPath = path.join(__dirname, 'app/build/outputs/apk/debug/app-debug.apk');
    if (!fs.existsSync(apkPath)) {
      return sendJson(res, { success: false, message: 'app-debug.apk not built yet.' }, 404);
    }
    const stat = fs.statSync(apkPath);
    res.writeHead(200, {
      'Content-Type': 'application/vnd.android.package-archive',
      'Content-Disposition': 'attachment; filename="NoteVault-DIU-debug.apk"',
      'Content-Length': stat.size
    });
    fs.createReadStream(apkPath).pipe(res);
    return;
  }

  if (pathname === '/api/docs') {
    const docName = urlObj.searchParams.get('file') || 'NOTEvault_IMPLEMENTATION_REPORT.md';
    const allowedDocs = [
      'NOTEvault_AUDIT_REPORT.md',
      'NOTEvault_IMPLEMENTATION_REPORT.md',
      'NOTEvault_API_DOCUMENTATION.md',
      'NOTEvault_DATABASE_MIGRATIONS.md'
    ];
    const target = allowedDocs.includes(docName) ? docName : 'NOTEvault_IMPLEMENTATION_REPORT.md';
    const fullPath = path.join(__dirname, target);
    const content = fs.existsSync(fullPath) ? fs.readFileSync(fullPath, 'utf-8') : '# Document not found';
    return sendJson(res, { file: target, content });
  }

  if (pathname.startsWith('/api/sample-pdf/')) {
    const id = Number(pathname.split('/').pop());
    const found = resources.find((r) => r.id === id);
    const pdfBuffer = buildMinimalPdfBuffer(found ? found.title : `NoteVault Academic Document #${id}`);
    res.writeHead(200, {
      'Content-Type': 'application/pdf',
      'Content-Disposition': `inline; filename="notevault_${id}.pdf"`,
      'Content-Length': pdfBuffer.length
    });
    res.end(pdfBuffer);
    return;
  }

  if (pathname === '/api/v1/universities' && req.method === 'GET') {
    return sendJson(res, UNIVERSITIES);
  }

  if (pathname === '/api/v1/departments' && req.method === 'GET') {
    const uniId = Number(urlObj.searchParams.get('university_id') || 1);
    return sendJson(res, DEPARTMENTS.filter((d) => !uniId || d.university_id === uniId));
  }

  if (pathname === '/api/v1/courses' && req.method === 'GET') {
    const dept = (urlObj.searchParams.get('department') || '').trim().toUpperCase();
    const sem = (urlObj.searchParams.get('semester') || '').trim().toUpperCase();
    const list = COURSES.filter((c) => {
      if (dept && dept !== 'ALL DEPARTMENTS' && c.department_code !== dept) return false;
      if (sem && sem !== 'ALL SEMESTERS' && c.semester_name !== sem) return false;
      return true;
    });
    return sendJson(res, list);
  }

  if ((pathname === '/api/notes' || pathname === '/api/v1/resources') && req.method === 'GET') {
    const dept = (urlObj.searchParams.get('department') || '').trim().toUpperCase();
    const courseCode = (urlObj.searchParams.get('courseCode') || urlObj.searchParams.get('course_code') || '').trim().toUpperCase();
    const category = (urlObj.searchParams.get('type') || urlObj.searchParams.get('category') || '').trim();
    const includePending = urlObj.searchParams.get('include_pending') === 'true';

    const filtered = resources.filter((r) => {
      if (!includePending && r.status !== 'APPROVED') return false;
      if (dept && dept !== 'ALL DEPARTMENTS' && r.departmentName.toUpperCase() !== dept) return false;
      if (courseCode && !r.courseCode.toUpperCase().includes(courseCode)) return false;
      if (category && category !== 'ALL' && r.resourceCategory.toLowerCase() !== category.toLowerCase()) return false;
      return true;
    });
    return sendJson(res, filtered);
  }

  if ((pathname === '/api/notes/search' || pathname === '/api/v1/search') && req.method === 'GET') {
    const q = (urlObj.searchParams.get('q') || '').trim().toLowerCase();
    if (!q) return sendJson(res, resources.filter((r) => r.status === 'APPROVED'));
    const matches = resources.filter((r) => {
      if (r.status !== 'APPROVED') return false;
      return (
        r.title.toLowerCase().includes(q) ||
        r.courseCode.toLowerCase().includes(q) ||
        r.courseName.toLowerCase().includes(q) ||
        r.departmentName.toLowerCase().includes(q) ||
        (r.description || '').toLowerCase().includes(q) ||
        (r.userName || '').toLowerCase().includes(q)
      );
    });
    return sendJson(res, matches);
  }

  if ((pathname === '/api/notes' || pathname === '/api/v1/resources') && req.method === 'POST') {
    const body = await parseJsonBody(req);
    const title = String(body.title || '').trim();
    if (!title) {
      return sendJson(res, { success: false, message: 'Resource title is required.' }, 422);
    }
    const newId = resources.reduce((max, r) => Math.max(max, r.id), 0) + 1;
    const created = {
      id: newId,
      title,
      description: String(body.description || 'Submitted via NoteVault Android student contribution workflow.'),
      courseId: 1,
      courseCode: String(body.course_code || body.courseCode || 'SWE211').trim(),
      courseName: String(body.course_name || body.courseName || 'Database Management Systems').trim(),
      departmentId: 1,
      departmentName: String(body.department_name || body.departmentName || 'SWE').trim(),
      universityName: String(body.university_name || body.universityName || 'Daffodil International University (DIU)').trim(),
      resourceCategory: String(body.resource_category || body.resourceCategory || 'Lecture Notes').trim(),
      semesterName: String(body.semester_name || body.semesterName || 'L2-T1').trim(),
      batchName: String(body.batch_name || body.batchName || 'SWE-43').trim(),
      userName: String(body.user_name || body.userName || 'DIU Scholar').trim(),
      createdAt: new Date().toISOString().slice(0, 10),
      updatedAt: new Date().toISOString().slice(0, 10),
      viewCount: 1,
      fileSize: Number(body.file_size || 1420000),
      status: 'APPROVED',
      isVerified: true,
      ratingAvg: 5.0,
      ratingCount: 1,
      totalPages: 18,
      fileUrl: `/api/sample-pdf/${newId}`
    };
    resources.unshift(created);
    return sendJson(res, {
      success: true,
      message: 'Submitted & verified in NoteVault DIU!',
      id: newId,
      status: 'APPROVED',
      resource: created
    }, 201);
  }

  const dlMatch = pathname.match(/^\/api\/(notes|v1\/resources)\/(\d+)\/download$/);
  if (dlMatch && req.method === 'POST') {
    const id = Number(dlMatch[2]);
    const item = resources.find((r) => r.id === id);
    if (!item) return sendJson(res, { success: false, message: 'Note not found.' }, 404);
    item.viewCount += 1;
    return sendJson(res, { success: true, viewCount: item.viewCount });
  }

  const revMatch = pathname.match(/^\/api\/v1\/resources\/(\d+)\/reviews$/);
  if (revMatch) {
    const id = Number(revMatch[1]);
    if (req.method === 'GET') {
      return sendJson(res, reviewsByResource[id] || []);
    }
    if (req.method === 'POST') {
      const body = await parseJsonBody(req);
      const rating = Math.min(5, Math.max(1, Number(body.rating || 5)));
      const reviewerName = String(body.reviewer_name || 'DIU Scholar').trim();
      const reviewText = String(body.review_text || '').trim();
      if (!reviewsByResource[id]) reviewsByResource[id] = [];
      const existingIdx = reviewsByResource[id].findIndex((r) => r.reviewer_name === reviewerName);
      const entry = {
        id: Date.now(),
        resource_id: id,
        reviewer_name: reviewerName,
        rating,
        review_text: reviewText,
        created_at: new Date().toISOString().slice(0, 10)
      };
      if (existingIdx >= 0) {
        reviewsByResource[id][existingIdx] = entry;
      } else {
        reviewsByResource[id].unshift(entry);
      }
      const list = reviewsByResource[id];
      const avg = list.reduce((sum, r) => sum + r.rating, 0) / list.length;
      const item = resources.find((r) => r.id === id);
      if (item) {
        item.ratingAvg = Number(avg.toFixed(2));
        item.ratingCount = list.length;
      }
      return sendJson(res, {
        success: true,
        ratingAvg: Number(avg.toFixed(2)),
        ratingCount: list.length,
        reviews: list
      }, 201);
    }
  }

  const repMatch = pathname.match(/^\/api\/v1\/resources\/(\d+)\/reports$/);
  if (repMatch && req.method === 'POST') {
    const id = Number(repMatch[1]);
    const body = await parseJsonBody(req);
    const report = {
      id: reportsQueue.length + 1,
      resource_id: id,
      reason: String(body.reason || 'Inaccurate course code / department mapping'),
      details: String(body.details || ''),
      reporter_name: String(body.reporter_name || 'DIU Scholar'),
      status: 'OPEN',
      created_at: new Date().toISOString().slice(0, 10)
    };
    reportsQueue.unshift(report);
    return sendJson(res, { success: true, message: 'Report submitted to moderation queue.', report }, 201);
  }

  if (pathname === '/api/questions' && req.method === 'GET') {
    return sendJson(res, QUESTIONS);
  }

  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(RENDER_ANDROID_SIMULATOR_HTML());
});

function RENDER_ANDROID_SIMULATOR_HTML() {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>NoteVault DIU — Android App Simulator (com.jptechgenius.notevault)</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@400;500;700&family=Plus+Jakarta+Sans:wght@500;600;700&display=swap" rel="stylesheet">
  <style>
    :root {
      --colorPrimary: #086B61;
      --colorPrimaryDark: #06463F;
      --colorAccent: #14B8A6;
      --colorBackground: #F6F8FA;
      --colorSurface: #FFFFFF;
      --colorSurfaceVariant: #EEF3F2;
      --colorTextPrimary: #17211F;
      --colorTextSecondary: #4B635E;
      --colorTextMuted: #7B938E;
      --colorDivider: #DCE5E3;
      --colorDownloaded: #ECFDF5;
      --colorChipBg: #E6F4F2;
      --colorChipText: #086B61;
      --colorSuccess: #10B981;
      --colorError: #EF4444;
      --colorWarning: #F59E0B;
    }
    [data-theme="dark"] {
      --colorPrimary: #14B8A6;
      --colorPrimaryDark: #086B61;
      --colorAccent: #2DD4BF;
      --colorBackground: #101817;
      --colorSurface: #192321;
      --colorSurfaceVariant: #21302D;
      --colorTextPrimary: #F0F5F4;
      --colorTextSecondary: #A3B8B4;
      --colorTextMuted: #6E8782;
      --colorDivider: #2A3B38;
      --colorDownloaded: #132E27;
      --colorChipBg: #1A332F;
      --colorChipText: #2DD4BF;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; -webkit-tap-highlight-color: transparent; }
    body {
      font-family: 'Roboto', 'Plus Jakarta Sans', sans-serif;
      background: #0B1211;
      color: #F0F5F4;
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 12px 16px 24px;
    }
    /* Top Studio Bar for APK Download & Android Info */
    .studio-bar {
      width: 100%;
      max-width: 960px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: 10px;
      background: #14201E;
      border: 1px solid #253A36;
      border-radius: 12px;
      padding: 10px 16px;
      margin-bottom: 14px;
      font-size: 13px;
    }
    .studio-bar-left {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
    }
    .apk-badge {
      background: #086B61;
      color: #FFFFFF;
      font-weight: 700;
      font-size: 11px;
      padding: 3px 8px;
      border-radius: 6px;
      letter-spacing: 0.03em;
    }
    .studio-actions {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
    }
    .btn-apk {
      background: #10B981;
      color: #06281E;
      font-weight: 700;
      font-size: 12px;
      padding: 7px 13px;
      border-radius: 8px;
      text-decoration: none;
      border: none;
      cursor: pointer;
    }
    .btn-studio-sec {
      background: #1E2F2C;
      color: #D2E5E1;
      border: 1px solid #2E4742;
      font-weight: 600;
      font-size: 12px;
      padding: 6px 11px;
      border-radius: 8px;
      cursor: pointer;
    }

    /* Android Phone Frame */
    .phone-frame {
      width: 412px;
      max-width: 100%;
      height: 820px;
      background: var(--colorBackground);
      color: var(--colorTextPrimary);
      border-radius: 38px;
      border: 10px solid #222D2B;
      box-shadow: 0 24px 60px rgba(0,0,0,0.65), 0 0 0 2px #364945;
      display: flex;
      flex-direction: column;
      overflow: hidden;
      position: relative;
    }

    /* Android Status Bar */
    .android-status-bar {
      height: 28px;
      background: var(--colorBackground);
      color: var(--colorTextPrimary);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 18px;
      font-size: 11px;
      font-weight: 700;
      flex-shrink: 0;
      z-index: 20;
    }
    .camera-punch {
      width: 10px;
      height: 10px;
      background: #111;
      border-radius: 50%;
    }

    /* Splash Screen (activity_splash.xml) */
    .splash-screen {
      position: absolute;
      inset: 0;
      background: linear-gradient(160deg, #06463F 0%, #086B61 55%, #0D9488 100%);
      color: #fff;
      z-index: 50;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 24px;
      text-align: center;
      transition: opacity 0.35s ease;
    }
    .splash-screen.hidden {
      opacity: 0;
      pointer-events: none;
    }
    .splash-logo-box {
      width: 84px;
      height: 84px;
      border-radius: 22px;
      background: rgba(255,255,255,0.16);
      border: 1.5px solid rgba(255,255,255,0.35);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 36px;
      margin-bottom: 16px;
    }

    /* Android Toolbar (activity_home.xml) */
    .android-toolbar {
      height: 52px;
      padding: 0 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      background: var(--colorBackground);
      flex-shrink: 0;
    }
    .toolbar-title {
      font-size: 18px;
      font-weight: 700;
      color: var(--colorTextPrimary);
      letter-spacing: -0.01em;
    }
    .toolbar-icons {
      display: flex;
      align-items: center;
      gap: 6px;
    }
    .icon-btn {
      background: var(--colorSurfaceVariant);
      color: var(--colorTextPrimary);
      border: 1px solid var(--colorDivider);
      border-radius: 8px;
      padding: 4px 8px;
      font-size: 11px;
      font-weight: 700;
      cursor: pointer;
    }

    /* Scrollable CoordinatorLayout Body */
    .android-scroll-body {
      flex: 1;
      overflow-y: auto;
      padding-bottom: 88px;
    }
    .android-scroll-body::-webkit-scrollbar { width: 4px; }
    .android-scroll-body::-webkit-scrollbar-thumb { background: var(--colorDivider); border-radius: 4px; }

    /* Academic Dashboard Header */
    .dashboard-header {
      padding: 4px 16px 10px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
    }
    .greeting-sub {
      font-size: 12px;
      color: var(--colorTextSecondary);
    }
    .greeting-name {
      font-size: 20px;
      font-weight: 700;
      color: var(--colorPrimary);
      letter-spacing: -0.02em;
    }
    .context-row {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-top: 5px;
      flex-wrap: wrap;
    }
    .context-pill {
      background: var(--colorChipBg);
      color: var(--colorChipText);
      font-size: 11px;
      font-weight: 700;
      padding: 3px 9px;
      border-radius: 99px;
    }
    .context-change-btn {
      background: none;
      border: none;
      color: var(--colorAccent);
      font-size: 11px;
      font-weight: 700;
      cursor: pointer;
    }
    .avatar-card {
      width: 44px;
      height: 44px;
      border-radius: 13px;
      background: var(--colorPrimary);
      color: #fff;
      font-weight: 700;
      font-size: 15px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      box-shadow: 0 3px 8px rgba(8,107,97,0.28);
      cursor: pointer;
    }

    /* Smart Search Card (etSearch) */
    .search-box {
      margin: 8px 16px 0;
      height: 46px;
      background: var(--colorSurface);
      border: 1px solid var(--colorDivider);
      border-radius: 14px;
      display: flex;
      align-items: center;
      padding: 0 12px;
      gap: 8px;
    }
    .search-box input {
      border: none;
      background: transparent;
      color: var(--colorTextPrimary);
      font-size: 13px;
      width: 100%;
      outline: none;
    }

    /* ChipGroup Categories (chipGroupCategories) */
    .chip-strip {
      display: flex;
      gap: 6px;
      overflow-x: auto;
      padding: 10px 16px 4px;
      scrollbar-width: none;
    }
    .chip-strip::-webkit-scrollbar { display: none; }
    .m3-chip {
      background: var(--colorSurfaceVariant);
      color: var(--colorTextSecondary);
      border: 1px solid var(--colorDivider);
      padding: 5px 11px;
      border-radius: 8px;
      font-size: 11.5px;
      font-weight: 600;
      white-space: nowrap;
      cursor: pointer;
    }
    .m3-chip.checked {
      background: var(--colorPrimary);
      color: #fff;
      border-color: var(--colorPrimary);
    }

    /* Continue Studying Card (cardContinueStudying) */
    .continue-card {
      margin: 10px 16px 0;
      background: var(--colorSurface);
      border: 1.5px solid var(--colorPrimary);
      border-radius: 14px;
      padding: 11px 13px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
    }
    .continue-kicker {
      font-size: 10.5px;
      font-weight: 700;
      color: var(--colorPrimary);
    }
    .continue-title {
      font-size: 13px;
      font-weight: 700;
      color: var(--colorTextPrimary);
      margin-top: 2px;
      display: -webkit-box;
      -webkit-line-clamp: 1;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .continue-sub {
      font-size: 11px;
      color: var(--colorTextSecondary);
      margin-top: 1px;
    }
    .btn-outline-sm {
      background: transparent;
      color: var(--colorPrimary);
      border: 1px solid var(--colorPrimary);
      border-radius: 9px;
      padding: 6px 10px;
      font-size: 11.5px;
      font-weight: 700;
      white-space: nowrap;
      cursor: pointer;
    }

    /* Study Workspace Card (cardStudyWorkspace) */
    .workspace-card {
      margin: 8px 16px 0;
      background: var(--colorSurfaceVariant);
      border: 1px solid var(--colorDivider);
      border-radius: 14px;
      padding: 11px 13px;
    }
    .workspace-title {
      font-size: 12px;
      font-weight: 700;
      color: var(--colorTextPrimary);
    }
    .workspace-summary {
      font-size: 11.5px;
      color: var(--colorTextSecondary);
      margin-top: 2px;
    }
    .workspace-btns {
      display: flex;
      gap: 10px;
      margin-top: 7px;
    }
    .btn-text-sm {
      background: none;
      border: none;
      color: var(--colorPrimary);
      font-size: 11.5px;
      font-weight: 700;
      cursor: pointer;
    }

    /* Filter Spinners Row (layout_filter_notes.xml) */
    .filter-spinners {
      margin: 8px 16px 0;
      display: grid;
      grid-template-columns: 1fr 1fr auto;
      gap: 6px;
    }
    .filter-spinners select {
      background: var(--colorSurface);
      color: var(--colorTextPrimary);
      border: 1px solid var(--colorDivider);
      border-radius: 9px;
      padding: 6px 8px;
      font-size: 11.5px;
      outline: none;
    }

    /* 5-Destination TabLayout (tabLayout) */
    .m3-tablayout {
      margin: 10px 16px 6px;
      background: var(--colorSurfaceVariant);
      border: 1px solid var(--colorDivider);
      border-radius: 13px;
      padding: 3px;
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 2px;
    }
    .m3-tab {
      background: transparent;
      border: none;
      color: var(--colorTextSecondary);
      padding: 7px 2px;
      border-radius: 10px;
      font-size: 11px;
      font-weight: 700;
      cursor: pointer;
      text-align: center;
    }
    .m3-tab.active {
      background: var(--colorSurface);
      color: var(--colorPrimary);
      box-shadow: 0 1px 3px rgba(0,0,0,0.08);
    }

    /* RecyclerView Item Cards (item_note_card.xml) */
    .recycler-list {
      padding: 4px 16px 20px;
      display: flex;
      flex-direction: column;
      gap: 10px;
    }
    .android-note-card {
      background: var(--colorSurface);
      border: 1px solid var(--colorDivider);
      border-radius: 16px;
      padding: 13px;
      box-shadow: 0 2px 6px rgba(0,0,0,0.03);
      transition: transform 0.12s ease;
    }
    .android-note-card.downloaded {
      background: var(--colorDownloaded);
      border-color: var(--colorPrimary);
    }
    .note-top-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 6px;
      margin-bottom: 6px;
    }
    .subject-chip {
      background: var(--colorChipBg);
      color: var(--colorChipText);
      font-size: 10.5px;
      font-weight: 700;
      padding: 3px 8px;
      border-radius: 6px;
    }
    .downloaded-badge {
      background: #D1FAE5;
      color: #065F46;
      font-size: 10px;
      font-weight: 700;
      padding: 2px 7px;
      border-radius: 6px;
    }
    .note-title {
      font-size: 14.5px;
      font-weight: 700;
      color: var(--colorTextPrimary);
      line-height: 1.35;
      margin-bottom: 4px;
      cursor: pointer;
    }
    .note-desc {
      font-size: 12px;
      color: var(--colorTextSecondary);
      line-height: 1.4;
      margin-bottom: 8px;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .note-meta {
      font-size: 11px;
      color: var(--colorTextMuted);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-top: 8px;
      border-top: 1px solid var(--colorDivider);
      margin-bottom: 9px;
    }
    .note-actions {
      display: flex;
      gap: 6px;
    }
    .btn-card-primary {
      flex: 1.2;
      background: var(--colorPrimary);
      color: #fff;
      border: none;
      border-radius: 9px;
      padding: 7px 8px;
      font-size: 11.5px;
      font-weight: 700;
      cursor: pointer;
    }
    .btn-card-sec {
      flex: 1;
      background: var(--colorSurfaceVariant);
      color: var(--colorTextPrimary);
      border: 1px solid var(--colorDivider);
      border-radius: 9px;
      padding: 7px 6px;
      font-size: 11px;
      font-weight: 600;
      cursor: pointer;
    }

    /* ExtendedFloatingActionButton (fabUpload) */
    .android-efab {
      position: absolute;
      right: 16px;
      bottom: 42px;
      background: var(--colorPrimary);
      color: #fff;
      border: none;
      border-radius: 16px;
      padding: 11px 16px;
      font-size: 12.5px;
      font-weight: 700;
      box-shadow: 0 6px 16px rgba(8,107,97,0.45);
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 6px;
      z-index: 25;
    }

    /* NoteDetailActivity Screen (activity_note_detail.xml) */
    .detail-activity {
      position: absolute;
      inset: 28px 0 28px 0;
      background: var(--colorBackground);
      z-index: 35;
      display: none;
      flex-direction: column;
    }
    .detail-activity.open {
      display: flex;
    }
    .detail-toolbar {
      background: linear-gradient(135deg, #06463F, #086B61);
      color: #fff;
      padding: 10px 14px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 8px;
    }
    .detail-title {
      font-size: 13.5px;
      font-weight: 700;
      flex: 1;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    /* Native Android PDFView Container */
    .pdf-view-wrapper {
      flex: 1;
      position: relative;
      background: #424947;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }
    .pdf-Viewport {
      flex: 1;
      overflow-y: auto;
      padding: 12px 14px 24px;
      display: flex;
      flex-direction: column;
      gap: 14px;
      scroll-behavior: smooth;
    }
    .pdf-Viewport::-webkit-scrollbar { width: 5px; }
    .pdf-Viewport::-webkit-scrollbar-thumb { background: #086B61; border-radius: 4px; }
    .pdf-sheet {
      background: #FFFFFF;
      color: #17211F;
      border-radius: 6px;
      box-shadow: 0 4px 14px rgba(0,0,0,0.35);
      padding: 16px 16px 18px;
      min-height: 460px;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      transform-origin: top center;
      transition: background 0.2s, color 0.2s;
    }
    .pdf-Viewport.night-paper .pdf-sheet {
      background: #16201E;
      color: #E6F2EF;
      border: 1px solid #2A3F3B;
    }
    .pdf-sheet-banner {
      background: #086B61;
      color: #FFFFFF;
      margin: -16px -16px 12px -16px;
      padding: 10px 14px;
      border-radius: 6px 6px 0 0;
      display: flex;
      justify-content: space-between;
      align-items: center;
      font-size: 10px;
      font-weight: 700;
      letter-spacing: 0.03em;
    }
    .pdf-code-block {
      font-family: monospace;
      font-size: 10.5px;
      background: #EEF3F2;
      color: #06463F;
      border-left: 3px solid #086B61;
      padding: 8px 10px;
      border-radius: 4px;
      margin: 8px 0;
      white-space: pre-wrap;
      line-height: 1.4;
    }
    .pdf-Viewport.night-paper .pdf-code-block {
      background: #0E1615;
      color: #5EEAD4;
      border-left-color: #14B8A6;
    }
    .pdf-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 10.5px;
      margin: 8px 0;
    }
    .pdf-table th, .pdf-table td {
      border: 1px solid #CBD5E1;
      padding: 5px 7px;
      text-align: left;
    }
    .pdf-table th {
      background: #E6F4F2;
      color: #086B61;
      font-weight: 700;
    }
    .pdf-Viewport.night-paper .pdf-table th {
      background: #1F3330;
      color: #2DD4BF;
      border-color: #334B47;
    }
    .pdf-Viewport.night-paper .pdf-table td {
      border-color: #334B47;
    }
    .pdf-scroll-handle {
      position: absolute;
      right: 6px;
      top: 16px;
      background: rgba(8, 107, 97, 0.92);
      color: #fff;
      font-size: 10px;
      font-weight: 700;
      padding: 4px 8px;
      border-radius: 12px 0 0 12px;
      pointer-events: none;
      box-shadow: 0 2px 6px rgba(0,0,0,0.3);
      transition: top 0.1s ease;
    }

    /* Android Bottom Sheet / Dialog Overlay */
    .sheet-backdrop {
      position: absolute;
      inset: 0;
      background: rgba(0,0,0,0.55);
      z-index: 45;
      display: none;
      align-items: flex-end;
    }
    .sheet-backdrop.open {
      display: flex;
    }
    .bottom-sheet {
      width: 100%;
      max-height: 82%;
      background: var(--colorSurface);
      color: var(--colorTextPrimary);
      border-radius: 22px 22px 0 0;
      padding: 16px;
      overflow-y: auto;
    }
    .sheet-handle {
      width: 38px;
      height: 4px;
      background: var(--colorDivider);
      border-radius: 4px;
      margin: 0 auto 12px;
    }

    /* Android System Navigation Bar */
    .android-nav-bar {
      height: 28px;
      background: var(--colorBackground);
      border-top: 1px solid var(--colorDivider);
      display: flex;
      align-items: center;
      justify-content: space-around;
      padding: 0 48px;
      flex-shrink: 0;
      z-index: 40;
    }
    .nav-glyph {
      color: var(--colorTextSecondary);
      font-size: 13px;
      cursor: pointer;
      user-select: none;
    }
  </style>
</head>
<body>
  <!-- Top Bar explaining Android Project & providing Direct APK Download -->
  <div class="studio-bar">
    <div class="studio-bar-left">
      <span class="apk-badge">ANDROID JAVA + XML</span>
      <span><strong>NoteVault DIU</strong> (<code>com.jptechgenius.notevault</code>) · Room DB v3 · Material 3</span>
    </div>
    <div class="studio-actions">
      <button class="btn-studio-sec" onclick="replaySplash()">Replay SplashActivity</button>
      <button class="btn-studio-sec" onclick="openDocsSheet()">View Audit & API Docs</button>
      <a class="btn-apk" href="/api/download-apk" download>⬇ Download NoteVault-DIU-debug.apk</a>
    </div>
  </div>

  <!-- Interactive Android Device Frame -->
  <div class="phone-frame" id="phoneFrame">
    <!-- Status Bar -->
    <div class="android-status-bar">
      <span>9:41</span>
      <div class="camera-punch"></div>
      <span>5G · 100% 🔋</span>
    </div>

    <!-- SplashActivity (activity_splash.xml) -->
    <div class="splash-screen" id="splashScreen">
      <div class="splash-logo-box">📘</div>
      <h1 style="font-size:22px; font-weight:700; margin-bottom:6px;">NoteVault DIU</h1>
      <p style="font-size:13px; opacity:0.9; margin-bottom:18px;">Daffodil International University Academic Resource & Offline Study Platform</p>
      <div style="font-size:11px; background:rgba(255,255,255,0.15); padding:5px 12px; border-radius:99px;">v2.0 · Room v3 · Offline-First PDF Engine</div>
    </div>

    <!-- HomeActivity Toolbar -->
    <div class="android-toolbar">
      <div class="toolbar-title" id="tvToolbarTitle">NoteVault DIU</div>
      <div class="toolbar-icons">
        <button class="icon-btn" id="btnOfflineSim" title="Simulate Offline Mode">📶 Online</button>
        <button class="icon-btn" id="btnLang">বাংলা</button>
        <button class="icon-btn" id="btnTheme">🌙</button>
      </div>
    </div>

    <!-- HomeActivity Scrollable Content (activity_home.xml) -->
    <div class="android-scroll-body">
      <!-- Academic Dashboard Header -->
      <div class="dashboard-header">
        <div>
          <div class="greeting-sub" id="tvGreetingSubtitle">Welcome back,</div>
          <div class="greeting-name" id="tvGreetingName">DIU Scholar</div>
          <div class="context-row">
            <span class="context-pill" id="tvAcademicContext">DIU • SWE • SWE-43 • L2-T1</span>
            <button class="context-change-btn" id="btnChangeContext">Change</button>
          </div>
        </div>
        <div class="avatar-card" id="cardProfileAvatar" title="Academic Profile">DS</div>
      </div>

      <!-- Offline Banner (offlineBanner) -->
      <div id="offlineBanner" style="display:none; margin:4px 16px 8px; background:#FEE2E2; color:#991B1B; border:1px solid #FCA5A5; border-radius:10px; padding:8px 12px; font-size:11.5px; font-weight:700; text-align:center;">
        ⚡ Offline Mode — Showing cached Room DB notes & downloaded PDFs
      </div>

      <!-- Smart Search Card (etSearch) -->
      <div class="search-box">
        <span>🔍</span>
        <input type="text" id="etSearch" placeholder="Search course code (SWE211), title, or teacher…" />
        <button id="btnClearSearch" style="display:none; background:none; border:none; cursor:pointer; color:var(--colorTextMuted); font-weight:700;">✕</button>
      </div>

      <!-- Quick Resource Category Filter Strip (chipGroupCategories) -->
      <div class="chip-strip" id="chipGroupCategories">
        <button class="m3-chip checked" data-cat="ALL">All</button>
        <button class="m3-chip" data-cat="Official Syllabus">Official Syllabus</button>
        <button class="m3-chip" data-cat="Lecture Notes">Lecture Notes</button>
        <button class="m3-chip" data-cat="Lecture Slides">Lecture Slides</button>
        <button class="m3-chip" data-cat="Suggestions">Suggestions</button>
        <button class="m3-chip" data-cat="Lab Manuals">Lab Manuals</button>
      </div>

      <!-- Continue Studying Card (cardContinueStudying) -->
      <div class="continue-card" id="cardContinueStudying">
        <div>
          <div class="continue-kicker" id="lblContinue">CONTINUE STUDYING</div>
          <div class="continue-title" id="tvContinueTitle">SWE211 — Database Management Systems</div>
          <div class="continue-sub" id="tvContinueSub">Resume Page 14 of 42</div>
        </div>
        <button class="btn-outline-sm" id="btnResumeStudying">Read PDF</button>
      </div>

      <!-- Study Planner, Exam Countdown & My Courses Bar (cardStudyWorkspace) -->
      <div class="workspace-card">
        <div class="workspace-title" id="lblPlannerTitle">📅 Study Planner & Exam Countdown</div>
        <div class="workspace-summary" id="tvExamCountdownSummary">Next: SWE211 Midterm Exam — 6 days left</div>
        <div class="workspace-btns">
          <button class="btn-text-sm" id="btnAddStudyTask">+ Add Exam / Task</button>
          <button class="btn-text-sm" id="btnManageMyCourses">My Courses (SWE211)</button>
        </div>
      </div>

      <!-- Dynamic Filter Spinners (layout_filter_notes.xml) -->
      <div class="filter-spinners">
        <select id="spinnerDept">
          <option value="">All Departments</option>
          <option value="SWE">SWE</option>
          <option value="CSE">CSE</option>
          <option value="EEE">EEE</option>
          <option value="BBA">BBA</option>
        </select>
        <select id="spinnerCourse">
          <option value="">All Courses</option>
          <option value="SWE112">SWE112</option>
          <option value="SWE122">SWE122</option>
          <option value="SWE211">SWE211</option>
          <option value="SWE223">SWE223</option>
          <option value="CSE1102">CSE1102</option>
          <option value="CSE226">CSE226</option>
        </select>
        <button class="icon-btn" id="btnResetSpinners">Reset</button>
      </div>

      <!-- 5-Destination Navigation Bar (tabLayout) -->
      <div class="m3-tablayout" id="tabLayout">
        <button class="m3-tab active" data-tab="HOME">Home</button>
        <button class="m3-tab" data-tab="EXPLORE">Questions</button>
        <button class="m3-tab" data-tab="DOWNLOADS">Downloads</button>
        <button class="m3-tab" data-tab="BOOKMARKS">Bookmarks</button>
        <button class="m3-tab" data-tab="PROFILE">Profile</button>
      </div>

      <!-- RecyclerView (recyclerNotes) -->
      <div class="recycler-list" id="recyclerNotes"></div>
    </div>

    <!-- ExtendedFloatingActionButton (fabUpload) -->
    <button class="android-efab" id="fabUpload">＋ Contribute PDF</button>

    <!-- NoteDetailActivity (activity_note_detail.xml) -->
    <div class="detail-activity" id="detailActivity">
      <div class="detail-toolbar">
        <button class="icon-btn" style="background:rgba(255,255,255,0.18); color:#fff; border:none;" onclick="closeDetailActivity()">← Back</button>
        <div class="detail-title" id="detailToolbarTitle">Note Title</div>
        <button class="icon-btn" style="background:rgba(255,255,255,0.18); color:#fff; border:none;" id="btnDetailInfo">ⓘ Info</button>
      </div>
      <div style="padding:7px 12px; background:var(--colorSurfaceVariant); border-bottom:1px solid var(--colorDivider); display:flex; justify-content:space-between; align-items:center; font-size:11px; font-weight:700; gap:6px;">
        <span id="detailCourseBadge" style="color:var(--colorPrimary); white-space:nowrap; overflow:hidden; text-overflow:ellipsis;">SWE211 • Lecture Notes</span>
        <div style="display:flex; align-items:center; gap:5px; flex-shrink:0;">
          <button class="icon-btn" style="padding:2px 6px; font-size:10px;" onclick="adjustPdfZoom(-0.1)" title="Zoom Out">A-</button>
          <button class="icon-btn" style="padding:2px 6px; font-size:10px;" onclick="adjustPdfZoom(0.1)" title="Zoom In">A+</button>
          <button class="icon-btn" style="padding:2px 6px; font-size:10px;" onclick="togglePdfNightMode()" title="Reader Dark Paper">◐</button>
          <span id="tvPageIndicator" style="background:var(--colorPrimary); color:#fff; padding:2px 8px; border-radius:99px;">Page 1 / 42</span>
        </div>
      </div>
      <!-- Multi-Page Android PDFView Canvas -->
      <div class="pdf-view-wrapper">
        <div class="pdf-Viewport" id="pdfViewport"></div>
        <div class="pdf-scroll-handle" id="pdfScrollHandle">1 / 42</div>
      </div>
      <div style="padding:10px 12px; background:var(--colorSurface); border-top:1px solid var(--colorDivider); display:flex; gap:6px; align-items:center;">
        <button class="btn-card-sec" onclick="stepDetailPage(-1)">◀ Prev</button>
        <button class="btn-card-sec" onclick="stepDetailPage(1)">Next ▶</button>
        <button class="btn-card-primary" id="btnDetailDownload">⬇ Save Offline</button>
        <button class="btn-card-sec" id="btnDetailRate">★ Rate</button>
      </div>
    </div>

    <!-- Bottom Sheet Overlay (bottom_sheet_note_info.xml / Dialogs) -->
    <div class="sheet-backdrop" id="sheetBackdrop">
      <div class="bottom-sheet" id="sheetContent"></div>
    </div>

    <!-- Android System Navigation Bar -->
    <div class="android-nav-bar">
      <span class="nav-glyph" onclick="handleAndroidBack()">◀</span>
      <span class="nav-glyph" onclick="closeDetailActivity(); closeSheet();">●</span>
      <span class="nav-glyph">■</span>
    </div>
  </div>

  <script>
    const state = {
      tab: 'HOME',
      lang: 'en',
      theme: 'light',
      offline: false,
      university: 'DIU',
      department: 'SWE',
      batch: 'SWE-43',
      semester: 'L2-T1',
      category: 'ALL',
      search: '',
      filterDept: '',
      filterCourse: '',
      resources: [],
      questions: [],
      downloadedIds: new Set(JSON.parse(localStorage.getItem('nv_downloaded') || '[1]')),
      bookmarkedIds: new Set(JSON.parse(localStorage.getItem('nv_bookmarks') || '[1, 4]')),
      readingProgress: JSON.parse(localStorage.getItem('nv_progress') || '{"id":1,"title":"Database Management Systems — Relational Algebra & SQL Normalization Notes","page":1,"total":42}'),
      plannerTasks: JSON.parse(localStorage.getItem('nv_tasks') || '[{"title":"SWE211 Midterm Exam","course":"SWE211","days":6}]'),
      activeNote: null,
      activePage: 1,
      pdfZoom: 1,
      pdfNight: false
    };

    // Hide SplashActivity after 1.1 seconds on initial load
    setTimeout(() => {
      document.getElementById('splashScreen').classList.add('hidden');
    }, 1100);

    window.replaySplash = function() {
      const sp = document.getElementById('splashScreen');
      sp.classList.remove('hidden');
      setTimeout(() => sp.classList.add('hidden'), 1400);
    };

    function saveLocal() {
      localStorage.setItem('nv_downloaded', JSON.stringify([...state.downloadedIds]));
      localStorage.setItem('nv_bookmarks', JSON.stringify([...state.bookmarkedIds]));
      localStorage.setItem('nv_progress', JSON.stringify(state.readingProgress));
      localStorage.setItem('nv_tasks', JSON.stringify(state.plannerTasks));
    }

    async function loadAll() {
      const [notes, questions] = await Promise.all([
        fetch('/api/notes').then(r => r.json()),
        fetch('/api/questions').then(r => r.json())
      ]);
      state.resources = notes;
      state.questions = questions;
      renderApp();
    }

    function renderApp() {
      const isBn = state.lang === 'bn';
      document.getElementById('tvToolbarTitle').textContent = isBn ? 'নোটভল্ট ডিআইইউ' : 'NoteVault DIU';
      document.getElementById('tvGreetingSubtitle').textContent = isBn ? 'স্বাগতম,' : 'Welcome back,';
      document.getElementById('tvGreetingName').textContent = isBn ? 'ডিআইইউ শিক্ষার্থী' : 'DIU Scholar';
      document.getElementById('btnLang').textContent = isBn ? 'EN' : 'বাংলা';
      document.getElementById('fabUpload').textContent = isBn ? '＋ নোট আপলোড' : '＋ Contribute PDF';

      document.getElementById('tvAcademicContext').textContent =
        \`\${state.university} • \${state.department} • \${state.batch} • \${state.semester}\`;

      const prog = state.readingProgress;
      document.getElementById('tvContinueTitle').textContent = prog.title;
      document.getElementById('tvContinueSub').textContent = \`Resume Page \${prog.page} of \${prog.total}\`;

      if (state.plannerTasks.length > 0) {
        const t = state.plannerTasks[0];
        document.getElementById('tvExamCountdownSummary').textContent = \`Next: \${t.title} (\${t.course}) — \${t.days} days remaining\`;
      }

      const recycler = document.getElementById('recyclerNotes');

      if (state.tab === 'PROFILE') {
        recycler.innerHTML = \`
          <div class="android-note-card">
            <div class="subject-chip" style="display:inline-block; margin-bottom:8px;">ACADEMIC PROFILE & STATS</div>
            <div class="note-title">\${state.university} — Dept. of \${state.department}</div>
            <div class="note-desc">Batch: \${state.batch} · Semester: \${state.semester}</div>
            <div class="note-meta">
              <span>Offline Saved PDFs: <strong>\${state.downloadedIds.size}</strong></span>
              <span>Bookmarks: <strong>\${state.bookmarkedIds.size}</strong></span>
            </div>
            <div class="note-actions">
              <button class="btn-card-primary" onclick="openContextSheet()">Edit Academic Profile</button>
              <button class="btn-card-sec" onclick="openDocsSheet()">Platform Docs</button>
            </div>
          </div>
        \`;
        return;
      }

      if (state.tab === 'EXPLORE') {
        const qFiltered = state.questions.filter(q => {
          if (state.filterDept && q.department_short !== state.filterDept) return false;
          if (state.search && !q.course_name.toLowerCase().includes(state.search.toLowerCase())) return false;
          return true;
        });
        recycler.innerHTML = qFiltered.map(q => \`
          <div class="android-note-card">
            <div class="note-top-row">
              <span class="subject-chip">\${q.department_short} • \${q.semester_name}</span>
              <span class="downloaded-badge" style="background:#FEF3C7; color:#92400E;">\${q.exam_type}</span>
            </div>
            <div class="note-title" onclick="openQuestionDetail(\${q.id}, '\${q.course_name.replace(/'/g, "")}')">\${q.course_name}</div>
            <div class="note-desc">Verified DIU Past Question Paper · \${q.submissions_count} student submissions</div>
            <div class="note-meta">
              <span>📅 \${q.created_at}</span>
              <span>PDF Exam Paper</span>
            </div>
            <div class="note-actions">
              <button class="btn-card-primary" onclick="openQuestionDetail(\${q.id}, '\${q.course_name.replace(/'/g, "")}')">Open Exam PDF</button>
            </div>
          </div>
        \`).join('');
        return;
      }

      let list = state.resources.slice();
      if (state.offline || state.tab === 'DOWNLOADS') {
        list = list.filter(r => state.downloadedIds.has(r.id));
      } else if (state.tab === 'BOOKMARKS') {
        list = list.filter(r => state.bookmarkedIds.has(r.id));
      }

      if (state.category !== 'ALL') {
        list = list.filter(r => r.resourceCategory === state.category);
      }
      if (state.filterDept) {
        list = list.filter(r => r.departmentName === state.filterDept);
      }
      if (state.filterCourse) {
        list = list.filter(r => r.courseCode === state.filterCourse);
      }
      if (state.search) {
        const q = state.search.toLowerCase();
        list = list.filter(r =>
          r.title.toLowerCase().includes(q) ||
          r.courseCode.toLowerCase().includes(q) ||
          r.courseName.toLowerCase().includes(q) ||
          r.userName.toLowerCase().includes(q)
        );
      }

      if (list.length === 0) {
        recycler.innerHTML = \`
          <div class="android-note-card" style="text-align:center; padding:28px 16px;">
            <div style="font-size:32px; margin-bottom:8px;">📄</div>
            <div class="note-title">No Academic Resources Found</div>
            <div class="note-desc">Try clearing your search or switching department/course filters.</div>
          </div>
        \`;
        return;
      }

      recycler.innerHTML = list.map(r => {
        const isDown = state.downloadedIds.has(r.id);
        const isBook = state.bookmarkedIds.has(r.id);
        const sizeMb = (r.fileSize / (1024 * 1024)).toFixed(1) + ' MB';
        return \`
          <div class="android-note-card \${isDown ? 'downloaded' : ''}">
            <div class="note-top-row">
              <span class="subject-chip">\${r.courseCode} • \${r.departmentName} • \${r.resourceCategory}</span>
              \${isDown ? '<span class="downloaded-badge">✓ Offline Ready</span>' : ''}
            </div>
            <div class="note-title" onclick="openDetailActivity(\${r.id})">\${r.title}</div>
            <div class="note-desc">\${r.description || ''}</div>
            <div class="note-meta">
              <span>👤 \${r.userName} \${r.isVerified ? '✓' : ''}</span>
              <span>★ \${r.ratingAvg || '5.0'} (\${r.ratingCount}) · \${sizeMb}</span>
            </div>
            <div class="note-actions">
              <button class="btn-card-primary" onclick="openDetailActivity(\${r.id})">Read PDF</button>
              <button class="btn-card-sec" onclick="toggleDownload(\${r.id})">\${isDown ? '✓ Saved' : '⬇ Offline'}</button>
              <button class="btn-card-sec" onclick="toggleBookmark(\${r.id})">\${isBook ? '★ Saved' : '☆ Save'}</button>
              <button class="btn-card-sec" onclick="openInfoSheet(\${r.id})">ⓘ</button>
            </div>
          </div>
        \`;
      }).join('');
    }

    // Generate Rich Multi-Page Academic Content for Android PDFView
    function buildPdfSheetHtml(note, pageNum, totalPages) {
      const code = (note.courseCode || 'SWE211').toUpperCase();
      let topicTitle = \`Chapter \${pageNum}: Fundamental Concepts & Core Architecture\`;
      let bodyHtml = '';

      if (code === 'SWE211') {
        const topics = [
          {
            t: 'Relational Database Architecture & ER Modeling',
            b: \`
              <p style="font-size:11.5px; line-height:1.5; margin-bottom:8px;">
                An <strong>Entity-Relationship (ER) Model</strong> represents data entities, attributes, and cardinality constraints (1:1, 1:N, M:N) before mapping to relational tables.
              </p>
              <table class="pdf-table">
                <tr><th>Entity</th><th>Primary Key</th><th>Attributes</th></tr>
                <tr><td>Student</td><td>student_id (PK)</td><td>name, batch, dept_id (FK)</td></tr>
                <tr><td>Course</td><td>course_code (PK)</td><td>title, credit_hours, semester</td></tr>
                <tr><td>Enrollment</td><td>(student_id, course_code)</td><td>grade, enrolled_at</td></tr>
              </table>
              <div class="pdf-code-block">-- SQL DDL Schema Definition (DIU SWE211 Lab)
CREATE TABLE students (
  student_id VARCHAR(16) PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  batch_name VARCHAR(16) DEFAULT 'SWE-43',
  cgpa DECIMAL(3,2) CHECK (cgpa BETWEEN 0.00 AND 4.00)
);</div>\`
          },
          {
            t: 'Relational Algebra Operators (σ, π, ⋈, ÷)',
            b: \`
              <p style="font-size:11.5px; line-height:1.5; margin-bottom:8px;">
                <strong>Relational Algebra</strong> is a procedural query language that takes one or two relations as input and produces a new relation without side effects.
              </p>
              <table class="pdf-table">
                <tr><th>Operator</th><th>Notation</th><th>SQL Equivalent</th></tr>
                <tr><td>Selection</td><td>σ_{dept='SWE'}(Students)</td><td>WHERE dept = 'SWE'</td></tr>
                <tr><td>Projection</td><td>π_{name, cgpa}(Students)</td><td>SELECT name, cgpa</td></tr>
                <tr><td>Natural Join</td><td>Students ⋈ Departments</td><td>NATURAL JOIN / INNER JOIN</td></tr>
              </table>
              <div class="pdf-code-block">-- Equivalent SQL Query for Relational Expression:
SELECT s.student_id, s.full_name, c.course_title
FROM students s
INNER JOIN enrollments e ON s.student_id = e.student_id
INNER JOIN courses c ON e.course_code = c.course_code
WHERE s.batch_name = 'SWE-43' AND e.semester = 'L2-T1';</div>\`
          },
          {
            t: 'Functional Dependencies & 1NF → 2NF → 3NF → BCNF',
            b: \`
              <p style="font-size:11.5px; line-height:1.5; margin-bottom:8px;">
                <strong>Boyce-Codd Normal Form (BCNF):</strong> For every non-trivial functional dependency <code>X → Y</code> in relation <code>R</code>, <code>X</code> must be a superkey of <code>R</code>.
              </p>
              <table class="pdf-table">
                <tr><th>Normal Form</th><th>Eliminates Anomaly</th><th>Core Requirement</th></tr>
                <tr><td>1NF</td><td>Repeating Groups</td><td>Atomic attribute values</td></tr>
                <tr><td>2NF</td><td>Partial Dependency</td><td>1NF + No non-prime attr depends on part of PK</td></tr>
                <tr><td>3NF</td><td>Transitive Dependency</td><td>2NF + No X → Y → Z non-key chain</td></tr>
                <tr><td>BCNF</td><td>All FD Redundancy</td><td>Every determinant X is a candidate/super key</td></tr>
              </table>
              <div class="pdf-code-block">Given R(A, B, C, D) with FDs: { A → BC, C → D, B → A }
1. Closure (A)+ = {A, B, C, D} => A is a Candidate Key
2. Closure (B)+ = {B, A, C, D} => B is a Candidate Key
3. C → D violates 3NF/BCNF because C is not a superkey.
Decomposition: R1(C, D) and R2(A, B, C) [Lossless & Dependency Preserving]</div>\`
          },
          {
            t: 'ACID Transactions, Concurrency & B+ Tree Indexing',
            b: \`
              <p style="font-size:11.5px; line-height:1.5; margin-bottom:8px;">
                Database transactions guarantee <strong>Atomicity, Consistency, Isolation, and Durability (ACID)</strong> using Write-Ahead Logging (WAL) and Two-Phase Locking (2PL).
              </p>
              <div class="pdf-code-block">BEGIN TRANSACTION;
  UPDATE accounts SET balance = balance - 5000 WHERE acc_id = 101;
  UPDATE accounts SET balance = balance + 5000 WHERE acc_id = 202;
COMMIT; -- Durable in redo log; ROLLBACK on constraint failure</div>
              <p style="font-size:11px; line-height:1.45; margin-top:6px;">
                • <strong>B+ Tree Index Lookup:</strong> O(log_b N) disk I/O operations; all data pointers reside in linked leaf nodes for fast range scans (<code>BETWEEN</code>).
              </p>\`
          }
        ];
        const pick = topics[(pageNum - 1) % topics.length];
        topicTitle = \`Section \${pageNum}: \${pick.t}\`;
        bodyHtml = pick.b;
      } else if (code === 'CSE226') {
        topicTitle = \`Lecture \${pageNum}: Numerical Root Finding & Linear Systems\`;
        bodyHtml = \`
          <p style="font-size:11.5px; line-height:1.5; margin-bottom:8px;">
            <strong>Newton-Raphson Iterative Formula:</strong> Rapidly approximates roots of a differentiable real function <code>f(x) = 0</code> with quadratic convergence O(h²).
          </p>
          <div class="pdf-code-block">x_{n+1} = x_n - [ f(x_n) / f'(x_n) ]   where f'(x_n) ≠ 0
Example: Solve f(x) = x³ - 2x - 5 = 0,  f'(x) = 3x² - 2,  x₀ = 2.0</div>
          <table class="pdf-table">
            <tr><th>Iteration (n)</th><th>x_n</th><th>f(x_n)</th><th>x_{n+1}</th><th>|Error|</th></tr>
            <tr><td>0</td><td>2.00000</td><td>-1.00000</td><td>2.10000</td><td>0.10000</td></tr>
            <tr><td>1</td><td>2.10000</td><td>0.06100</td><td>2.09457</td><td>0.00543</td></tr>
            <tr><td>2</td><td>2.09457</td><td>0.00002</td><td>2.09455</td><td>0.00002</td></tr>
          </table>\`;
      } else if (code === 'exam' || note.isQuestion) {
        topicTitle = \`DIU Examination Paper — Section \${pageNum}\`;
        bodyHtml = \`
          <p style="font-size:11.5px; line-height:1.5; margin-bottom:8px;">
            <strong>Time:</strong> 1.5 Hours &nbsp;|&nbsp; <strong>Full Marks:</strong> 25 &nbsp;|&nbsp; <em>Answer all questions. Figures in the right margin indicate full marks.</em>
          </p>
          <div class="pdf-code-block">Q\${pageNum}(a) [CO1 — 4 Marks]
Explain the architectural difference between 3NF and BCNF with a concrete relational schema example from a university registration system.

Q\${pageNum}(b) [CO2 — 5 Marks]
Given the relation R(CourseCode, Section, TeacherID, RoomNo, TimeSlot), identify all candidate keys and normalize R up to BCNF.</div>
          <p style="font-size:11px; color:#4B635E;">
            <strong>Solution Guideline (Verified by Faculty):</strong> Show closure sets for each determinant before decomposing relations.
          </p>\`;
      } else {
        topicTitle = \`Module \${pageNum}: \${note.courseName || note.title} — Lecture & Lab Guide\`;
        bodyHtml = \`
          <p style="font-size:11.5px; line-height:1.5; margin-bottom:8px;">
            \${note.description || 'Outcome-Based Education (OBE) aligned lecture resource for Daffodil International University students.'}
          </p>
          <div class="pdf-code-block">// Verified Reference Implementation — \${code} (Page \${pageNum})
public class AcademicResourceModule_\${pageNum} {
    public static void executeVerification(String courseCode) {
        System.out.println("DIU Verified Study Module: " + courseCode + " [Page \${pageNum}]");
    }
}</div>
          <table class="pdf-table">
            <tr><th>Assessment Component</th><th>Weight</th><th>OBE Target</th></tr>
            <tr><td>Midterm Examination</td><td>25%</td><td>CO1, CO2</td></tr>
            <tr><td>Lab / Project Defense</td><td>25%</td><td>CO3</td></tr>
            <tr><td>Final Examination</td><td>40%</td><td>CO2, CO3, CO4</td></tr>
          </table>\`;
      }

      return \`
        <div class="pdf-sheet" id="pdfPage_\${pageNum}" data-page="\${pageNum}" style="zoom:\${state.pdfZoom};">
          <div>
            <div class="pdf-sheet-banner">
              <span>DAFFODIL INTERNATIONAL UNIVERSITY (DIU) • \${code}</span>
              <span>PAGE \${pageNum} OF \${totalPages}</span>
            </div>
            <div style="font-size:10px; font-weight:700; color:#086B61; margin-bottom:3px;">
              \${note.departmentName || 'SWE'} DEPARTMENT • \${note.resourceCategory || 'LECTURE NOTES'} • \${note.semesterName || 'L2-T1'}
            </div>
            <h3 style="font-size:13.5px; font-weight:700; margin-bottom:8px; line-height:1.35;">
              \${topicTitle}
            </h3>
            \${bodyHtml}
          </div>
          <div style="margin-top:12px; padding-top:8px; border-top:1px solid #DCE5E3; display:flex; justify-content:space-between; font-size:9.5px; color:#7B938E;">
            <span>Contributor: \${note.userName || 'DIU Faculty'} ✓</span>
            <span>NoteVault Offline PDF Engine • Sheet #\${pageNum}</span>
          </div>
        </div>
      \`;
    }

    function renderPdfDocumentInViewport(note, startPage) {
      const viewport = document.getElementById('pdfViewport');
      const total = Math.max(1, Number(note.totalPages || 12));
      const pagesHtml = [];
      for (let p = 1; p <= total; p++) {
        pagesHtml.push(buildPdfSheetHtml(note, p, total));
      }
      viewport.innerHTML = pagesHtml.join('');

      setTimeout(() => {
        const targetEl = document.getElementById('pdfPage_' + startPage);
        if (targetEl) {
          targetEl.scrollIntoView({ behavior: 'instant', block: 'start' });
        } else {
          viewport.scrollTop = 0;
        }
        updatePdfPageIndicators(startPage, total);
      }, 30);
    }

    function updatePdfPageIndicators(page, total) {
      document.getElementById('tvPageIndicator').textContent = \`Page \${page} / \${total}\`;
      const handle = document.getElementById('pdfScrollHandle');
      if (handle) {
        handle.textContent = \`\${page} / \${total}\`;
      }
    }

    window.adjustPdfZoom = function(delta) {
      state.pdfZoom = Math.min(1.4, Math.max(0.85, Number((state.pdfZoom + delta).toFixed(2))));
      document.querySelectorAll('#pdfViewport .pdf-sheet').forEach(el => {
        el.style.zoom = state.pdfZoom;
      });
    };

    window.togglePdfNightMode = function() {
      state.pdfNight = !state.pdfNight;
      document.getElementById('pdfViewport').classList.toggle('night-paper', state.pdfNight);
    };

    // Track scroll inside #pdfViewport just like Android PDFView DefaultScrollHandle
    document.getElementById('pdfViewport').addEventListener('scroll', function() {
      if (!state.activeNote) return;
      const viewport = this;
      const total = state.activeNote.totalPages || 12;
      const maxScroll = Math.max(1, viewport.scrollHeight - viewport.clientHeight);
      const ratio = Math.min(1, Math.max(0, viewport.scrollTop / maxScroll));

      const handle = document.getElementById('pdfScrollHandle');
      if (handle) {
        const topPx = 14 + ratio * Math.max(0, viewport.clientHeight - 52);
        handle.style.top = topPx + 'px';
      }

      const sheets = viewport.querySelectorAll('.pdf-sheet');
      let visiblePage = state.activePage;
      const viewMid = viewport.scrollTop + 140;
      sheets.forEach(sheet => {
        if (sheet.offsetTop <= viewMid) {
          visiblePage = Number(sheet.dataset.page || 1);
        }
      });

      if (visiblePage !== state.activePage) {
        state.activePage = visiblePage;
        updatePdfPageIndicators(state.activePage, total);
        if (!state.activeNote.isQuestion) {
          state.readingProgress = {
            id: state.activeNote.id,
            title: state.activeNote.title,
            page: state.activePage,
            total
          };
          saveLocal();
          renderApp();
        }
      }
    });

    // Detail Activity (NoteDetailActivity.java)
    window.openDetailActivity = function(id, resumePage) {
      const note = state.resources.find(r => r.id === id) || state.resources[0];
      state.activeNote = note;
      const total = note.totalPages || 24;
      state.activePage = resumePage || (state.readingProgress.id === note.id ? state.readingProgress.page : 1);

      document.getElementById('detailToolbarTitle').textContent = note.title;
      document.getElementById('detailCourseBadge').textContent = \`\${note.courseCode} • \${note.resourceCategory} • \${note.semesterName}\`;
      updatePdfPageIndicators(state.activePage, total);

      const isDown = state.downloadedIds.has(note.id);
      document.getElementById('btnDetailDownload').textContent = isDown ? '✓ Saved Offline' : '⬇ Save Offline';
      document.getElementById('btnDetailDownload').onclick = () => {
        toggleDownload(note.id);
        document.getElementById('btnDetailDownload').textContent = state.downloadedIds.has(note.id) ? '✓ Saved Offline' : '⬇ Save Offline';
      };
      document.getElementById('btnDetailInfo').onclick = () => openInfoSheet(note.id);
      document.getElementById('btnDetailRate').onclick = () => openRateSheet(note.id);

      state.readingProgress = { id: note.id, title: note.title, page: state.activePage, total };
      saveLocal();
      renderApp();

      document.getElementById('detailActivity').classList.add('open');
      renderPdfDocumentInViewport(note, state.activePage);
    };

    window.openQuestionDetail = function(id, title) {
      const qObj = state.questions.find(q => q.id === id) || {
        id,
        course_name: title,
        department_short: 'SWE',
        exam_type: 'MID TERM',
        semester_name: 'L2-T1'
      };
      const questionNote = {
        id: qObj.id,
        title: qObj.course_name,
        courseCode: 'exam',
        courseName: qObj.course_name,
        departmentName: qObj.department_short,
        resourceCategory: qObj.exam_type + ' PAPER',
        semesterName: qObj.semester_name,
        userName: 'DIU Exam Controller',
        totalPages: 6,
        isQuestion: true
      };
      state.activeNote = questionNote;
      state.activePage = 1;
      document.getElementById('detailToolbarTitle').textContent = title;
      document.getElementById('detailCourseBadge').textContent = \`\${qObj.department_short} • \${qObj.exam_type} • \${qObj.semester_name}\`;
      updatePdfPageIndicators(1, 6);
      document.getElementById('detailActivity').classList.add('open');
      renderPdfDocumentInViewport(questionNote, 1);
    };

    window.stepDetailPage = function(delta) {
      if (!state.activeNote) return;
      const total = state.activeNote.totalPages || 12;
      state.activePage = Math.min(total, Math.max(1, state.activePage + delta));
      updatePdfPageIndicators(state.activePage, total);

      const targetEl = document.getElementById('pdfPage_' + state.activePage);
      if (targetEl) {
        targetEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }

      if (!state.activeNote.isQuestion) {
        state.readingProgress = {
          id: state.activeNote.id,
          title: state.activeNote.title,
          page: state.activePage,
          total
        };
        saveLocal();
        renderApp();
      }
    };

    window.closeDetailActivity = function() {
      document.getElementById('detailActivity').classList.remove('open');
    };

    window.handleAndroidBack = function() {
      if (document.getElementById('sheetBackdrop').classList.contains('open')) {
        closeSheet();
      } else if (document.getElementById('detailActivity').classList.contains('open')) {
        closeDetailActivity();
      }
    };

    // Offline Download & Bookmark actions
    window.toggleDownload = async function(id) {
      if (state.downloadedIds.has(id)) {
        state.downloadedIds.delete(id);
      } else {
        state.downloadedIds.add(id);
        await fetch(\`/api/notes/\${id}/download\`, { method: 'POST' });
      }
      saveLocal();
      renderApp();
    };

    window.toggleBookmark = function(id) {
      if (state.bookmarkedIds.has(id)) {
        state.bookmarkedIds.delete(id);
      } else {
        state.bookmarkedIds.add(id);
      }
      saveLocal();
      renderApp();
    };

    // Bottom Sheet Info (bottom_sheet_note_info.xml)
    window.openInfoSheet = function(id) {
      const r = state.resources.find(x => x.id === id);
      if (!r) return;
      const sizeMb = (r.fileSize / (1024 * 1024)).toFixed(2) + ' MB';
      showSheet(\`
        <div class="sheet-handle"></div>
        <div style="font-size:11px; font-weight:700; color:var(--colorPrimary); margin-bottom:4px;">BOTTOM_SHEET_NOTE_INFO.XML</div>
        <div style="font-size:15px; font-weight:700; margin-bottom:10px;">\${r.title}</div>
        <div style="display:grid; gap:7px; font-size:12.5px; margin-bottom:14px;">
          <div><strong>University:</strong> \${r.universityName}</div>
          <div><strong>Course:</strong> \${r.courseCode} — \${r.courseName}</div>
          <div><strong>Department & Semester:</strong> \${r.departmentName} · \${r.semesterName} (\${r.batchName})</div>
          <div><strong>Category:</strong> \${r.resourceCategory}</div>
          <div><strong>Contributor:</strong> \${r.userName} \${r.isVerified ? '✓ Verified' : ''}</div>
          <div><strong>File Size & Pages:</strong> \${sizeMb} · \${r.totalPages} pages · \${r.viewCount} views</div>
        </div>
        <div style="display:flex; gap:8px;">
          <button class="btn-card-primary" onclick="closeSheet(); openRateSheet(\${r.id})">★ Rate / Review</button>
          <button class="btn-card-sec" onclick="closeSheet()">Close</button>
        </div>
      \`);
    };

    window.openRateSheet = async function(id) {
      const r = state.resources.find(x => x.id === id);
      const reviews = await fetch(\`/api/v1/resources/\${id}/reviews\`).then(res => res.json());
      showSheet(\`
        <div class="sheet-handle"></div>
        <div style="font-size:14px; font-weight:700; margin-bottom:8px;">Rate & Review: \${r ? r.courseCode : ''}</div>
        <select id="sheetRating" style="width:100%; padding:8px; border-radius:8px; border:1px solid var(--colorDivider); margin-bottom:8px; background:var(--colorBackground); color:var(--colorTextPrimary);">
          <option value="5">★★★★★ (5/5) — Accurate & Clear Exam Resource</option>
          <option value="4">★★★★☆ (4/5) — Very Helpful</option>
          <option value="3">★★★☆☆ (3/5) — Good</option>
        </select>
        <textarea id="sheetReviewText" rows="2" placeholder="Share feedback for fellow DIU students…" style="width:100%; padding:8px; border-radius:8px; border:1px solid var(--colorDivider); margin-bottom:8px; background:var(--colorBackground); color:var(--colorTextPrimary);"></textarea>
        <div style="display:flex; gap:8px; margin-bottom:12px;">
          <button class="btn-card-primary" onclick="submitSheetReview(\${id})">Submit Rating</button>
          <button class="btn-card-sec" onclick="closeSheet()">Cancel</button>
        </div>
        <div style="border-top:1px solid var(--colorDivider); padding-top:8px; font-size:12px;">
          <strong>Recent Reviews (\${reviews.length})</strong>
          \${reviews.map(rv => \`<div style="margin-top:6px;">★ \${rv.rating} · <strong>\${rv.reviewer_name}</strong>: \${rv.review_text}</div>\`).join('')}
        </div>
      \`);
    };

    window.submitSheetReview = async function(id) {
      const rating = Number(document.getElementById('sheetRating').value);
      const review_text = document.getElementById('sheetReviewText').value;
      await fetch(\`/api/v1/resources/\${id}/reviews\`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rating, review_text, reviewer_name: 'DIU Scholar (' + state.batch + ')' })
      });
      await loadAll();
      closeSheet();
    };

    window.openContextSheet = function() {
      showSheet(\`
        <div class="sheet-handle"></div>
        <div style="font-size:15px; font-weight:700; margin-bottom:10px;">Academic Context Preferences</div>
        <div style="display:grid; gap:8px; margin-bottom:12px; font-size:12px;">
          <label>University</label>
          <select id="ctxUni" style="padding:7px; border-radius:8px;">
            <option value="DIU">Daffodil International University (DIU)</option>
            <option value="BUET">BUET</option>
            <option value="DU">University of Dhaka (DU)</option>
            <option value="NSU">North South University (NSU)</option>
          </select>
          <label>Department</label>
          <select id="ctxDept" style="padding:7px; border-radius:8px;">
            <option value="SWE">SWE — Software Engineering</option>
            <option value="CSE">CSE — Computer Science</option>
            <option value="EEE">EEE — Electrical Engineering</option>
            <option value="BBA">BBA — Business Admin</option>
          </select>
          <label>Batch / Intake</label>
          <input id="ctxBatch" value="\${state.batch}" style="padding:7px; border-radius:8px; border:1px solid var(--colorDivider);" />
          <label>Semester</label>
          <select id="ctxSem" style="padding:7px; border-radius:8px;">
            <option value="L1-T1">L1-T1</option>
            <option value="L1-T2">L1-T2</option>
            <option value="L2-T1" selected>L2-T1</option>
            <option value="L2-T2">L2-T2</option>
            <option value="L3-T1">L3-T1</option>
          </select>
        </div>
        <div style="display:flex; gap:8px;">
          <button class="btn-card-primary" onclick="saveContextSheet()">Save Preferences</button>
          <button class="btn-card-sec" onclick="closeSheet()">Cancel</button>
        </div>
      \`);
    };

    window.saveContextSheet = function() {
      state.university = document.getElementById('ctxUni').value;
      state.department = document.getElementById('ctxDept').value;
      state.batch = document.getElementById('ctxBatch').value || 'SWE-43';
      state.semester = document.getElementById('ctxSem').value;
      closeSheet();
      renderApp();
    };

    window.openDocsSheet = async function(file = 'NOTEvault_IMPLEMENTATION_REPORT.md') {
      const data = await fetch('/api/docs?file=' + encodeURIComponent(file)).then(r => r.json());
      showSheet(\`
        <div class="sheet-handle"></div>
        <div style="font-size:14px; font-weight:700; margin-bottom:8px;">Project Deliverables & Architecture</div>
        <div style="display:flex; gap:4px; flex-wrap:wrap; margin-bottom:8px;">
          <button class="btn-card-sec" onclick="openDocsSheet('NOTEvault_AUDIT_REPORT.md')">Audit</button>
          <button class="btn-card-sec" onclick="openDocsSheet('NOTEvault_IMPLEMENTATION_REPORT.md')">Implementation</button>
          <button class="btn-card-sec" onclick="openDocsSheet('NOTEvault_API_DOCUMENTATION.md')">API Docs</button>
          <button class="btn-card-sec" onclick="openDocsSheet('NOTEvault_DATABASE_MIGRATIONS.md')">DB Migrations</button>
        </div>
        <pre style="font-size:10.5px; background:var(--colorBackground); padding:10px; border-radius:8px; max-height:320px; overflow-y:auto; white-space:pre-wrap;">\${data.content.replace(/</g, '&lt;')}</pre>
        <button class="btn-card-sec" style="width:100%; margin-top:8px;" onclick="closeSheet()">Close</button>
      \`);
    };

    function showSheet(html) {
      document.getElementById('sheetContent').innerHTML = html;
      document.getElementById('sheetBackdrop').classList.add('open');
    }
    window.closeSheet = function() {
      document.getElementById('sheetBackdrop').classList.remove('open');
    };

    // Wire UI Controls
    document.getElementById('btnChangeContext').onclick = openContextSheet;
    document.getElementById('cardProfileAvatar').onclick = () => {
      state.tab = 'PROFILE';
      document.querySelectorAll('.m3-tab').forEach(b => b.classList.toggle('active', b.dataset.tab === 'PROFILE'));
      renderApp();
    };

    document.getElementById('btnOfflineSim').onclick = () => {
      state.offline = !state.offline;
      document.getElementById('btnOfflineSim').textContent = state.offline ? '⚡ Offline' : '📶 Online';
      document.getElementById('offlineBanner').style.display = state.offline ? 'block' : 'none';
      renderApp();
    };

    document.getElementById('btnLang').onclick = () => {
      state.lang = state.lang === 'en' ? 'bn' : 'en';
      renderApp();
    };

    document.getElementById('btnTheme').onclick = () => {
      state.theme = state.theme === 'light' ? 'dark' : 'light';
      document.getElementById('phoneFrame').setAttribute('data-theme', state.theme);
      document.getElementById('btnTheme').textContent = state.theme === 'light' ? '🌙' : '☀️';
    };

    document.getElementById('btnResumeStudying').onclick = () => {
      openDetailActivity(state.readingProgress.id, state.readingProgress.page);
    };

    document.getElementById('btnAddStudyTask').onclick = () => {
      showSheet(\`
        <div class="sheet-handle"></div>
        <div style="font-size:15px; font-weight:700; margin-bottom:10px;">Add Study Task / Exam Countdown</div>
        <input id="newTaskTitle" placeholder="Exam Title (e.g. SWE223 Final Exam)" style="width:100%; padding:8px; border-radius:8px; border:1px solid var(--colorDivider); margin-bottom:8px;" />
        <input id="newTaskCourse" value="SWE211" placeholder="Course Code" style="width:100%; padding:8px; border-radius:8px; border:1px solid var(--colorDivider); margin-bottom:8px;" />
        <input id="newTaskDays" type="number" value="5" placeholder="Days left" style="width:100%; padding:8px; border-radius:8px; border:1px solid var(--colorDivider); margin-bottom:10px;" />
        <div style="display:flex; gap:8px;">
          <button class="btn-card-primary" onclick="saveNewTask()">Save to Room DB</button>
          <button class="btn-card-sec" onclick="closeSheet()">Cancel</button>
        </div>
      \`);
    };

    window.saveNewTask = function() {
      const title = document.getElementById('newTaskTitle').value.trim();
      const course = document.getElementById('newTaskCourse').value.trim() || 'SWE211';
      const days = Math.max(1, Number(document.getElementById('newTaskDays').value || 5));
      if (!title) return;
      state.plannerTasks.unshift({ title, course, days });
      saveLocal();
      closeSheet();
      renderApp();
    };

    document.getElementById('btnManageMyCourses').onclick = () => {
      state.filterCourse = state.filterCourse === 'SWE211' ? '' : 'SWE211';
      document.getElementById('spinnerCourse').value = state.filterCourse;
      renderApp();
    };

    document.getElementById('fabUpload').onclick = () => {
      showSheet(\`
        <div class="sheet-handle"></div>
        <div style="font-size:15px; font-weight:700; margin-bottom:8px;">Contribute Academic PDF Resource</div>
        <input id="upTitle" placeholder="PDF Resource Title (e.g. SWE211 SQL Joins Cheatsheet)" style="width:100%; padding:8px; border-radius:8px; border:1px solid var(--colorDivider); margin-bottom:8px;" />
        <select id="upCourse" style="width:100%; padding:8px; border-radius:8px; border:1px solid var(--colorDivider); margin-bottom:8px;">
          <option value="SWE211|Database Management Systems">SWE211 — Database Management Systems</option>
          <option value="SWE122|Data Structures & Algorithms">SWE122 — Data Structures & Algorithms</option>
          <option value="SWE223|Software Engineering">SWE223 — Software Engineering</option>
          <option value="CSE226|Numerical Methods">CSE226 — Numerical Methods</option>
        </select>
        <select id="upCat" style="width:100%; padding:8px; border-radius:8px; border:1px solid var(--colorDivider); margin-bottom:8px;">
          <option value="Lecture Notes">Lecture Notes</option>
          <option value="Lecture Slides">Lecture Slides</option>
          <option value="Official Syllabus">Official Syllabus</option>
          <option value="Suggestions">Suggestions</option>
          <option value="Lab Manuals">Lab Manuals</option>
        </select>
        <textarea id="upDesc" rows="2" placeholder="Topics covered & batch…" style="width:100%; padding:8px; border-radius:8px; border:1px solid var(--colorDivider); margin-bottom:10px;"></textarea>
        <div style="display:flex; gap:8px;">
          <button class="btn-card-primary" onclick="submitNewResource()">Upload PDF</button>
          <button class="btn-card-sec" onclick="closeSheet()">Cancel</button>
        </div>
      \`);
    };

    window.submitNewResource = async function() {
      const title = document.getElementById('upTitle').value.trim();
      if (!title) return;
      const [course_code, course_name] = document.getElementById('upCourse').value.split('|');
      const resource_category = document.getElementById('upCat').value;
      const description = document.getElementById('upDesc').value.trim();
      await fetch('/api/notes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title,
          description,
          course_code,
          course_name,
          department_name: state.department,
          resource_category,
          semester_name: state.semester,
          batch_name: state.batch
        })
      });
      await loadAll();
      closeSheet();
    };

    document.querySelectorAll('#chipGroupCategories .m3-chip').forEach(chip => {
      chip.onclick = () => {
        document.querySelectorAll('#chipGroupCategories .m3-chip').forEach(c => c.classList.remove('checked'));
        chip.classList.add('checked');
        state.category = chip.dataset.cat;
        renderApp();
      };
    });

    document.querySelectorAll('#tabLayout .m3-tab').forEach(tab => {
      tab.onclick = () => {
        document.querySelectorAll('#tabLayout .m3-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        state.tab = tab.dataset.tab;
        renderApp();
      };
    });

    document.getElementById('etSearch').oninput = (e) => {
      state.search = e.target.value.trim();
      document.getElementById('btnClearSearch').style.display = state.search ? 'inline' : 'none';
      renderApp();
    };

    document.getElementById('btnClearSearch').onclick = () => {
      state.search = '';
      document.getElementById('etSearch').value = '';
      document.getElementById('btnClearSearch').style.display = 'none';
      renderApp();
    };

    document.getElementById('spinnerDept').onchange = (e) => {
      state.filterDept = e.target.value;
      renderApp();
    };
    document.getElementById('spinnerCourse').onchange = (e) => {
      state.filterCourse = e.target.value;
      renderApp();
    };
    document.getElementById('btnResetSpinners').onclick = () => {
      state.filterDept = '';
      state.filterCourse = '';
      state.search = '';
      state.category = 'ALL';
      document.getElementById('spinnerDept').value = '';
      document.getElementById('spinnerCourse').value = '';
      document.getElementById('etSearch').value = '';
      document.querySelectorAll('#chipGroupCategories .m3-chip').forEach((c, idx) => c.classList.toggle('checked', idx === 0));
      renderApp();
    };

    loadAll();
  </script>
</body>
</html>`;
}

server.listen(PORT, '0.0.0.0', () => {
  console.log(`[NoteVault Android App Simulator] Listening on http://0.0.0.0:${PORT}`);
});
