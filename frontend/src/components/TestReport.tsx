import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import './TestReport.css';

interface ITestReport {
  id: string;
  executionId: string;
  suiteId: string;
  name: string;
  summary: {
    totalTests: number;
    passedTests: number;
    failedTests: number;
    skippedTests: number;
    passRate: number;
    averageResponseTime: number;
    startTime: string;
    endTime: string;
  };
  details: Array<{
    testCaseId?: string;
    testCaseName: string;
    testType?: string;
    status: string;
    responseTime: number;
    message?: string;
    errorDetails?: string;
    startTime?: string;
    endTime?: string;
  }>;
  createdAt: string;
}

interface TestReportProps {
  executionId: string;
}

const TestReport: React.FC<TestReportProps> = ({ executionId }) => {
  const [report, setReport] = useState<ITestReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchReport = useCallback(async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      
      // 首先尝试获取现有报告
      const response = await axios.get(`http://localhost:8180/api/test-reports/execution/${executionId}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      if (response.data && response.data.length > 0) {
        setReport(response.data[0]);
      } else {
        // 如果没有现有报告，则生成新报告
        const generateResponse = await axios.post(`http://localhost:8180/api/test-reports/generate/${executionId}`, {}, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        setReport(generateResponse.data);
      }
    } catch (err) {
      console.error('获取测试报告失败:', err);
      setError('获取测试报告失败');
    } finally {
      setLoading(false);
    }
  }, [executionId]);

  useEffect(() => {
    fetchReport();
  }, [executionId, fetchReport]);

  if (loading) {
    return <div className="test-report">加载中...</div>;
  }

  if (error) {
    return <div className="test-report error">{error}</div>;
  }

  if (!report) {
    return <div className="test-report">未找到测试报告</div>;
  }

  return (
    <div className="test-report">
      <h2>{report.name}</h2>
      
      <div className="report-summary">
        <h3>测试摘要</h3>
        <div className="summary-grid">
          <div className="summary-item">
            <span className="label">总测试数:</span>
            <span className="value">{report.summary.totalTests}</span>
          </div>
          <div className="summary-item">
            <span className="label">通过:</span>
            <span className="value passed">{report.summary.passedTests}</span>
          </div>
          <div className="summary-item">
            <span className="label">失败:</span>
            <span className="value failed">{report.summary.failedTests}</span>
          </div>
          <div className="summary-item">
            <span className="label">跳过:</span>
            <span className="value">{report.summary.skippedTests}</span>
          </div>
          <div className="summary-item">
            <span className="label">通过率:</span>
            <span className="value">{report.summary.passRate.toFixed(2)}%</span>
          </div>
          <div className="summary-item">
            <span className="label">平均响应时间:</span>
            <span className="value">{report.summary.averageResponseTime}ms</span>
          </div>
          <div className="summary-item">
            <span className="label">开始时间:</span>
            <span className="value">{report.summary.startTime}</span>
          </div>
          <div className="summary-item">
            <span className="label">结束时间:</span>
            <span className="value">{report.summary.endTime}</span>
          </div>
        </div>
      </div>
      
      <div className="report-details">
        <h3>详细结果</h3>
        <table className="details-table">
          <thead>
              <tr>
                <th>测试用例</th>
                <th>测试类型</th>
                <th>状态</th>
                <th>响应时间</th>
                <th>信息</th>
              </tr>
            </thead>
          <tbody>
            {report.details.map((detail, index) => (
              <tr key={index}>
                <td>{detail.testCaseName}</td>
                <td>{detail.testType || '-'}</td>
                <td className={detail.status === '通过' ? 'status-passed' : 'status-failed'}>
                  {detail.status}
                </td>
                <td>{detail.responseTime}ms</td>
                <td className="error-message">
                  {detail.errorDetails || (detail.message || '-')}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default TestReport;