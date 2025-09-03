import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Navigation.css';

const Navigation: React.FC = () => {
  const location = useLocation();

  return (
    <nav className="navigation">
      <ul>
        <li>
          <Link 
            to="/test-suites" 
            className={location.pathname === '/test-suites' || location.pathname === '/' ? 'active' : ''}
          >
            测试套件管理
          </Link>
        </li>
        <li>
          <Link 
            to="/test-cases" 
            className={location.pathname === '/test-cases' ? 'active' : ''}
          >
            测试用例管理
          </Link>
        </li>
        <li>
          <Link 
            to="/execution" 
            className={location.pathname === '/execution' ? 'active' : ''}
          >
            测试执行
          </Link>
        </li>
        <li>
          <Link 
            to="/reports" 
            className={location.pathname === '/reports' ? 'active' : ''}
          >
            报告中心
          </Link>
        </li>
        <li>
          <Link 
            to="/scheduler" 
            className={location.pathname === '/scheduler' ? 'active' : ''}
          >
            任务调度
          </Link>
        </li>
      </ul>
    </nav>
  );
};

export default Navigation;