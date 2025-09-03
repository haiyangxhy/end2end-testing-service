# API文档

## 测试套件管理

### 获取测试套件列表
```
GET /api/test-suites
```

### 创建测试套件
```
POST /api/test-suites
```

### 获取测试套件详情
```
GET /api/test-suites/{id}
```

### 更新测试套件
```
PUT /api/test-suites/{id}
```

### 删除测试套件
```
DELETE /api/test-suites/{id}
```

## 测试用例管理

### 获取测试用例列表
```
GET /api/test-cases
```

### 创建测试用例
```
POST /api/test-cases
```

### 获取测试用例详情
```
GET /api/test-cases/{id}
```

### 更新测试用例
```
PUT /api/test-cases/{id}
```

### 删除测试用例
```
DELETE /api/test-cases/{id}
```

## 测试执行

### 启动测试执行
```
POST /api/executions
```

### 获取执行状态
```
GET /api/executions/{id}
```

### 停止执行
```
DELETE /api/executions/{id}
```

## 报告管理

### 获取报告列表
```
GET /api/reports
```

### 获取报告详情
```
GET /api/reports/{id}
```

### 下载报告
```
GET /api/reports/{id}/download
```