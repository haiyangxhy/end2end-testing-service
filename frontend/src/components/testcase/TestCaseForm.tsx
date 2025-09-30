import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './TestCaseForm.css';
import { TestCase, TestSuite, TestCaseConfig } from '../../types';
import TestCaseConfigForm from './TestCaseConfigForm';

interface TestCaseFormProps {
  testCase: TestCase | null;
  onSave: (testCase: TestCase) => void;
  onCancel: () => void;
}

const TestCaseForm: React.FC<TestCaseFormProps> = ({ testCase, onSave, onCancel }) => {
  const [formData, setFormData] = useState<TestCase>({
    id: '',
    name: '',
    description: '',
    // 移除type字段，测试用例类型由所属的测试套件决定
    config: '',
    priority: 'MEDIUM' as const,
    status: 'ACTIVE' as const,
    isActive: true,
    testSteps: '',
    expectedResult: '',
    tags: '',
    createdAt: '',
    updatedAt: ''
  });
  
  const [configData, setConfigData] = useState<TestCaseConfig>({
    method: 'GET',
    endpoint: '',
    headers: {},
    params: {},
    body: null,
    assertions: [],
    extract: {},
    timeout: 30000,
    retries: 0,
  });
  
  // 移除testSuites状态，测试用例不再直接关联测试套件

  // 移除fetchTestSuites调用，测试用例不再直接关联测试套件

  useEffect(() => {
    if (testCase) {
      setFormData(testCase);
      // 解析配置JSON
      if (testCase.config) {
        try {
          const parsedConfig = JSON.parse(testCase.config);
          setConfigData(parsedConfig);
        } catch (error) {
          console.warn('解析测试用例配置失败:', error);
        }
      }
    }
  }, [testCase]);

  // 移除fetchTestSuites函数，测试用例不再直接关联测试套件

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prevFormData: any) => ({
      ...prevFormData,
      [name]: value
    }));
  };

  const handleConfigChange = (config: TestCaseConfig) => {
    setConfigData(config);
    // 将配置转换为JSON字符串
    const configJson = JSON.stringify(config, null, 2);
    setFormData(prev => ({
      ...prev,
      config: configJson
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // 确保配置数据已同步
    const configJson = JSON.stringify(configData, null, 2);
    const finalFormData = {
      ...formData,
      config: configJson
    };
    
    onSave(finalFormData);
  };

  return (
    <div className="test-case-form">
      <h3>{formData.id ? '编辑测试用例' : '创建测试用例'}</h3>
      <form onSubmit={handleSubmit}>
        {/* 移除测试套件选择，测试用例和测试套件通过TestSuiteCase关联表关联 */}
        
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
        
        {/* 移除类型选择，测试用例类型由所属的测试套件决定 */}
        
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
          <TestCaseConfigForm
            value={configData}
            onChange={handleConfigChange}
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