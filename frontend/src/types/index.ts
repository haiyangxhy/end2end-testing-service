// 基础类型定义
export interface BaseEntity {
  id: string;
  createdAt: string;
  updatedAt: string;
}

// 用户相关类型
export interface User extends BaseEntity {
  username: string;
  email: string;
  fullName: string;
  role: 'ADMIN' | 'USER';
  isActive: boolean;
  lastLogin?: string;
}

// 测试用例配置相关类型
export interface TestCaseConfig {
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  endpoint: string;
  headers?: Record<string, string>;
  params?: Record<string, any>;
  body?: any;
  assertions?: TestAssertion[];
  extract?: Record<string, string>;
  timeout?: number;
  retries?: number;
}

export interface TestAssertion {
  type: 'statusCode' | 'jsonPath' | 'responseTime' | 'contains' | 'equals';
  path?: string;
  expected: any;
  operator?: 'equals' | 'notEquals' | 'greaterThan' | 'lessThan' | 'contains' | 'notContains' | 'EQUALS' | 'NOT_EQUALS' | 'CONTAINS' | 'NOT_CONTAINS';
}

// 测试用例相关类型
export interface TestCase extends BaseEntity {
  name: string;
  description?: string;
  // 移除type字段，测试用例类型由所属的测试套件决定
  config: string; // JSON字符串，存储TestCaseConfig
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'ACTIVE' | 'INACTIVE' | 'DRAFT';
  tags?: string;
  isActive: boolean;
  testSteps?: string;
  expectedResult?: string;
}

// 测试套件用例关联类型
export interface TestSuiteCase extends BaseEntity {
  id: string;
  suiteId: string;
  testCaseId: string;
  testCase?: TestCase; // 可选的测试用例详情，用于前端显示
  executionOrder: number;
  isEnabled: boolean;
  createdAt: string;
  updatedAt: string;
}

// 测试套件相关类型
export interface TestSuite extends BaseEntity {
  name: string;
  description?: string;
  type: 'API' | 'UI' | 'BUSINESS';
  testCases: string[]; // 向后兼容
  testSuiteCases?: TestSuiteCase[]; // 新的关联关系
}

// 测试执行相关类型
export interface TestExecution extends BaseEntity {
  suiteId: string;
  testSuiteName: string;
  testCaseId?: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  startTime: string;
  endTime?: string;
  result?: string;
  errorMessage?: string;
  duration?: number;
  progress?: number;
  environmentId?: string;
  executionLogs?: TestExecutionLog[];
}

export interface TestExecutionLog {
  id: string;
  executionId: string;
  testCaseId: string;
  step: string;
  timestamp: string;
  level: 'INFO' | 'WARN' | 'ERROR' | 'DEBUG';
  message: string;
  requestData?: any;
  responseData?: any;
  duration?: number;
}

export interface TestExecutionContext {
  environmentId: string;
  environment: TestEnvironment;
  authToken?: string;
  refreshToken?: string;
  tokenVersion?: string;
  variables: Record<string, any>;
  extractedData: Record<string, any>;
}

// 测试报告相关类型
export interface TestReport extends BaseEntity {
  executionId: string;
  suiteId: string;
  name: string;
  status: 'PASSED' | 'FAILED' | 'SKIPPED' | 'RUNNING';
  totalTests: number;
  passedTests: number;
  failedTests: number;
  skippedTests: number;
  passRate: number;
  averageResponseTime: number;
  startTime: string;
  endTime: string;
  details: ReportDetail[];
}

export interface ReportSummary {
  totalTests: number;
  passedTests: number;
  failedTests: number;
  skippedTests: number;
  passRate: number;
  averageResponseTime: number;
  startTime: string;
  endTime: string;
}

export interface ReportDetail {
  testCaseId: string;
  testCaseName: string;
  testType?: string;
  status: 'PASSED' | 'FAILED' | 'SKIPPED';
  message?: string;
  errorMessage?: string;
  errorDetails?: string;
  responseTime: number;
  startTime?: string;
  endTime?: string;
  timestamp: string;
  metadata?: string;
}

// 认证配置相关类型
export interface AuthConfig {
  type: 'jwt' | 'basic' | 'oauth2' | 'apiKey' | 'none';
  loginUrl?: string;
  credentials?: {
    username: string;
    password: string;
  };
  tokenField?: string;
  refreshTokenField?: string;
  tokenVersionField?: string;
  headerName?: string;
  headerFormat?: string;
  refreshUrl?: string;
  refreshMethod?: 'GET' | 'POST' | 'PUT';
  refreshParams?: Record<string, string>;
  expiresIn?: number;
  autoRefresh?: boolean;
  apiKey?: string;
  apiKeyHeader?: string;
  apiKeyValue?: string;
}

