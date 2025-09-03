# 开发指南

## 环境准备

### 后端环境
- Java 11+
- Maven 3.6+
- PostgreSQL 13
- MongoDB 4.4

### 前端环境
- Node.js 16+
- npm 8+

## 项目结构

```
end2end-testing-service/
├── backend/              # 后端服务
│   ├── src/main/java/    # Java源代码
│   ├── src/main/resources/ # 配置文件
│   └── pom.xml           # Maven配置
├── frontend/             # 前端应用
│   ├── src/              # React源代码
│   ├── package.json      # npm配置
│   └── tsconfig.json     # TypeScript配置
├── docker/               # Docker配置
├── kubernetes/           # Kubernetes配置
└── docs/                 # 文档
```

## 后端开发

### 构建项目
```bash
cd backend
mvn clean install
```

### 运行项目
```bash
mvn spring-boot:run
```

## 前端开发

### 安装依赖
```bash
cd frontend
npm install
```

### 运行开发服务器
```bash
npm start
```

### 构建生产版本
```bash
npm run build
```

## 数据库配置

### PostgreSQL
- 数据库名: testplatform
- 用户名: postgres
- 密码: postgres

### MongoDB
- 数据库名: testplatform
- 用户名: root
- 密码: mongodb

## 容器化部署

### 使用Docker Compose
```bash
cd docker
docker-compose up -d
```

### 使用Kubernetes
```bash
kubectl apply -f kubernetes/pv-pvc.yaml
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/ingress.yaml
```