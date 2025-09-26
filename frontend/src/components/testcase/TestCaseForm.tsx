import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './TestCaseForm.css';
import { TestCase, TestSuite } from '../types';

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
  
  // 移除testSuites状态，测试用例不再直接关联测试套件

  // 移除fetchTestSuites调用，测试用例不再直接关联测试套件

  useEffect(() => {
    if (testCase) {
      setFormData(testCase);
    }
  }, [testCase]);

  // 移除fetchTestSuites函数，测试用例不再直接关联测试套件

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prevFormData => ({
      ...prevFormData,
      [name]: value
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
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