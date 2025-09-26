import React from 'react';
import './ReportList.css';

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
  details: any[];
  createdAt: string;
}

interface ReportListProps {
  reports: TestReport[];
  onSelectReport: (report: TestReport) => void;
}

const ReportList: React.FC<ReportListProps> = ({ reports, onSelectReport }) => {
  const formatTime = (timeString: string) => {
    return new Date(timeString).toLocaleString();
  };

  const getPassRateColor = (passRate: number) => {
    if (passRate >= 90) return 'green';
    if (passRate >= 80) return 'yellow';
    return 'red';
  };

  return (
    <div className="report-list">
      <h3>测试报告列表</h3>
      <table>
        <thead>
          <tr>
            <th>报告名称</th>
            <th>执行时间</th>
            <th>总测试数</th>
            <th>通过率</th>
            <th>平均响应时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          {reports.map(report => (
            <tr key={report.id}>
              <td>{report.name}</td>
              <td>{formatTime(report.createdAt)}</td>
              <td>{report.summary.totalTests}</td>
              <td>
                <span className={`pass-rate ${getPassRateColor(report.summary.passRate)}`}>
                  {report.summary.passRate}%
                </span>
              </td>
              <td>{report.summary.averageResponseTime}ms</td>
              <td>
                <button 
                  className="view-btn" 
                  onClick={() => onSelectReport(report)}
                >
                  查看详情
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default ReportList;