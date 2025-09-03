import React from 'react';
import './ReportDetail.css';

interface TestReport {
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
  details: ReportDetail[];
  createdAt: string;
}

interface ReportDetail {
  testCaseId: string;
  testCaseName: string;
  status: string;
  errorMessage: string;
  responseTime: number;
  startTime: string;
  endTime: string;
}

interface ReportDetailProps {
  report: TestReport;
  onBack: () => void;
}

const ReportDetail: React.FC<ReportDetailProps> = ({ report, onBack }) => {
  const formatTime = (timeString: string) => {
    return new Date(timeString).toLocaleString();
  };

  const getStatusClass = (status: string) => {
    switch (status) {
      case 'PASSED': return 'status-passed';
      case 'FAILED': return 'status-failed';
      case 'SKIPPED': return 'status-skipped';
      default: return '';
    }
  };

  return (
    <div className="report-detail">
      <div className="header">
        <button className="back-btn" onClick={onBack}>返回列表</button>
        <h2>{report.name}</h2>
        <button className="download-btn">下载报告</button>
      </div>
      
      <div className="summary-section">
        <h3>执行摘要</h3>
        <div className="summary-cards">
          <div className="summary-card">
            <div className="card-value">{report.summary.totalTests}</div>
            <div className="card-label">总测试数</div>
          </div>
          <div className="summary-card">
            <div className="card-value passed">{report.summary.passedTests}</div>
            <div className="card-label">通过</div>
          </div>
          <div className="summary-card">
            <div className="card-value failed">{report.summary.failedTests}</div>
            <div className="card-label">失败</div>
          </div>
          <div className="summary-card">
            <div className="card-value skipped">{report.summary.skippedTests}</div>
            <div className="card-label">跳过</div>
          </div>
          <div className="summary-card">
            <div className="card-value">{report.summary.passRate}%</div>
            <div className="card-label">通过率</div>
          </div>
          <div className="summary-card">
            <div className="card-value">{report.summary.averageResponseTime}ms</div>
            <div className="card-label">平均响应时间</div>
          </div>
        </div>
        
        <div className="summary-info">
          <div className="info-item">
            <span className="label">执行开始时间:</span>
            <span className="value">{formatTime(report.summary.startTime)}</span>
          </div>
          <div className="info-item">
            <span className="label">执行结束时间:</span>
            <span className="value">{formatTime(report.summary.endTime)}</span>
          </div>
          <div className="info-item">
            <span className="label">报告生成时间:</span>
            <span className="value">{formatTime(report.createdAt)}</span>
          </div>
        </div>
      </div>
      
      <div className="details-section">
        <h3>详细结果</h3>
        <table>
          <thead>
            <tr>
              <th>测试用例</th>
              <th>状态</th>
              <th>响应时间</th>
              <th>开始时间</th>
              <th>结束时间</th>
              <th>错误信息</th>
            </tr>
          </thead>
          <tbody>
            {report.details.map(detail => (
              <tr key={detail.testCaseId}>
                <td>{detail.testCaseName}</td>
                <td>
                  <span className={`status-badge ${getStatusClass(detail.status)}`}>
                    {detail.status}
                  </span>
                </td>
                <td>{detail.responseTime}ms</td>
                <td>{formatTime(detail.startTime)}</td>
                <td>{formatTime(detail.endTime)}</td>
                <td className="error-message">{detail.errorMessage}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default ReportDetail;