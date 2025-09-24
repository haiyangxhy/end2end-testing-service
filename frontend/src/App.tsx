import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import Header from './components/Header';
import Navigation from './components/Navigation';
import TestSuiteManagement from './components/TestSuiteManagement';
import TestCaseManagement from './components/TestCaseManagement';
import TestExecution from './components/TestExecution';
import ReportDashboard from './components/ReportDashboard';
import MonitoringDashboard from './components/MonitoringDashboard';
import EnvironmentManagement from './components/EnvironmentManagement';
import VariableManagement from './components/VariableManagement';
import Login from './components/Login';
import './App.css';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  // 检查是否存在保存的token
  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    if (savedToken) {
      setIsLoggedIn(true);
    }
  }, []);

  const handleLogin = (token: string) => {
    setIsLoggedIn(true);
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
    localStorage.removeItem('token');
  };

  return (
    <ConfigProvider locale={zhCN}>
      <Router>
        <div className="App">
          {isLoggedIn ? (
            <>
              <Header onLogout={handleLogout} />
              <div className="main-container">
                <Navigation />
                <main className="content">
                  <Routes>
                    <Route path="/" element={<TestSuiteManagement />} />
                    <Route path="/test-suites" element={<TestSuiteManagement />} />
                    <Route path="/test-cases" element={<TestCaseManagement />} />
                    <Route path="/execution" element={<TestExecution />} />
                    <Route path="/reports" element={<ReportDashboard />} />
                    <Route path="/monitoring" element={<MonitoringDashboard />} />
                    <Route path="/environments" element={<EnvironmentManagement />} />
                    <Route path="/variables" element={<VariableManagement />} />
                  </Routes>
                </main>
              </div>
            </>
          ) : (
            <Routes>
              <Route path="/" element={<Login onLogin={handleLogin} />} />
              <Route path="/login" element={<Login onLogin={handleLogin} />} />
            </Routes>
          )}
        </div>
      </Router>
    </ConfigProvider>
  );
}

export default App;