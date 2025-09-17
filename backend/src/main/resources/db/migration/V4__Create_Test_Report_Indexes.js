// MongoDB脚本用于为test_reports集合创建索引

// 确保test_reports集合存在
db.createCollection("test_reports");

// 为test_reports集合创建索引
db.test_reports.createIndex({ "execution_id": 1 });
db.test_reports.createIndex({ "suite_id": 1 });
db.test_reports.createIndex({ "created_at": -1 });