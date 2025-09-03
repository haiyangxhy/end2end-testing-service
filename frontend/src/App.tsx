import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Header from './components/Header';
import Navigation from './components/Navigation';
import TestSuiteManagement from './components/TestSuiteManagement';
import TestCaseManagement from './components/TestCaseManagement';
import TestExecution from './components/TestExecution';
import ReportDashboard from './components/ReportDashboard';
import Scheduler from './components/Scheduler';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <Header />
        <div className="main-container">
          <Navigation />
          <main className="content">
            <Routes>
              <Route path="/" element={<TestSuiteManagement />} />
              <Route path="/test-suites" element={<TestSuiteManagement />} />
              <Route path="/test-cases" element={<TestCaseManagement />} />
              <Route path="/execution" element={<TestExecution />} />
              <Route path="/reports" element={<ReportDashboard />} />
              <Route path="/scheduler" element={<Scheduler />} />
            </Routes>
          </main>
        </div>
      </div>
    </Router>
  );
}

export default App;