// 环境管理相关类型
export interface TestEnvironment extends BaseEntity {
  name: string;
  description?: string;
  type: 'DEVELOPMENT' | 'TESTING' | 'STAGING' | 'PRODUCTION';
  apiBaseUrl?: string;
  uiBaseUrl?: string;
  databaseConfig?: string;
  authConfig?: string; // JSON字符串，存储AuthConfig
  isActive: boolean;
  createdBy: string;
}

// 变量管理相关类型
export interface GlobalVariable extends BaseEntity {
  name: string;
  value: string;
  description?: string;
  environmentId: string;
  variableType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON';
  isEncrypted: boolean;
  createdBy: string;
}


// 监控相关类型
export interface RealTimeData {
  runningTests: number;
  runningTestDetails: Array<{
    id: string;
    suiteId: string;
    startTime: string;
    duration: number;
  }>;
  recentCompleted: number;
  recentFailed: number;
  timestamp: string;
}

export interface StatisticsReport {
  today: {
    total: number;
    passed: number;
    failed: number;
    successRate: number;
  };
  week: {
    total: number;
    passed: number;
    failed: number;
    successRate: number;
  };
  overall: {
    total: number;
    passed: number;
    failed: number;
    successRate: number;
  };
  timestamp: string;
}

export interface TrendReport {
  dailyStats: Record<string, {
    total: number;
    passed: number;
    failed: number;
  }>;
  totalExecutions: number;
  successRate: number;
  averageExecutionTime: number;
}

export interface PerformanceReport {
  minExecutionTime: number;
  maxExecutionTime: number;
  avgExecutionTime: number;
  totalExecutions: number;
}

// 告警相关类型
export interface AlertRule {
  name: string;
  displayName: string;
  description: string;
  type: 'METRIC' | 'DURATION' | 'CONSECUTIVE';
  condition: string;
  checkInterval: number;
}

export interface AlertRecord {
  ruleName: string;
  message: string;
  timestamp: string;
  level: 'INFO' | 'WARNING' | 'ERROR' | 'CRITICAL';
  lastChecked: string;
}

// API响应类型
export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  error?: string;
}

export interface PaginatedResponse<T = any> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// 表单相关类型
export interface LoginForm {
  username: string;
  password: string;
}

export interface RegisterForm {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
}

export interface TestCaseForm {
  name: string;
  description?: string;
  // 移除type字段，测试用例类型由所属的测试套件决定
  config: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  tags?: string; // 统一为string类型，与TestCase接口保持一致
}

export interface TestSuiteForm {
  name: string;
  description?: string;
  type: 'API' | 'UI' | 'BUSINESS';
  testCases: string[];
}

export interface ScheduledTask extends BaseEntity {
  name: string;
  description?: string;
  suiteId: string;
  environmentId: string;
  cronExpression: string;
  isActive: boolean;
  lastRun?: string;
  nextRun?: string;
}

export interface ScheduledTaskForm {
  name: string;
  description?: string;
  suiteId: string;
  environmentId: string;
  cronExpression: string;
  isActive: boolean;
}

export interface EnvironmentForm {
  name: string;
  description?: string;
  apiBaseUrl?: string;
  uiBaseUrl?: string;
  databaseConfig?: string;
  authConfig?: string;
  isActive?: boolean;
}

export interface VariableForm {
  name: string;
  value: string;
  description?: string;
  environmentId: string;
  variableType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON';
  isEncrypted: boolean;
}

// 图表相关类型
export interface ChartData {
  name: string;
  value: number;
  [key: string]: any;
}

export interface TimeSeriesData {
  time: string;
  value: number;
  [key: string]: any;
}

// 组件Props类型
export interface BaseComponentProps {
  className?: string;
  style?: React.CSSProperties;
}

export interface TableColumn<T = any> {
  key: string;
  title: string;
  dataIndex: string;
  render?: (value: any, record: T, index: number) => React.ReactNode;
  sorter?: boolean;
  filterable?: boolean;
  width?: number;
  fixed?: 'left' | 'right';
}

// 路由相关类型
export interface RouteConfig {
  path: string;
  component: React.ComponentType;
  exact?: boolean;
  protected?: boolean;
  roles?: string[];
}

// 主题相关类型
export interface Theme {
  primaryColor: string;
  secondaryColor: string;
  successColor: string;
  warningColor: string;
  errorColor: string;
  infoColor: string;
  backgroundColor: string;
  textColor: string;
  borderColor: string;
}

// 通知相关类型
export interface Notification {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  title: string;
  message: string;
  duration?: number;
  timestamp: string;
}

// 搜索和过滤相关类型
export interface SearchParams {
  query?: string;
  filters?: Record<string, any>;
  sort?: {
    field: string;
    order: 'asc' | 'desc';
  };
  pagination?: {
    page: number;
    size: number;
  };
}

// 导出相关类型
export interface ExportOptions {
  format: 'json' | 'csv' | 'excel';
  filename?: string;
  includeHeaders?: boolean;
  dateRange?: {
    start: string;
    end: string;
  };
}
