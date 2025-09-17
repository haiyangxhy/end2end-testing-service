import React, { useState, useEffect } from 'react';
import ExecutionControl from './ExecutionControl';
import LiveStatus from './LiveStatus';
import './TestExecution.css';

interface TestSuite {
  id: string;
  name: string;
  description: string;
  type: 'API' | 'UI' | 'BUSINESS';
}

interface ExecutionStatus {
  id: string;
  suiteId: string;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  startTime: string;
  endTime: string;
  result: string;
}

const TestExecution: React.FC = () => {
  const [testSuites, setTestSuites] = useState<TestSuite[]>([]);
  const [executionStatus, setExecutionStatus] = useState<ExecutionStatus | null>(null);
  const [isExecuting, setIsExecuting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 从后端获取真实的测试套件数据
  useEffect(() => {
    const fetchTestSuites = async () => {
      try {
        setLoading(true);
        // 从localStorage获取token
        const token = localStorage.getItem('token');
        
        const response = await fetch('http://localhost:8180/api/test-suites', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        setTestSuites(data);
        setError(null);
      } catch (err) {
        console.error('获取测试套件失败:', err);
        setError('获取测试套件数据失败');
      } finally {
        setLoading(false);
      }
    };

    fetchTestSuites();
  }, []);

  const handleExecute = async (suiteId: string) => {
    try {
      setIsExecuting(true);
      setError(null);
      
      // 从localStorage获取token
      const token = localStorage.getItem('token');
      
      // 调用后端API执行测试
      const response = await fetch(`http://localhost:8180/api/test-executions/execute/${suiteId}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
      });
      
      if (!response.ok) {
        throw new Error(`执行测试失败: ${response.status}`);
      }
      
      const executionData = await response.json();
      setExecutionStatus(executionData);
    } catch (err) {
      console.error('执行测试失败:', err);
      setError('执行测试失败');
      setIsExecuting(false);
    }
  };

  const handleStopExecution = async () => {
    if (executionStatus) {
      try {
        // 从localStorage获取token
        const token = localStorage.getItem('token');
        
        // 调用后端API停止测试执行
        const response = await fetch(`http://localhost:8180/api/test-executions/stop/${executionStatus.id}`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        if (!response.ok) {
          throw new Error(`停止测试失败: ${response.status}`);
        }
        
        const updatedStatus = await response.json();
        setExecutionStatus(updatedStatus);
        setIsExecuting(false);
      } catch (err) {
        console.error('停止测试失败:', err);
        setError('停止测试失败');
      }
    }
  };

  if (loading) {
    return <div className="test-execution">加载中...</div>;
  }

  if (error) {
    return <div className="test-execution">错误: {error}</div>;
  }

  return (
    <div className="test-execution">
      <div className="header">
        <h2>测试执行</h2>
      </div>
      
      <div className="execution-content">
        <ExecutionControl 
          testSuites={testSuites}
          onExecute={handleExecute}
          onStop={handleStopExecution}
          isExecuting={isExecuting}
        />
        
        <LiveStatus executionStatus={executionStatus} />
      </div>
    </div>
  );
};

export default TestExecution;