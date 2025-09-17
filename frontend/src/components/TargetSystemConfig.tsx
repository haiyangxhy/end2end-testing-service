import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './TargetSystemConfig.css';

// 目标系统配置接口
interface TargetSystemConfig {
  id: string;
  name: string;
  apiUrl: string;
  uiUrl: string;
  description: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

const TargetSystemConfig: React.FC = () => {
  const [configs, setConfigs] = useState<TargetSystemConfig[]>([]);
  const [currentConfig, setCurrentConfig] = useState<TargetSystemConfig>({
    id: '',
    name: '',
    apiUrl: '',
    uiUrl: '',
    description: '',
    isActive: false,
    createdAt: '',
    updatedAt: ''
  });
  const [isEditing, setIsEditing] = useState(false);

  // 从后端获取配置列表
  useEffect(() => {
    fetchConfigs();
  }, []);

  const fetchConfigs = async () => {
    try {
      // 从localStorage获取token
      const token = localStorage.getItem('token');
      
      const response = await axios.get('http://localhost:8180/api/target-system-configs', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setConfigs(response.data);
    } catch (error) {
      console.error('获取配置列表失败:', error);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const target = e.target as HTMLInputElement;
    const { name, type } = target;
    const value = type === 'checkbox' ? target.checked : target.value;
    setCurrentConfig(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      // 从localStorage获取token
      const token = localStorage.getItem('token');
      
      const configWithAuth = {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      };
      
      if (isEditing) {
        const response = await axios.put(
          `http://localhost:8180/api/target-system-configs/${currentConfig.id}`, 
          currentConfig,
          configWithAuth
        );
        setConfigs(configs.map(config => config.id === currentConfig.id ? response.data : config));
      } else {
        const response = await axios.post(
          'http://localhost:8180/api/target-system-configs', 
          {
            ...currentConfig,
            id: currentConfig.id || undefined // 如果ID为空，让后端生成
          },
          configWithAuth
        );
        setConfigs([...configs, response.data]);
      }
      
      // 重置表单
      setCurrentConfig({
        id: '',
        name: '',
        apiUrl: '',
        uiUrl: '',
        description: '',
        isActive: false,
        createdAt: '',
        updatedAt: ''
      });
      setIsEditing(false);
    } catch (error) {
      console.error('保存配置失败:', error);
    }
  };

  const handleEdit = (config: TargetSystemConfig) => {
    setCurrentConfig(config);
    setIsEditing(true);
  };

  const handleDelete = async (id: string) => {
    try {
      // 从localStorage获取token
      const token = localStorage.getItem('token');
      
      await axios.delete(`http://localhost:8180/api/target-system-configs/${id}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      setConfigs(configs.filter(config => config.id !== id));
    } catch (error) {
      console.error('删除配置失败:', error);
    }
  };

  const handleCancel = () => {
    setCurrentConfig({
      id: '',
      name: '',
      apiUrl: '',
      uiUrl: '',
      description: '',
      isActive: false,
      createdAt: '',
      updatedAt: ''
    });
    setIsEditing(false);
  };

  return (
    <div className="target-system-config">
      <h2>目标系统配置管理</h2>
      
      <div className="config-form">
        <h3>{isEditing ? '编辑配置' : '新增配置'}</h3>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="name">系统名称:</label>
            <input
              type="text"
              id="name"
              name="name"
              value={currentConfig.name}
              onChange={handleInputChange}
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="apiUrl">API地址:</label>
            <input
              type="text"
              id="apiUrl"
              name="apiUrl"
              value={currentConfig.apiUrl}
              onChange={handleInputChange}
              placeholder="例如: https://api.example.com"
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="uiUrl">UI地址:</label>
            <input
              type="text"
              id="uiUrl"
              name="uiUrl"
              value={currentConfig.uiUrl}
              onChange={handleInputChange}
              placeholder="例如: https://example.com"
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="description">描述:</label>
            <textarea
              id="description"
              name="description"
              value={currentConfig.description}
              onChange={handleInputChange}
              rows={3}
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="isActive">
              <input
                type="checkbox"
                id="isActive"
                name="isActive"
                checked={currentConfig.isActive}
                onChange={handleInputChange}
              />
              启用此配置
            </label>
          </div>
          
          <div className="form-actions">
            <button type="submit" className="save-btn">
              {isEditing ? '更新' : '保存'}
            </button>
            <button type="button" className="cancel-btn" onClick={handleCancel}>
              取消
            </button>
          </div>
        </form>
      </div>
      
      <div className="config-list">
        <h3>配置列表</h3>
        <table className="config-table">
          <thead>
            <tr>
              <th>系统名称</th>
              <th>API地址</th>
              <th>UI地址</th>
              <th>启用状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            {configs.map(config => (
              <tr key={config.id}>
                <td>{config.name}</td>
                <td>{config.apiUrl}</td>
                <td>{config.uiUrl || 'N/A'}</td>
                <td>{config.isActive ? '已启用' : '未启用'}</td>
                <td>
                  <button onClick={() => handleEdit(config)} className="edit-btn">编辑</button>
                  <button onClick={() => handleDelete(config.id)} className="delete-btn">删除</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default TargetSystemConfig;