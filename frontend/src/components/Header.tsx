import React from 'react';
import './Header.css';

interface HeaderProps {
  onLogout: () => void;
}

const Header: React.FC<HeaderProps> = ({ onLogout }) => {
  return (
    <header className="App-header">
      <h1>端到端系统测试平台</h1>
      <div className="user-info">
        <span>管理员</span>
        <button className="logout-btn" onClick={onLogout}>退出</button>
      </div>
    </header>
  );
};

export default Header;