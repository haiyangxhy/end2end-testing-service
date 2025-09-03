import React from 'react';
import './TestCaseList.css';

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

interface TestCaseListProps {
  testCases: TestCase[];
  onEdit: (testCase: TestCase) => void;
  onDelete: (id: string) => void;
}

const TestCaseList: React.FC<TestCaseListProps> = ({ testCases, onEdit, onDelete }) => {
  return (
    <div className="test-case-list">
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
          {testCases.map(testCase => (
            <tr key={testCase.id}>
              <td>{testCase.name}</td>
              <td>{testCase.description}</td>
              <td>
                <span className={`type-badge type-${testCase.type.toLowerCase()}`}>
                  {testCase.type}
                </span>
              </td>
              <td>{new Date(testCase.createdAt).toLocaleString()}</td>
              <td>
                <button className="edit-btn" onClick={() => onEdit(testCase)}>编辑</button>
                <button className="delete-btn" onClick={() => onDelete(testCase.id)}>删除</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default TestCaseList;