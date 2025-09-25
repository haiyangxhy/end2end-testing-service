import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8180/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// 认证相关API
export const authAPI = {
  login: (credentials: { username: string; password: string }) =>
    api.post('/auth/login', credentials),
  register: (userData: { username: string; email: string; password: string }) =>
    api.post('/auth/register', userData),
  logout: () => api.post('/auth/logout'),
};

// 测试用例相关API
export const testCaseAPI = {
  getAll: () => api.get('/test-cases'),
  getById: (id: string) => api.get(`/test-cases/${id}`),
  create: (data: any) => api.post('/test-cases', data),
  update: (id: string, data: any) => api.put(`/test-cases/${id}`, data),
  delete: (id: string) => api.delete(`/test-cases/${id}`),
};

// 测试套件相关API
export const testSuiteAPI = {
  getAll: () => api.get('/test-suites'),
  getById: (id: string) => api.get(`/test-suites/${id}`),
  create: (data: any) => api.post('/test-suites', data),
  update: (id: string, data: any) => api.put(`/test-suites/${id}`, data),
  delete: (id: string) => api.delete(`/test-suites/${id}`),
};

// 测试执行相关API
export const testExecutionAPI = {
  getAll: () => api.get('/test-executions'),
  getById: (id: string) => api.get(`/test-executions/${id}`),
  create: (data: any) => api.post('/test-executions', data),
  execute: (suiteId: string) => api.post(`/test-executions/execute/${suiteId}`),
  start: (id: string) => api.post(`/test-executions/${id}/start`),
  stop: (id: string) => api.post(`/test-executions/${id}/stop`),
  getStatus: (id: string) => api.get(`/test-executions/${id}/status`),
};

// 测试报告相关API
export const testReportAPI = {
  getAll: () => api.get('/test-reports'),
  getById: (id: string) => api.get(`/test-reports/${id}`),
  generate: (executionId: string) => api.post(`/test-reports/generate/${executionId}`),
  exportJson: (id: string) => api.get(`/test-reports/${id}/export/json`),
  exportCsv: (id: string) => api.get(`/test-reports/${id}/export/csv`),
};

// 环境管理相关API
export const environmentAPI = {
  getAll: () => api.get('/environments'),
  getById: (id: string) => api.get(`/environments/${id}`),
  create: (data: any) => api.post('/environments', data),
  update: (id: string, data: any) => api.put(`/environments/${id}`, data),
  delete: (id: string) => api.delete(`/environments/${id}`),
  activate: (id: string) => api.post(`/environments/${id}/activate`),
  validate: (id: string) => api.post(`/environments/${id}/validate`),
};

// 变量管理相关API
export const variableAPI = {
  getAll: (environmentId?: string) => 
    api.get('/variables', { params: { environmentId } }),
  getById: (id: string) => api.get(`/variables/${id}`),
  create: (data: any) => api.post('/variables', data),
  update: (id: string, data: any) => api.put(`/variables/${id}`, data),
  delete: (id: string) => api.delete(`/variables/${id}`),
  replace: (data: { text: string; environmentId?: string }) => 
    api.post('/variables/replace', data),
  copy: (data: { variableId: string; targetEnvironmentId: string }) =>
    api.post('/variables/copy', data),
};


// 监控相关API
export const monitoringAPI = {
  getRealTimeData: () => api.get('/monitoring/realtime'),
  getStatistics: () => api.get('/monitoring/statistics'),
  generateComprehensiveReport: (suiteId: string, startTime?: string, endTime?: string) =>
    api.post('/monitoring/reports/comprehensive', null, {
      params: { suiteId, startTime, endTime }
    }),
  generateTrendReport: (suiteId: string, days: number = 7) =>
    api.get('/monitoring/reports/trend', {
      params: { suiteId, days }
    }),
  generatePerformanceReport: (suiteId: string, days: number = 7) =>
    api.get('/monitoring/reports/performance', {
      params: { suiteId, days }
    }),
  exportReportJson: (reportId: string) => api.get(`/monitoring/reports/${reportId}/export/json`),
  exportReportCsv: (reportId: string) => api.get(`/monitoring/reports/${reportId}/export/csv`),
  getConnectionCount: () => api.get('/monitoring/connections/count'),
  closeAllConnections: () => api.post('/monitoring/connections/close-all'),
};

// 目标系统配置相关API
export const targetSystemAPI = {
  getAll: () => api.get('/target-systems'),
  getById: (id: string) => api.get(`/target-systems/${id}`),
  create: (data: any) => api.post('/target-systems', data),
  update: (id: string, data: any) => api.put(`/target-systems/${id}`, data),
  delete: (id: string) => api.delete(`/target-systems/${id}`),
  testConnection: (id: string) => api.post(`/target-systems/${id}/test-connection`),
};

export default api;
