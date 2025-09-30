import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Menu } from 'antd';
import {
  ExperimentOutlined,
  FileTextOutlined,
  PlayCircleOutlined,
  BarChartOutlined,
  MonitorOutlined,
  EnvironmentOutlined,
  KeyOutlined,
  SettingOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import './Navigation.css';

const Navigation: React.FC = () => {
  const location = useLocation();

  const menuItems = [
    {
      key: '/test-suites',
      icon: <ExperimentOutlined />,
      label: <Link to="/test-suites">测试套件管理</Link>,
    },
    {
      key: '/test-cases',
      icon: <FileTextOutlined />,
      label: <Link to="/test-cases">测试用例管理</Link>,
    },
    {
      key: '/execution',
      icon: <PlayCircleOutlined />,
      label: <Link to="/execution">测试执行</Link>,
    },
    {
      key: '/execution-management',
      icon: <PlayCircleOutlined />,
      label: <Link to="/execution-management">执行管理</Link>,
    },
    {
      key: '/reports',
      icon: <BarChartOutlined />,
      label: <Link to="/reports">报告中心</Link>,
    },
    {
      key: '/monitoring',
      icon: <MonitorOutlined />,
      label: <Link to="/monitoring">监控仪表板</Link>,
    },
    {
      key: '/environments',
      icon: <EnvironmentOutlined />,
      label: <Link to="/environments">环境管理</Link>,
    },
    {
      key: '/variables',
      icon: <KeyOutlined />,
      label: <Link to="/variables">变量管理</Link>,
    },
    {
      key: '/scheduled-tasks',
      icon: <ClockCircleOutlined />,
      label: <Link to="/scheduled-tasks">定时任务</Link>,
    },
  ];

  return (
    <nav className="navigation">
      <Menu
        mode="inline"
        selectedKeys={[location.pathname]}
        items={menuItems}
        className="navigation-menu"
      />
    </nav>
  );
};

export default Navigation;