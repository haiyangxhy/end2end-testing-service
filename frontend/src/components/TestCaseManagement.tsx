import React, { useState, useEffect } from 'react';
import TestCaseList from './TestCaseList';
import TestCaseForm from './TestCaseForm';
import './TestCaseManagement.css';

interface TestCase {
  id: string;
  suiteId: string;
  name: string;
  description: string;
  type: 'API' | 'UI' | 'BUSINESS';
  config: string;
  createdAt: string;
  updatedAt: string;
}

const TestCaseManagement: React.FC = () => {
  const [testCases, setTestCases] = useState<TestCase[]>([]);
  const [selectedCase, setSelectedCase] = useState<TestCase | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);

  // Mock data for demonstration
  useEffect(() => {
    const mockData: TestCase[] = [
      {
        id: '1',
        suiteId: '1',
        name: '用户登录接口测试',
        description: '测试用户登录接口的正常流程',
        type: 'API',
        config: '{"url": "/api/login", "method": "POST", "body": {"username": "test", "password": "123456"}}',
        createdAt: '2023-01-15T10:30:00Z',
        updatedAt: '2023-01-15T10:30:00Z'
      },
      {
        id: '2',
        suiteId: '1',
        name: '用户注册接口测试',
        description: '测试用户注册接口的正常流程',
        type: 'API',
        config: '{"url": "/api/register", "method": "POST", "body": {"username": "newuser", "password": "123456"}}',
        createdAt: '2023-01-15T11:00:00Z',
        updatedAt: '2023-01-15T11:00:00Z'
      },
      {
        id: '3',
        suiteId: '1',
        name: '用户信息获取接口测试',
        description: '测试获取用户信息接口',
        type: 'API',
        config: '{"url": "/api/user/1", "method": "GET"}',
        createdAt: '2023-01-15T11:30:00Z',
        updatedAt: '2023-01-15T11:30:00Z'
      }
    ];
    setTestCases(mockData);
  }, []);

  const handleCreateCase = () => {
    setSelectedCase(null);
    setIsFormOpen(true);
  };

  const handleEditCase = (testCase: TestCase) => {
    setSelectedCase(testCase);
    setIsFormOpen(true);
  };

  const handleDeleteCase = (id: string) => {
    setTestCases(testCases.filter(testCase => testCase.id !== id));
  };

  const handleSaveCase = (testCase: TestCase) => {
    if (testCase.id) {
      // Update existing test case
      setTestCases(testCases.map(tc => tc.id === testCase.id ? testCase : tc));
    } else {
      // Create new test case
      const newCase = { ...testCase, id: Date.now().toString() };
      setTestCases([...testCases, newCase]);
    }
    setIsFormOpen(false);
  };

  return (
    <div className="test-case-management">
      <div className="header">
        <h2>测试用例管理</h2>
        <button className="create-btn" onClick={handleCreateCase}>创建测试用例</button>
      </div>
      
      {isFormOpen ? (
        <TestCaseForm 
          testCase={selectedCase} 
          onSave={handleSaveCase} 
          onCancel={() => setIsFormOpen(false)} 
        />
      ) : (
        <TestCaseList 
          testCases={testCases} 
          onEdit={handleEditCase} 
          onDelete={handleDeleteCase} 
        />
      )}
    </div>
  );
};

export default TestCaseManagement;