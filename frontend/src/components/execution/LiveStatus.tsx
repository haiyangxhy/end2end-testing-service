import React, { useState } from 'react';
import TestReport from '../report/TestReport';
import './LiveStatus.css';

interface ExecutionStatus {
  id: string;
  suiteId: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  startTime: string;
  endTime: string;
  result: string;
}

interface LiveStatusProps {
  executionStatus: ExecutionStatus | null;
}

const LiveStatus: React.FC<LiveStatusProps> = ({ executionStatus }) => {
  const [showReport, setShowReport] = useState(false);

  if (!executionStatus) {
    return (
      <div className="live-status">
        <h3>执行状态</h3>
        <div className="status-placeholder">
          <p>暂无执行任务</p>
        </div>
      </div>
    );
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING': return 'gray';
      case 'RUNNING': return 'blue';
      case 'COMPLETED': return 'green';
      case 'FAILED': return 'red';
      default: return 'gray';
    }
  };

  const formatTime = (timeString: string) => {
    if (!timeString) return '-';
    return new Date(timeString).toLocaleString();
  };

  const toggleReport = () => {
    setShowReport(!showReport);
  };

  return (
    <div className="live-status">
      <h3>执行状态</h3>
      
      <div className="status-card">
        <div className="status-header">
          <h4>执行详情</h4>
          <span 
            className="status-badge" 
            style={{ backgroundColor: getStatusColor(executionStatus.status) }}
          >
            {executionStatus.status}
          </span>
        </div>
        
        <div className="status-details">
          <div className="detail-item">
            <span className="label">执行ID:</span>
            <span className="value">{executionStatus.id}</span>
          </div>
          
          <div className="detail-item">
            <span className="label">开始时间:</span>
            <span className="value">{formatTime(executionStatus.startTime)}</span>
          </div>
          
          <div className="detail-item">
            <span className="label">结束时间:</span>
            <span className="value">{formatTime(executionStatus.endTime)}</span>
          </div>
          
          <div className="detail-item">
            <span className="label">执行结果:</span>
            <span className="value">{executionStatus.result}</span>
          </div>
        </div>
        
        <div className="progress-section">
          <h4>执行进度</h4>
          <div className="progress-bar">
            <div 
              className="progress-fill" 
              style={{ 
                width: executionStatus.status === 'COMPLETED' ? '100%' : 
                       executionStatus.status === 'RUNNING' ? '70%' : '0%'
              }}
            ></div>
          </div>
          <div className="progress-text">
            {executionStatus.status === 'RUNNING' ? '正在执行...' : 
             executionStatus.status === 'COMPLETED' ? '执行完成' : 
             executionStatus.status === 'FAILED' ? '执行失败' : '等待执行'}
          </div>
        </div>
        
        {(executionStatus.status === 'COMPLETED' || executionStatus.status === 'FAILED') && (
          <div className="report-section">
            <button className="report-btn" onClick={toggleReport}>
              {showReport ? '隐藏报告' : '查看详细报告'}
            </button>
            
            {showReport && (
              <div className="report-container">
                <TestReport executionId={executionStatus.id} />
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default LiveStatus;