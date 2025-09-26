import React from 'react';
import './ChartVisualization.css';

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

interface ChartVisualizationProps {
  reports: TestReport[];
}

const ChartVisualization: React.FC<ChartVisualizationProps> = ({ reports }) => {
  // Calculate overall statistics
  const totalTests = reports.reduce((sum, report) => sum + report.summary.totalTests, 0);
  const passedTests = reports.reduce((sum, report) => sum + report.summary.passedTests, 0);
  const failedTests = reports.reduce((sum, report) => sum + report.summary.failedTests, 0);
  const skippedTests = reports.reduce((sum, report) => sum + report.summary.skippedTests, 0);
  
  const passRate = totalTests > 0 ? Math.round((passedTests / totalTests) * 100) : 0;
  
  const avgResponseTime = reports.length > 0 
    ? Math.round(reports.reduce((sum, report) => sum + report.summary.averageResponseTime, 0) / reports.length)
    : 0;

  return (
    <div className="charts-section">
      <div className="chart-card">
        <h3>测试结果分布</h3>
        <div className="pie-chart">
          <div className="chart-legend">
            <div className="legend-item">
              <div className="color-box passed"></div>
              <span>通过: {passedTests}</span>
            </div>
            <div className="legend-item">
              <div className="color-box failed"></div>
              <span>失败: {failedTests}</span>
            </div>
            <div className="legend-item">
              <div className="color-box skipped"></div>
              <span>跳过: {skippedTests}</span>
            </div>
          </div>
          <div className="pie-chart-container">
            <svg viewBox="0 0 100 100" className="pie-svg">
              {/* Passed segment */}
              <circle 
                cx="50" 
                cy="50" 
                r="40" 
                fill="transparent" 
                stroke="#28a745" 
                strokeWidth="20" 
                strokeDasharray={`${(passedTests/totalTests)*251.2} 251.2`} 
                transform="rotate(-90 50 50)"
              />
              {/* Failed segment */}
              <circle 
                cx="50" 
                cy="50" 
                r="40" 
                fill="transparent" 
                stroke="#dc3545" 
                strokeWidth="20" 
                strokeDasharray={`${(failedTests/totalTests)*251.2} 251.2`} 
                transform="rotate(-90 50 50)"
                strokeDashoffset={-((passedTests/totalTests)*251.2)}
              />
              {/* Skipped segment */}
              <circle 
                cx="50" 
                cy="50" 
                r="40" 
                fill="transparent" 
                stroke="#ffc107" 
                strokeWidth="20" 
                strokeDasharray={`${(skippedTests/totalTests)*251.2} 251.2`} 
                transform="rotate(-90 50 50)"
                strokeDashoffset={-((passedTests/totalTests)*251.2 + (failedTests/totalTests)*251.2)}
              />
            </svg>
            <div className="chart-center">
              <div className="center-value">{passRate}%</div>
              <div className="center-label">通过率</div>
            </div>
          </div>
        </div>
      </div>
      
      <div className="chart-card">
        <h3>执行统计</h3>
        <div className="stats-grid">
          <div className="stat-item">
            <div className="stat-value">{totalTests}</div>
            <div className="stat-label">总测试数</div>
          </div>
          <div className="stat-item">
            <div className="stat-value">{reports.length}</div>
            <div className="stat-label">报告数</div>
          </div>
          <div className="stat-item">
            <div className="stat-value">{avgResponseTime}ms</div>
            <div className="stat-label">平均响应时间</div>
          </div>
          <div className="stat-item">
            <div className="stat-value">{passRate}%</div>
            <div className="stat-label">通过率</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ChartVisualization;