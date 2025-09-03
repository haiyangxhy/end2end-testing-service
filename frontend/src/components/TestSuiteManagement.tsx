import React, { useState, useEffect } from 'react';
import TestSuiteList from './TestSuiteList';
import TestSuiteForm from './TestSuiteForm';
import './TestSuiteManagement.css';

interface TestSuite {
  id: string;
  name: string;
  description: string;
  type: 'API' | 'UI' | 'BUSINESS';
  createdAt: string;
  updatedAt: string;
  testCases: string[];
}

const TestSuiteManagement: React.FC = () => {
  const [testSuites, setTestSuites] = useState<TestSuite[]>([]);
  const [selectedSuite, setSelectedSuite] = useState<TestSuite | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);

  // Mock data for demonstration
  useEffect(() => {
    const mockData: TestSuite[] = [
      {
        id: '1',
        name: '用户管理API测试',
        description: '测试用户管理相关API接口',
        type: 'API',
        createdAt: '2023-01-15T10:30:00Z',
        updatedAt: '2023-01-15T10:30:00Z',
        testCases: ['1', '2', '3']
      },
      {
        id: '2',
        name: '订单流程测试',
        description: '测试订单创建、支付、发货等业务流程',
        type: 'BUSINESS',
        createdAt: '2023-01-16T14:20:00Z',
        updatedAt: '2023-01-16T14:20:00Z',
        testCases: ['4', '5', '6', '7']
      },
      {
        id: '3',
        name: '登录页面UI测试',
        description: '测试登录页面的UI元素和交互',
        type: 'UI',
        createdAt: '2023-01-17T09:15:00Z',
        updatedAt: '2023-01-17T09:15:00Z',
        testCases: ['8', '9']
      }
    ];
    setTestSuites(mockData);
  }, []);

  const handleCreateSuite = () => {
    setSelectedSuite(null);
    setIsFormOpen(true);
  };

  const handleEditSuite = (suite: TestSuite) => {
    setSelectedSuite(suite);
    setIsFormOpen(true);
  };

  const handleDeleteSuite = (id: string) => {
    setTestSuites(testSuites.filter(suite => suite.id !== id));
  };

  const handleSaveSuite = (suite: TestSuite) => {
    if (suite.id) {
      // Update existing suite
      setTestSuites(testSuites.map(s => s.id === suite.id ? suite : s));
    } else {
      // Create new suite
      const newSuite = { ...suite, id: Date.now().toString() };
      setTestSuites([...testSuites, newSuite]);
    }
    setIsFormOpen(false);
  };

  return (
    <div className="test-suite-management">
      <div className="header">
        <h2>测试套件管理</h2>
        <button className="create-btn" onClick={handleCreateSuite}>创建测试套件</button>
      </div>
      
      {isFormOpen ? (
        <TestSuiteForm 
          suite={selectedSuite} 
          onSave={handleSaveSuite} 
          onCancel={() => setIsFormOpen(false)} 
        />
      ) : (
        <TestSuiteList 
          testSuites={testSuites} 
          onEdit={handleEditSuite} 
          onDelete={handleDeleteSuite} 
        />
      )}
    </div>
  );
};

export default TestSuiteManagement;