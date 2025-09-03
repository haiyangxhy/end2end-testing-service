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

  // Mock data for demonstration
  useEffect(() => {
    const mockSuites: TestSuite[] = [
      {
        id: '1',
        name: '用户管理API测试',
        description: '测试用户管理相关API接口',
        type: 'API'
      },
      {
        id: '2',
        name: '订单流程测试',
        description: '测试订单创建、支付、发货等业务流程',
        type: 'BUSINESS'
      },
      {
        id: '3',
        name: '登录页面UI测试',
        description: '测试登录页面的UI元素和交互',
        type: 'UI'
      }
    ];
    setTestSuites(mockSuites);
  }, []);

  const handleExecute = (suiteId: string) => {
    // Simulate test execution
    setIsExecuting(true);
    
    const newExecution: ExecutionStatus = {
      id: Date.now().toString(),
      suiteId,
      status: 'RUNNING',
      startTime: new Date().toISOString(),
      endTime: '',
      result: '执行中...'
    };
    
    setExecutionStatus(newExecution);
    
    // Simulate execution progress
    setTimeout(() => {
      setExecutionStatus({
        ...newExecution,
        status: 'COMPLETED',
        endTime: new Date().toISOString(),
        result: '测试执行完成，通过率: 85%'
      });
      setIsExecuting(false);
    }, 5000);
  };

  const handleStopExecution = () => {
    if (executionStatus) {
      setExecutionStatus({
        ...executionStatus,
        status: 'FAILED',
        endTime: new Date().toISOString(),
        result: '测试执行已停止'
      });
      setIsExecuting(false);
    }
  };

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