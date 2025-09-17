import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import Navigation from './components/Navigation';
import TestSuiteManagement from './components/TestSuiteManagement';
import TestCaseManagement from './components/TestCaseManagement';
import TestExecution from './components/TestExecution';
import ReportDashboard from './components/ReportDashboard';
import TargetSystemConfig from './components/TargetSystemConfig';
import Login from './components/Login';
import './App.css';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [token, setToken] = useState('');

  // 检查是否存在保存的token
  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    if (savedToken) {
      setIsLoggedIn(true);
      setToken(savedToken);
    }
  }, []);

  const handleLogin = (token: string) => {
    setIsLoggedIn(true);
    setToken(token);
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
    setToken('');
    localStorage.removeItem('token');
  };

  return (
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
                  <Route path="/target-system-config" element={<TargetSystemConfig />} />
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
  );
}

export default App;