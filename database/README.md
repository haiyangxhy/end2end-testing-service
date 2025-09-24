# 端到端测试平台数据库

本目录包含端到端测试平台的数据库初始化脚本和部署工具。

## 文件说明

### 核心文件
- `init.sql` - 数据库初始化脚本，创建所有表、索引、约束和函数
- `sample-data.sql` - 示例数据脚本，包含测试用户、测试用例等示例数据
- `database.conf` - 数据库配置文件

### 部署脚本
- `deploy.sh` - Linux/macOS 部署脚本
- `deploy.bat` - Windows 部署脚本

## 快速开始

### 1. 环境准备

确保已安装 PostgreSQL 12+ 版本：

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

**CentOS/RHEL:**
```bash
sudo yum install postgresql-server postgresql-contrib
sudo postgresql-setup initdb
sudo systemctl enable postgresql
sudo systemctl start postgresql
```

**Windows:**
下载并安装 PostgreSQL: https://www.postgresql.org/download/windows/

**macOS:**
```bash
brew install postgresql
brew services start postgresql
```

### 2. 部署数据库

#### Linux/macOS
```bash
# 给脚本执行权限
chmod +x deploy.sh

# 使用默认配置部署
./deploy.sh

# 使用自定义配置部署
./deploy.sh -n mydb -u myuser -p mypass

# 不导入示例数据
./deploy.sh --no-sample-data
```

#### Windows
```cmd
REM 使用默认配置部署
deploy.bat

REM 使用自定义配置部署
deploy.bat -n mydb -u myuser -p mypass

REM 不导入示例数据
deploy.bat --no-sample-data
```

### 3. 手动部署

如果自动部署脚本不可用，可以手动执行：

```bash
# 1. 创建数据库和用户
psql -h localhost -U postgres -c "CREATE DATABASE testplatform;"
psql -h localhost -U postgres -c "CREATE USER testplatform WITH PASSWORD 'testplatform123';"
psql -h localhost -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE testplatform TO testplatform;"
psql -h localhost -U postgres -c "ALTER USER testplatform CREATEDB;"

# 2. 执行初始化脚本
psql -h localhost -U testplatform -d testplatform -f init.sql

# 3. 导入示例数据（可选）
psql -h localhost -U testplatform -d testplatform -f sample-data.sql
```

## 数据库结构

### 核心表

1. **用户认证相关**
   - `users` - 用户表
   - `user_sessions` - 用户会话表

2. **环境配置相关**
   - `test_environments` - 测试环境表
   - `global_variables` - 全局变量表

3. **测试用例相关**
   - `test_suites` - 测试套件表
   - `test_cases` - 测试用例表
   - `test_suite_cases` - 套件用例关联表

4. **测试执行相关**
   - `test_executions` - 测试执行表
   - `test_case_executions` - 用例执行结果表

5. **测试报告相关**
   - `test_reports` - 测试报告表

6. **定时任务相关**
   - `scheduled_tasks` - 定时任务表
   - `task_execution_history` - 任务执行历史表

### 视图

- `test_suite_stats` - 测试套件统计视图
- `test_execution_stats` - 测试执行统计视图

### 函数

- `update_updated_at_column()` - 自动更新 updated_at 字段
- `cleanup_expired_sessions()` - 清理过期会话
- `update_scheduled_task_next_run()` - 更新定时任务下次执行时间

## 配置说明

### 环境变量

可以通过环境变量自定义配置：

```bash
export DB_NAME=my_testplatform
export DB_USER=my_user
export DB_PASSWORD=my_password
export BACKUP_DIR=/path/to/backups
```

### 数据库配置

编辑 `database.conf` 文件来调整数据库连接参数：

```ini
# 数据库连接信息
DB_HOST=localhost
DB_PORT=5432
DB_NAME=testplatform
DB_USER=testplatform
DB_PASSWORD=testplatform123

# 连接池配置
DB_MAX_CONNECTIONS=20
DB_MIN_CONNECTIONS=5
DB_CONNECTION_TIMEOUT=30000
DB_IDLE_TIMEOUT=600000
```

## 示例数据

示例数据包含：

- **用户**: 1个管理员用户，3个测试用户
- **环境**: 4个测试环境（默认、开发、测试、预生产）
- **变量**: 10个全局变量
- **套件**: 5个测试套件（API、UI、业务流程）
- **用例**: 11个测试用例（API、UI、业务流程）
- **任务**: 4个定时任务
- **执行**: 4个测试执行记录
- **报告**: 3个测试报告

## 维护操作

### 备份数据库

```bash
# 创建备份
pg_dump -h localhost -U testplatform -d testplatform > backup_$(date +%Y%m%d_%H%M%S).sql

# 恢复备份
psql -h localhost -U testplatform -d testplatform < backup_file.sql
```

### 清理过期数据

```sql
-- 清理过期会话
SELECT cleanup_expired_sessions();

-- 清理30天前的执行记录
DELETE FROM test_executions WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '30 days';

-- 清理30天前的报告
DELETE FROM test_reports WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '30 days';
```

### 性能优化

```sql
-- 更新表统计信息
ANALYZE;

-- 重建索引
REINDEX DATABASE testplatform;

-- 查看慢查询
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;
```

## 故障排除

### 常见问题

1. **连接被拒绝**
   ```
   psql: error: connection to server at "localhost" (127.0.0.1), port 5432 failed: Connection refused
   ```
   解决：检查 PostgreSQL 服务是否启动

2. **认证失败**
   ```
   psql: error: FATAL: password authentication failed for user "testplatform"
   ```
   解决：检查用户名和密码是否正确

3. **权限不足**
   ```
   psql: error: FATAL: permission denied for database "testplatform"
   ```
   解决：确保用户有数据库访问权限

4. **表已存在**
   ```
   ERROR: relation "users" already exists
   ```
   解决：删除现有数据库或使用不同的数据库名称

### 日志查看

```bash
# 查看 PostgreSQL 日志
sudo tail -f /var/log/postgresql/postgresql-*.log

# 查看数据库连接
psql -h localhost -U postgres -c "SELECT * FROM pg_stat_activity;"
```

## 开发说明

### 添加新表

1. 在 `init.sql` 中添加表定义
2. 添加相应的索引和约束
3. 更新示例数据脚本（如需要）
4. 测试部署脚本

### 修改现有表

1. 创建新的迁移脚本
2. 测试迁移脚本
3. 更新部署脚本

### 版本控制

- 使用语义化版本号
- 每个版本创建对应的迁移脚本
- 保持向后兼容性

## 许可证

本项目采用 MIT 许可证。详见 LICENSE 文件。
