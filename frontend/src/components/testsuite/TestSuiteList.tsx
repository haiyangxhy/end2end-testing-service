import React from 'react';
import './TestSuiteList.css';

interface TestSuite {
  id: string;
  name: string;
  description: string;
  type: 'API' | 'UI' | 'BUSINESS';
  createdAt: string;
  updatedAt: string;
  testCases: string[];
}

interface TestSuiteListProps {
  testSuites: TestSuite[];
  onEdit: (suite: TestSuite) => void;
  onDelete: (id: string) => void;
}

const TestSuiteList: React.FC<TestSuiteListProps> = ({ testSuites, onEdit, onDelete }) => {
  return (
    <div className="test-suite-list">
      <table>
        <thead>
          <tr>
            <th>名称</th>
            <th>描述</th>
            <th>类型</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          {testSuites.map(suite => (
            <tr key={suite.id}>
              <td>{suite.name}</td>
              <td>{suite.description}</td>
              <td>
                <span className={`type-badge type-${suite.type.toLowerCase()}`}>
                  {suite.type}
                </span>
              </td>
              <td>{new Date(suite.createdAt).toLocaleString()}</td>
              <td>
                <button className="edit-btn" onClick={() => onEdit(suite)}>编辑</button>
                <button className="delete-btn" onClick={() => onDelete(suite.id)}>删除</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default TestSuiteList;