import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './TestCaseForm.css';

interface TestCase {
  id: string;
  suiteId: string;
  name: string;
  description: string;
  type: 'API' | 'UI' | 'BUSINESS';
  config: string;
  testSteps: string;
  createdAt: string;
  updatedAt: string;
}

interface TestSuite {
  id: string;
  name: string;
  description: string;
  type: 'API' | 'UI' | 'BUSINESS';
  createdAt: string;
  updatedAt: string;
  testCases: string[];
}

interface TestCaseFormProps {
  testCase: TestCase | null;
  onSave: (testCase: TestCase) => void;
  onCancel: () => void;
}

const TestCaseForm: React.FC<TestCaseFormProps> = ({ testCase, onSave, onCancel }) => {
  const [formData, setFormData] = useState<TestCase>({
    id: '',
    suiteId: '',
    name: '',
    description: '',
    type: 'API',
    config: '',
    testSteps: '',
    createdAt: '',
    updatedAt: ''
  });
  
  const [testSuites, setTestSuites] = useState<TestSuite[]>([]);

  useEffect(() => {
    // Fetch test suites from backend
    fetchTestSuites();
  }, []);

  useEffect(() => {
    if (testCase) {
      setFormData(testCase);
    }
  }, [testCase]);

  const fetchTestSuites = async () => {
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get('http://localhost:8180/api/test-suites', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      console.log('Raw response from backend:', response);
      console.log('Test suites data:', response.data);
      
      // 不过滤数据，直接使用后端返回的数据
      setTestSuites(response.data);
    } catch (err) {
      console.error('Failed to fetch test suites:', err);
      alert('获取测试套件列表失败');
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prevFormData => ({
      ...prevFormData,
      [name]: value
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validate suiteId
    if (!formData.suiteId) {
      alert('请选择关联的测试套件');
      return;
    }
    
    // Validate JSON format for config field
    if (formData.config.trim() !== '') {
      try {
        JSON.parse(formData.config);
      } catch (error) {
        alert('配置字段必须是有效的JSON格式');
        return;
      }
    }
    
    onSave(formData);
  };

  return (
    <div className="test-case-form">
      <h3>{formData.id ? '编辑测试用例' : '创建测试用例'}</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="suiteId">关联测试套件:</label>
          <select
            id="suiteId"
            name="suiteId"
            value={formData.suiteId}
            onChange={handleChange}
            required
          >
            <option value="">请选择测试套件</option>
            {testSuites.map(suite => (
              <option key={suite.id} value={suite.id}>
                {suite.name}
              </option>
            ))}
          </select>
        </div>
        
        <div className="form-group">
          <label htmlFor="name">名称:</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleChange}
            required
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="description">描述:</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleChange}
            rows={3}
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="type">类型:</label>
          <select
            id="type"
            name="type"
            value={formData.type}
            onChange={handleChange}
          >
            <option value="API">API测试</option>
            <option value="UI">UI测试</option>
            <option value="BUSINESS">业务流程测试</option>
          </select>
        </div>
        
        <div className="form-group">
          <label htmlFor="testSteps">测试步骤:</label>
          <textarea
            id="testSteps"
            name="testSteps"
            value={formData.testSteps}
            onChange={handleChange}
            rows={6}
            placeholder="请输入测试步骤，例如：&#10;1. 打开登录页面&#10;2. 输入用户名和密码&#10;3. 点击登录按钮&#10;4. 验证是否登录成功"
          />
        </div>
        
        <div className="form-group">
          <label htmlFor="config">配置:</label>
          <textarea
            id="config"
            name="config"
            value={formData.config}
            onChange={handleChange}
            rows={6}
            placeholder="请输入测试配置，JSON格式，例如：&#10;{&#10;  &quot;url&quot;: &quot;/api/login&quot;,&#10;  &quot;method&quot;: &quot;POST&quot;,&#10;  &quot;body&quot;: {&#10;    &quot;username&quot;: &quot;test&quot;,&#10;    &quot;password&quot;: &quot;123456&quot;&#10;  }&#10;}"
          />
        </div>
        
        <div className="form-actions">
          <button type="submit" className="save-btn">保存</button>
          <button type="button" className="cancel-btn" onClick={onCancel}>取消</button>
        </div>
      </form>
    </div>
  );
};

export default TestCaseForm;