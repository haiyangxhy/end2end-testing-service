import React, { useState } from 'react';
import './ExecutionControl.css';

interface TestSuite {
  id: string;
  name: string;
  description: string;
  type: 'API' | 'UI' | 'BUSINESS';
}

interface ExecutionControlProps {
  testSuites: TestSuite[];
  onExecute: (suiteId: string) => void;
  onStop: () => void;
  isExecuting: boolean;
}

const ExecutionControl: React.FC<ExecutionControlProps> = ({ testSuites, onExecute, onStop, isExecuting }) => {
  const [selectedSuiteId, setSelectedSuiteId] = useState<string>('');

  const handleExecute = () => {
    if (selectedSuiteId) {
      onExecute(selectedSuiteId);
    }
  };

  return (
    <div className="execution-control">
      <h3>执行控制</h3>
      
      <div className="control-panel">
        <div className="form-group">
          <label htmlFor="testSuite">选择测试套件:</label>
          <select
            id="testSuite"
            value={selectedSuiteId}
            onChange={(e) => setSelectedSuiteId(e.target.value)}
            disabled={isExecuting}
          >
            <option value="">请选择测试套件</option>
            {testSuites.map(suite => (
              <option key={suite.id} value={suite.id}>
                {suite.name} ({suite.type})
              </option>
            ))}
          </select>
        </div>
        
        <div className="actions">
          <button 
            className="execute-btn" 
            onClick={handleExecute} 
            disabled={!selectedSuiteId || isExecuting}
          >
            {isExecuting ? '执行中...' : '开始执行'}
          </button>
          
          <button 
            className="stop-btn" 
            onClick={onStop} 
            disabled={!isExecuting}
          >
            停止执行
          </button>
        </div>
      </div>
      
      <div className="suite-list">
        <h4>所有测试套件</h4>
        <ul>
          {testSuites.map(suite => (
            <li key={suite.id} className="suite-item">
              <div className="suite-info">
                <h5>{suite.name}</h5>
                <p>{suite.description}</p>
              </div>
              <div className="suite-type">
                <span className={`type-badge type-${suite.type.toLowerCase()}`}>
                  {suite.type}
                </span>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};

export default ExecutionControl;