# 端到端测试平台 API 接口规范

## 1. 接口概述

### 1.1 基础信息
- **基础URL**: `http://localhost:8080/api`
- **认证方式**: JWT Bearer Token
- **数据格式**: JSON
- **字符编码**: UTF-8

### 1.2 通用响应格式

#### 成功响应
```json
{
  "success": true,
  "data": {},
  "message": "操作成功",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

#### 错误响应
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "错误描述",
    "details": "详细错误信息"
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 1.3 分页响应格式
```json
{
  "success": true,
  "data": {
    "content": [],
    "totalElements": 100,
    "totalPages": 10,
    "currentPage": 1,
    "size": 10,
    "first": true,
    "last": false
  }
}
```

## 2. 认证接口

### 2.1 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

**响应示例:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "admin-001",
      "username": "admin",
      "email": "admin@testplatform.com",
      "role": "ADMIN",
      "fullName": "系统管理员"
    },
    "expiresIn": 3600
  }
}
```

### 2.2 用户登出
```http
POST /api/auth/logout
Authorization: Bearer {token}
```

### 2.3 刷新Token
```http
POST /api/auth/refresh
Authorization: Bearer {token}
```

### 2.4 获取当前用户信息
```http
GET /api/auth/me
Authorization: Bearer {token}
```

## 3. 用户管理接口

### 3.1 获取用户列表
```http
GET /api/users?page=1&size=10&search={keyword}&role={role}
Authorization: Bearer {token}
```

### 3.2 创建用户
```http
POST /api/users
Authorization: Bearer {token}
Content-Type: application/json

{
  "username": "newuser",
  "email": "newuser@example.com",
  "password": "password123",
  "fullName": "新用户",
  "role": "USER"
}
```

### 3.3 更新用户
```http
PUT /api/users/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "email": "updated@example.com",
  "fullName": "更新后的用户名",
  "role": "USER",
  "isActive": true
}
```

### 3.4 删除用户
```http
DELETE /api/users/{id}
Authorization: Bearer {token}
```

## 4. 环境配置接口

### 4.1 获取环境列表
```http
GET /api/environments?page=1&size=10&active={true|false}
Authorization: Bearer {token}
```

### 4.2 创建环境
```http
POST /api/environments
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "测试环境",
  "description": "用于测试的环境",
  "apiBaseUrl": "http://test-api.example.com",
  "uiBaseUrl": "http://test-ui.example.com",
  "databaseConfig": {
    "host": "localhost",
    "port": 5432,
    "database": "testdb"
  },
  "authConfig": {
    "type": "JWT",
    "tokenUrl": "/api/auth/login"
  }
}
```

### 4.3 更新环境
```http
PUT /api/environments/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

### 4.4 激活/停用环境
```http
PATCH /api/environments/{id}/toggle
Authorization: Bearer {token}
```

### 4.5 删除环境
```http
DELETE /api/environments/{id}
Authorization: Bearer {token}
```

## 5. 全局变量接口

### 5.1 获取变量列表
```http
GET /api/variables?environmentId={id}&page=1&size=10
Authorization: Bearer {token}
```

### 5.2 创建变量
```http
POST /api/variables
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "apiTimeout",
  "value": "30000",
  "description": "API请求超时时间",
  "environmentId": "env-001",
  "variableType": "NUMBER",
  "isEncrypted": false
}
```

### 5.3 更新变量
```http
PUT /api/variables/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

### 5.4 删除变量
```http
DELETE /api/variables/{id}
Authorization: Bearer {token}
```

## 6. 测试套件接口

### 6.1 获取套件列表
```http
GET /api/test-suites?page=1&size=10&search={keyword}&type={type}&environmentId={id}
Authorization: Bearer {token}
```

### 6.2 创建套件
```http
POST /api/test-suites
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "用户管理测试套件",
  "description": "用户管理相关功能的测试套件",
  "suiteType": "API",
  "environmentId": "env-001",
  "executionOrder": "SEQUENTIAL",
  "stopOnFailure": true,
  "timeoutSeconds": 3600,
  "retryCount": 0,
  "tags": ["api", "user-management"]
}
```

### 6.3 获取套件详情
```http
GET /api/test-suites/{id}
Authorization: Bearer {token}
```

### 6.4 更新套件
```http
PUT /api/test-suites/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

### 6.5 删除套件
```http
DELETE /api/test-suites/{id}
Authorization: Bearer {token}
```

### 6.6 获取套件统计信息
```http
GET /api/test-suites/{id}/stats
Authorization: Bearer {token}
```

## 7. 测试用例接口

### 7.1 获取用例列表
```http
GET /api/test-cases?suiteId={id}&page=1&size=10&type={type}&priority={priority}
Authorization: Bearer {token}
```

### 7.2 创建用例
```http
POST /api/test-cases
Authorization: Bearer {token}
Content-Type: application/json

