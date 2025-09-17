import React, { useState } from 'react';
import axios from 'axios';
import './Login.css';

interface LoginProps {
  onLogin: (token: string) => void;
}

const Login: React.FC<LoginProps> = ({ onLogin }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8180/api/auth/login', {
        username,
        password
      });
      
      // 保存token到localStorage
      localStorage.setItem('token', response.data.token);
      
      // 通知父组件登录成功
      onLogin(response.data.token);
    } catch (err) {
      setError('登录失败，请检查用户名和密码');
      console.error('登录失败:', err);
    }
  };

  return (
    <div className="login-container">
      <div className="login-form">
        <h2>端到端系统测试平台登录</h2>
        {error && <div className="error-message">{error}</div>}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="username">用户名:</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="password">密码:</label>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="login-btn">登录</button>
        </form>
        <div className="login-info">
          <p>默认用户名: admin</p>
          <p>默认密码: password</p>
        </div>
      </div>
    </div>
  );
};

export default Login;