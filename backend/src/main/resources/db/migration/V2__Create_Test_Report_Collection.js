// MongoDB脚本用于初始化test_reports集合
// 由于MongoDB是无模式的，我们只需要确保集合存在

// 创建test_reports集合（如果不存在）
db.createCollection("test_reports");

// 创建索引以提高查询性能
db.test_reports.createIndex({ "execution_id": 1 });
db.test_reports.createIndex({ "suite_id": 1 });
db.test_reports.createIndex({ "created_at": 1 });