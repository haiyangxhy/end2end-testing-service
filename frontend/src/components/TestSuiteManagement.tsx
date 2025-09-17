import React, { useState, useEffect } from 'react';
import TestSuiteList from './TestSuiteList';
import TestSuiteForm from './TestSuiteForm';
import axios from 'axios';
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
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch data from backend API
  useEffect(() => {
    fetchTestSuites();
  }, []);

  const fetchTestSuites = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('token');
      const response = await axios.get('http://localhost:8180/api/test-suites', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setTestSuites(response.data);
      setError(null);
    } catch (err) {
      console.error('Failed to fetch test suites:', err);
      setError('获取测试套件数据失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateSuite = () => {
    setSelectedSuite(null);
    setIsFormOpen(true);
  };

  const handleEditSuite = (suite: TestSuite) => {
    setSelectedSuite(suite);
    setIsFormOpen(true);
  };

  const handleDeleteSuite = async (id: string) => {
    try {
      const token = localStorage.getItem('token');
      await axios.delete(`http://localhost:8180/api/test-suites/${id}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setTestSuites(testSuites.filter(suite => suite.id !== id));
    } catch (err) {
      console.error('Failed to delete test suite:', err);
      alert('删除测试套件失败');
    }
  };

  const handleSaveSuite = async (suite: TestSuite) => {
    try {
      const token = localStorage.getItem('token');
      if (suite.id) {
        // Update existing suite
        const response = await axios.put(`http://localhost:8180/api/test-suites/${suite.id}`, suite, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        setTestSuites(testSuites.map(s => s.id === suite.id ? response.data : s));
      } else {
        // Create new suite
        const response = await axios.post('http://localhost:8180/api/test-suites', suite, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        setTestSuites([...testSuites, response.data]);
      }
      setIsFormOpen(false);
    } catch (err) {
      console.error('Failed to save test suite:', err);
      alert('保存测试套件失败');
    }
  };

  if (loading) {
    return <div className="test-suite-management">加载中...</div>;
  }

  if (error) {
    return <div className="test-suite-management">错误: {error}</div>;
  }

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