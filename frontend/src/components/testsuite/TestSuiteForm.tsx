import React, { useState, useEffect } from 'react';
import './TestSuiteForm.css';

interface TestSuite {
  id: string;
  name: string;
  description: string;
  type: 'API' | 'UI' | 'BUSINESS';
  createdAt: string;
  updatedAt: string;
  testCases: string[];
}

interface TestSuiteFormProps {
  suite: TestSuite | null;
  onSave: (suite: TestSuite) => void;
  onCancel: () => void;
}

const TestSuiteForm: React.FC<TestSuiteFormProps> = ({ suite, onSave, onCancel }) => {
  const [formData, setFormData] = useState<TestSuite>({
    id: '',
    name: '',
    description: '',
    type: 'API',
    createdAt: '',
    updatedAt: '',
    testCases: []
  });

  useEffect(() => {
    if (suite) {
      setFormData(suite);
    }
  }, [suite]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(formData);
  };

  return (
    <div className="test-suite-form">
      <h3>{formData.id ? '编辑测试套件' : '创建测试套件'}</h3>
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
        
        <div className="form-actions">
          <button type="submit" className="save-btn">保存</button>
          <button type="button" className="cancel-btn" onClick={onCancel}>取消</button>
        </div>
      </form>
    </div>
  );
};

export default TestSuiteForm;