{
  "suiteId": "suite-001",
  "name": "创建用户API测试",
  "description": "测试创建用户接口的功能",
  "testType": "API",
  "priority": "HIGH",
  "config": {
    "method": "POST",
    "url": "/api/users",
    "headers": {
      "Content-Type": "application/json"
    },
    "body": "{\"username\": \"testuser\", \"email\": \"test@example.com\"}",
    "assertions": [
      {
        "type": "statusCode",
        "expected": 201
      },
      {
        "type": "responseBody",
        "expression": "$.data.username",
        "expected": "testuser"
      }
    ],
    "extractors": [
      {
        "type": "json",
        "expression": "$.data.id",
        "variable": "userId"
      }
    ]
  },
  "preconditions": "系统已启动，数据库连接正常",
  "expectedResult": "返回201状态码，用户创建成功",
  "tags": ["api", "user", "create"]
}
```

### 7.3 获取用例详情
```http
GET /api/test-cases/{id}
Authorization: Bearer {token}
```

### 7.4 更新用例
```http
PUT /api/test-cases/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

### 7.5 删除用例
```http
DELETE /api/test-cases/{id}
Authorization: Bearer {token}
```

### 7.6 复制用例
```http
POST /api/test-cases/{id}/copy
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "复制的测试用例",
  "suiteId": "suite-002"
}
```

### 7.7 批量操作用例
```http
POST /api/test-cases/batch
Authorization: Bearer {token}
Content-Type: application/json

{
  "action": "delete|activate|deactivate|move",
  "testCaseIds": ["case-001", "case-002"],
  "targetSuiteId": "suite-002"
}
```

## 8. 测试套件用例关联接口

### 8.1 获取套件中的用例
```http
GET /api/test-suites/{id}/test-cases
Authorization: Bearer {token}
```

### 8.2 添加用例到套件
```http
POST /api/test-suites/{id}/test-cases
Authorization: Bearer {token}
Content-Type: application/json

{
  "testCaseId": "case-001",
  "executionOrder": 1,
  "isEnabled": true
}
```

### 8.3 更新用例在套件中的顺序
```http
PUT /api/test-suites/{id}/test-cases/order
Authorization: Bearer {token}
Content-Type: application/json

{
  "testCaseOrders": [
    {"testCaseId": "case-001", "executionOrder": 1},
    {"testCaseId": "case-002", "executionOrder": 2}
  ]
}
```

### 8.4 从套件中移除用例
```http
DELETE /api/test-suites/{suiteId}/test-cases/{testCaseId}
Authorization: Bearer {token}
```

## 9. 测试执行接口

### 9.1 执行测试套件
```http
POST /api/test-executions
Authorization: Bearer {token}
Content-Type: application/json

{
  "suiteId": "suite-001",
  "environmentId": "env-001",
  "executionType": "MANUAL",
  "variables": {
    "customVar": "customValue"
  }
}
```

### 9.2 获取执行列表
```http
GET /api/test-executions?page=1&size=10&suiteId={id}&status={status}&startDate={date}&endDate={date}
Authorization: Bearer {token}
```

### 9.3 获取执行详情
```http
GET /api/test-executions/{id}
Authorization: Bearer {token}
```

### 9.4 获取执行结果
```http
GET /api/test-executions/{id}/results
Authorization: Bearer {token}
```

### 9.5 停止执行
```http
POST /api/test-executions/{id}/stop
Authorization: Bearer {token}
```

### 9.6 重新执行
```http
POST /api/test-executions/{id}/retry
Authorization: Bearer {token}
```

### 9.7 获取执行日志
```http
GET /api/test-executions/{id}/logs
Authorization: Bearer {token}
```

## 10. 测试报告接口

### 10.1 获取报告列表
```http
GET /api/test-reports?page=1&size=10&suiteId={id}&executionId={id}&startDate={date}&endDate={date}
Authorization: Bearer {token}
```

### 10.2 获取报告详情
```http
GET /api/test-reports/{id}
Authorization: Bearer {token}
```

### 10.3 生成报告
```http
POST /api/test-reports/generate
Authorization: Bearer {token}
Content-Type: application/json

{
  "executionId": "exec-001",
  "reportType": "EXECUTION",
  "format": "HTML|PDF|JSON"
}
```

### 10.4 下载报告
```http
GET /api/test-reports/{id}/download?format={format}
Authorization: Bearer {token}
```

### 10.5 获取报告统计
```http
GET /api/test-reports/statistics?suiteId={id}&startDate={date}&endDate={date}
Authorization: Bearer {token}
```

## 11. 定时任务接口

### 11.1 获取任务列表
```http
GET /api/scheduled-tasks?page=1&size=10&active={true|false}
Authorization: Bearer {token}
```

