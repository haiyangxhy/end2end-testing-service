import React, { useState, useEffect } from 'react';
import './TestCaseForm.css';

interface TestCase {
  id?: string;
  suiteId: string;
  name: string;
  description: string;
  type: 'API' | 'UI' | 'BUSINESS';
  config: string;
  createdAt?: string;
  updatedAt?: string;
}

interface TestCaseFormProps {
  testCase: TestCase | null;
  onSave: (testCase: TestCase) => void;
  onCancel: () => void;
}

const TestCaseForm: React.FC<TestCaseFormProps> = ({ testCase, onSave, onCancel }) => {
  const [formData, setFormData] = useState<TestCase>({
    suiteId: '1',
    name: '',
    description: '',
    type: 'API',
    config: ''
  });

  useEffect(() => {
    if (testCase) {
      setFormData(testCase);
    }
  }, [testCase]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(formData);
  };

  return (
    <div className="test-case-form">
      <h3>{formData.id ? '编辑测试用例' : '创建测试用例</h3>
      <form onSubmit={handleSubmit}>
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
          <label htmlFor="config">配置:</label>
          <textarea
            id="config"
            name="config"
            value={formData.config}
            onChange={handleChange}
            rows={6}
            placeholder="请输入测试配置，JSON格式"
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