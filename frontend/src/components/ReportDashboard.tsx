import React, { useState, useEffect } from 'react';
import ReportList from './ReportList';

import ChartVisualization from './ChartVisualization';
import ReportDetail from './ReportDetail';
import './ReportDashboard.css';

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



const ReportDashboard: React.FC = () => {
  const [reports, setReports] = useState<TestReport[]>([]);
  const [selectedReport, setSelectedReport] = useState<TestReport | null>(null);

  // Mock data for demonstration
  useEffect(() => {
    const mockReports: TestReport[] = [
      {
        id: '1',
        executionId: '101',
        suiteId: '1',
        name: '用户管理API测试报告',
        summary: {
          totalTests: 100,
          passedTests: 85,
          failedTests: 10,
          skippedTests: 5,
          passRate: 85,
          averageResponseTime: 150,
          startTime: '2023-01-15T10:30:00Z',
          endTime: '2023-01-15T10:45:00Z'
        },
        details: [
          {
            testCaseId: '1',
            testCaseName: '用户登录接口测试',
            status: 'PASSED',
            errorMessage: '',
            responseTime: 120,
            startTime: '2023-01-15T10:30:00Z',
            endTime: '2023-01-15T10:30:01Z'
          },
          {
            testCaseId: '2',
            testCaseName: '用户注册接口测试',
            status: 'FAILED',
            errorMessage: '响应状态码不匹配',
            responseTime: 200,
            startTime: '2023-01-15T10:30:01Z',
            endTime: '2023-01-15T10:30:02Z'
          }
        ],
        createdAt: '2023-01-15T10:45:00Z'
      },
      {
        id: '2',
        executionId: '102',
        suiteId: '2',
        name: '订单流程测试报告',
        summary: {
          totalTests: 50,
          passedTests: 45,
          failedTests: 3,
          skippedTests: 2,
          passRate: 90,
          averageResponseTime: 220,
          startTime: '2023-01-16T14:20:00Z',
          endTime: '2023-01-16T14:35:00Z'
        },
        details: [],
        createdAt: '2023-01-16T14:35:00Z'
      }
    ];
    setReports(mockReports);
  }, []);

  const handleSelectReport = (report: TestReport) => {
    setSelectedReport(report);
  };

  const handleBackToList = () => {
    setSelectedReport(null);
  };

  return (
    <div className="report-dashboard">
      <div className="header">
        <h2>报告中心</h2>
      </div>
      
      {selectedReport ? (
        <ReportDetail report={selectedReport} onBack={handleBackToList} />
      ) : (
        <>
          <ChartVisualization reports={reports} />
          <ReportList reports={reports} onSelectReport={handleSelectReport} />
        </>
      )}
    </div>
  );
};

export default ReportDashboard;