### 11.2 创建定时任务
```http
POST /api/scheduled-tasks
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "每日API测试",
  "description": "每天凌晨2点执行API测试套件",
  "suiteId": "suite-001",
  "environmentId": "env-001",
  "cronExpression": "0 0 2 * * ?"
}
```

### 11.3 更新定时任务
```http
PUT /api/scheduled-tasks/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

### 11.4 启用/禁用任务
```http
PATCH /api/scheduled-tasks/{id}/toggle
Authorization: Bearer {token}
```

### 11.5 立即执行任务
```http
POST /api/scheduled-tasks/{id}/execute
Authorization: Bearer {token}
```

### 11.6 删除任务
```http
DELETE /api/scheduled-tasks/{id}
Authorization: Bearer {token}
```

### 11.7 获取任务执行历史
```http
GET /api/scheduled-tasks/{id}/history?page=1&size=10
Authorization: Bearer {token}
```

## 12. 系统管理接口

### 12.1 获取系统状态
```http
GET /api/system/status
Authorization: Bearer {token}
```

### 12.2 获取系统统计
```http
GET /api/system/statistics
Authorization: Bearer {token}
```

### 12.3 清理过期数据
```http
POST /api/system/cleanup
Authorization: Bearer {token}
Content-Type: application/json

{
  "cleanupType": "executions|reports|sessions",
  "olderThanDays": 30
}
```

### 12.4 导出数据
```http
POST /api/system/export
Authorization: Bearer {token}
Content-Type: application/json

{
  "exportType": "test-cases|test-suites|executions",
  "format": "JSON|CSV|EXCEL",
  "filters": {
    "suiteId": "suite-001",
    "startDate": "2024-01-01",
    "endDate": "2024-01-31"
  }
}
```

### 12.5 导入数据
```http
POST /api/system/import
Authorization: Bearer {token}
Content-Type: multipart/form-data

{
  "file": "file",
  "importType": "test-cases|test-suites",
  "options": {
    "updateExisting": true,
    "createNew": true
  }
}
```

## 13. 错误码说明

| 错误码 | HTTP状态码 | 说明 |
|--------|------------|------|
| AUTH_001 | 401 | 未认证或Token无效 |
| AUTH_002 | 401 | Token已过期 |
| AUTH_003 | 403 | 权限不足 |
| VALIDATION_001 | 400 | 请求参数验证失败 |
| VALIDATION_002 | 400 | 必填参数缺失 |
| NOT_FOUND_001 | 404 | 资源不存在 |
| NOT_FOUND_002 | 404 | 用户不存在 |
| NOT_FOUND_003 | 404 | 测试套件不存在 |
| NOT_FOUND_004 | 404 | 测试用例不存在 |
| CONFLICT_001 | 409 | 用户名已存在 |
| CONFLICT_002 | 409 | 邮箱已存在 |
| SERVER_001 | 500 | 服务器内部错误 |
| SERVER_002 | 500 | 数据库连接失败 |
| SERVER_003 | 500 | 测试执行失败 |

## 14. 使用示例

### 14.1 完整的测试流程示例

```bash
# 1. 登录获取Token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# 2. 创建测试环境
curl -X POST http://localhost:8080/api/environments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "name": "测试环境",
    "apiBaseUrl": "http://test-api.example.com",
    "uiBaseUrl": "http://test-ui.example.com"
  }'

# 3. 创建测试套件
curl -X POST http://localhost:8080/api/test-suites \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "name": "API测试套件",
    "suiteType": "API",
    "environmentId": "env-001"
  }'

# 4. 创建测试用例
curl -X POST http://localhost:8080/api/test-cases \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "suiteId": "suite-001",
    "name": "用户API测试",
    "testType": "API",
    "config": {
      "method": "GET",
      "url": "/api/users",
      "assertions": [{"type": "statusCode", "expected": 200}]
    }
  }'

# 5. 执行测试
curl -X POST http://localhost:8080/api/test-executions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "suiteId": "suite-001",
    "environmentId": "env-001"
  }'

# 6. 查看执行结果
curl -X GET http://localhost:8080/api/test-executions/{executionId}/results \
  -H "Authorization: Bearer {token}"
```

## 15. 注意事项

1. **认证**: 除登录接口外，所有接口都需要在请求头中携带有效的JWT Token
2. **分页**: 列表接口支持分页，默认每页10条记录
3. **时间格式**: 所有时间字段使用ISO 8601格式 (YYYY-MM-DDTHH:mm:ssZ)
4. **文件上传**: 支持multipart/form-data格式的文件上传
5. **错误处理**: 所有错误都会返回统一的错误格式
6. **限流**: API接口有请求频率限制，超出限制会返回429状态码
7. **版本控制**: API支持版本控制，当前版本为v1
