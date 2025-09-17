# 数据库连接信息
$DB_HOST = "localhost"
$DB_PORT = "5432"
$DB_NAME = "testplatform"
$DB_USER = "postgres"
$DB_PASS = "root"

# 连接数据库并检查表结构
$env:PGPASSWORD = $DB_PASS
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c @"
-- 检查表是否存在
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name IN 
('test_suites', 'test_cases', 'test_executions', 'target_system_configs', 'test_suite_test_cases');

-- 检查test_cases表结构
SELECT column_name, data_type, is_nullable FROM information_schema.columns 
WHERE table_name = 'test_cases' ORDER BY ordinal_position;

-- 检查target_system_configs表结构
SELECT column_name, data_type, is_nullable FROM information_schema.columns 
WHERE table_name = 'target_system_configs' ORDER BY ordinal_position;

-- 检查test_suites表结构
SELECT column_name, data_type, is_nullable FROM information_schema.columns 
WHERE table_name = 'test_suites' ORDER BY ordinal_position;

-- 检查test_executions表结构
SELECT column_name, data_type, is_nullable FROM information_schema.columns 
WHERE table_name = 'test_executions' ORDER BY ordinal_position;
"@