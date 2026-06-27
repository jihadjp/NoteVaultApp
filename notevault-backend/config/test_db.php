<?php
// test_db.php
// ডাটাবেস কানেকশন টেস্ট করার জন্য ছোট স্ক্রিপ্ট

define('DB_HOST',     'localhost');
define('DB_NAME',     'jihadjp1_notevault_diu');
define('DB_USER',     'jihadjp1_notevault_diu');
define('DB_PASS',     'notevault_diu#jihadjp');

try {
    $dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=utf8mb4";
    $pdo = new PDO($dsn, DB_USER, DB_PASS);
    
    // কানেকশন সাকসেসফুল হলে এটি দেখাবে
    echo "<h1>কানেকশন সফল হয়েছে! (Connection Successful)</h1>";
    
    // ডাটাবেসের টেবিলগুলো চেক করা
    $stmt = $pdo->query("SHOW TABLES");
    $tables = $stmt->fetchAll(PDO::FETCH_COLUMN);
    
    echo "<h3>ডাটাবেসে টেবিলসমূহ:</h3>";
    echo "<ul>";
    foreach ($tables as $table) {
        echo "<li>" . $table . "</li>";
    }
    echo "</ul>";

} catch (PDOException $e) {
    // কানেকশন ফেইল করলে এরর মেসেজ দেখাবে
    echo "<h1 style='color:red;'>কানেকশন ফেইল করেছে!</h1>";
    echo "<p>এরর মেসেজ: " . $e->getMessage() . "</p>";
}
?>