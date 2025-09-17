import React, { useState, useEffect } from 'react';
import TestCaseList from './TestCaseList';
import TestCaseForm from './TestCaseForm';
import axios from 'axios';
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
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch data from backend API
  useEffect(() => {
    fetchTestCases();
  }, []);

  const fetchTestCases = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      const response = await axios.get('http://localhost:8180/api/test-cases', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setTestCases(response.data);
      setError(null);
    } catch (err) {
      console.error('Failed to fetch test cases:', err);
      setError('获取测试用例数据失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateCase = () => {
    setSelectedCase(null);
    setIsFormOpen(true);
  };

  const handleEditCase = (testCase: TestCase) => {
    setSelectedCase(testCase);
    setIsFormOpen(true);
  };

  const handleDeleteCase = async (id: string) => {
    try {
      const token = localStorage.getItem('token');
      await axios.delete(`http://localhost:8180/api/test-cases/${id}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setTestCases(testCases.filter(testCase => testCase.id !== id));
    } catch (err) {
      console.error('Failed to delete test case:', err);
      alert('删除测试用例失败');
    }
  };

  const handleSaveCase = async (testCase: TestCase) => {
    try {
      const token = localStorage.getItem('token');
      if (testCase.id) {
        // Update existing test case
        const response = await axios.put(`http://localhost:8180/api/test-cases/${testCase.id}`, testCase, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        setTestCases(testCases.map(tc => tc.id === testCase.id ? response.data : tc));
      } else {
        // Create new test case
        const response = await axios.post('http://localhost:8180/api/test-cases', testCase, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        setTestCases([...testCases, response.data]);
      }
      setIsFormOpen(false);
    } catch (err) {
      console.error('Failed to save test case:', err);
      alert('保存测试用例失败');
    }
  };

  if (loading) {
    return <div className="test-case-management">加载中...</div>;
  }

  if (error) {
    return <div className="test-case-management">错误: {error}</div>;
  }

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