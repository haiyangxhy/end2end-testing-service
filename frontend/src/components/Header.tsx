import React from 'react';
import './Header.css';

const Header: React.FC = () => {
  return (
    <header className="App-header">
      <h1>系统测试服务平台</h1>
      <div className="user-info">
        <span>管理员</span>
        <button className="logout-btn">退出</button>
      </div>
    </header>
  );
};

export default Header;