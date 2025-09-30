import React, { useState, useEffect } from 'react';
import {
  Card,
  Button,
  Space,
  Select,
  message,
  Typography,
  Row,
  Col,
  Modal,
  Tabs,
  Badge,
  Progress,
  Statistic,
  Alert,
} from 'antd';
import {
  PlayCircleOutlined,
  StopOutlined,
  EyeOutlined,
  ReloadOutlined,
  HistoryOutlined,
} from '@ant-design/icons';
import { testSuiteAPI, environmentAPI, testExecutionAPI } from '../../services/api';
import { TestSuite, TestEnvironment, TestExecution } from '../../types';
import TestExecutionLogs from './TestExecutionLogs';
import './TestExecutionManagement.css';

const { Title, Text } = Typography;
const { Option } = Select;
const { TabPane } = Tabs;

const TestExecutionManagement: React.FC = () => {
  const [testSuites, setTestSuites] = useState<TestSuite[]>([]);
  const [environments, setEnvironments] = useState<TestEnvironment[]>([]);
  const [executions, setExecutions] = useState<TestExecution[]>([]);
  const [selectedSuite, setSelectedSuite] = useState<string>('');
  const [selectedEnvironment, setSelectedEnvironment] = useState<string>('');
  const [currentExecution, setCurrentExecution] = useState<TestExecution | null>(null);
  const [showLogs, setShowLogs] = useState(false);
  const [loading, setLoading] = useState(false);

  // 获取测试套件列表
  const fetchTestSuites = async () => {
    try {
      const response = await testSuiteAPI.getAll();
      setTestSuites(response.data);
    } catch (error) {
      message.error('获取测试套件列表失败');
      console.error('获取测试套件列表失败:', error);
    }
  };

  // 获取激活的测试环境
  const fetchActiveEnvironment = async () => {
    try {
      const response = await environmentAPI.getActive();
      if (response.data) {
        setSelectedEnvironment(response.data.id);
        setEnvironments([response.data]);
      } else {
        // 如果没有激活环境，获取所有环境供用户选择
        const allResponse = await environmentAPI.getAll();
        setEnvironments(allResponse.data);
        message.warning('没有激活的测试环境，请手动选择');
      }
    } catch (error) {
      console.error('获取激活环境失败:', error);
      // 如果获取激活环境失败，尝试获取所有环境
      try {
        const response = await environmentAPI.getAll();
        setEnvironments(response.data);
        message.warning('获取激活环境失败，请手动选择测试环境');
      } catch (fallbackError) {
        message.error('获取测试环境失败');
      }
    }
  };

  // 获取执行记录列表
  const fetchExecutions = async () => {
    try {
      const response = await testExecutionAPI.getAll();
      setExecutions(response.data);
    } catch (error) {
      message.error('获取执行记录失败');
      console.error('获取执行记录失败:', error);
    }
  };

  // 执行测试套件
  const executeTestSuite = async () => {
    if (!selectedSuite) {
      message.warning('请选择测试套件');
      return;
    }
    if (!selectedEnvironment) {
      message.warning('请选择测试环境');
      return;
    }

    try {
      setLoading(true);
      const response = await testExecutionAPI.execute({
        suiteId: selectedSuite,
        environmentId: selectedEnvironment,
      });
      
      setCurrentExecution(response.data);
      setShowLogs(true);
      message.success('测试执行已开始');
      
      // 刷新执行记录列表
      await fetchExecutions();
    } catch (error) {
      message.error('执行测试套件失败');
      console.error('执行测试套件失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 查看执行日志
  const viewExecutionLogs = (execution: TestExecution) => {
    setCurrentExecution(execution);
    setShowLogs(true);
  };

  // 停止当前执行
  const stopCurrentExecution = async () => {
    if (!currentExecution) return;

    try {
      await testExecutionAPI.stop(currentExecution.id);
      message.success('测试执行已停止');
      await fetchExecutions();
    } catch (error) {
      message.error('停止测试执行失败');
      console.error('停止测试执行失败:', error);
    }
  };

  // 执行完成回调
  const handleExecutionComplete = (execution: TestExecution) => {
    setCurrentExecution(execution);
    message.success('测试执行完成');
    fetchExecutions();
  };

  // 获取状态颜色
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'FAILED': return 'error';
      case 'RUNNING': return 'processing';
      case 'CANCELLED': return 'warning';
      default: return 'default';
    }
  };

  // 获取状态文本
  const getStatusText = (status: string) => {
    switch (status) {
      case 'PENDING': return '等待中';
      case 'RUNNING': return '运行中';
      case 'COMPLETED': return '已完成';
      case 'FAILED': return '失败';
      case 'CANCELLED': return '已取消';
      default: return status;
    }
  };

  // 初始化数据
  useEffect(() => {
    fetchTestSuites();
    fetchActiveEnvironment();
    fetchExecutions();
  }, []);

  return (
    <div className="test-execution-management">
      <Card>
        <div className="execution-controls">
          <Title level={3}>
            <PlayCircleOutlined /> 测试执行管理
          </Title>
          
          <Space size="large" wrap>
            <div>
              <Text strong>测试套件:</Text>
              <Select
                placeholder="选择测试套件"
                value={selectedSuite}
                onChange={setSelectedSuite}
                style={{ width: 200, marginLeft: 8 }}
                showSearch
                optionFilterProp="children"
              >
                {testSuites.map(suite => (
                  <Option key={suite.id} value={suite.id}>
                    {suite.name}
                  </Option>
                ))}
              </Select>
            </div>
            
            <div>
              <Text strong>测试环境:</Text>
              {environments.length === 1 && selectedEnvironment ? (
                // 只有一个环境时，显示当前环境信息
                <div style={{ marginLeft: 8, display: 'inline-block' }}>
                  <Space>
                    <Text>{environments[0].name}</Text>
                    <Badge status="processing" text="已激活" />
                  </Space>
                </div>
              ) : (
                // 多个环境时，显示选择下拉框
                <Select
                  placeholder="选择测试环境"
                  value={selectedEnvironment}
                  onChange={setSelectedEnvironment}
                  style={{ width: 200, marginLeft: 8 }}
                  showSearch
                  optionFilterProp="children"
                >
                  {environments.map(env => (
                    <Option key={env.id} value={env.id}>
                      {env.name} {env.isActive ? '(已激活)' : ''}
                    </Option>
                  ))}
                </Select>
              )}
            </div>
            
            <Button
              type="primary"
              icon={<PlayCircleOutlined />}
              onClick={executeTestSuite}
              loading={loading}
              disabled={!selectedSuite || !selectedEnvironment}
            >
              开始执行
            </Button>
            
            <Button
              icon={<ReloadOutlined />}
              onClick={fetchExecutions}
            >
              刷新
            </Button>
          </Space>
        </div>

        <div className="execution-stats">
          <Row gutter={16}>
            <Col span={6}>
              <Statistic
                title="总执行次数"
                value={executions.length}
                prefix={<HistoryOutlined />}
              />
            </Col>
            <Col span={6}>
              <Statistic
                title="成功次数"
                value={executions.filter(e => e.status === 'COMPLETED').length}
                valueStyle={{ color: '#3f8600' }}
              />
            </Col>
            <Col span={6}>
              <Statistic
                title="失败次数"
                value={executions.filter(e => e.status === 'FAILED').length}
                valueStyle={{ color: '#cf1322' }}
              />
            </Col>
            <Col span={6}>
              <Statistic
                title="运行中"
                value={executions.filter(e => e.status === 'RUNNING').length}
                valueStyle={{ color: '#1890ff' }}
              />
            </Col>
          </Row>
        </div>

        <div className="execution-list">
          <Title level={4}>执行记录</Title>
          
          {executions.length === 0 ? (
            <Alert message="暂无执行记录" type="info" />
          ) : (
            <div className="execution-items">
              {executions.map(execution => (
                <Card
                  key={execution.id}
                  size="small"
                  className="execution-item"
                  actions={[
                    <Button
                      type="link"
                      icon={<EyeOutlined />}
                      onClick={() => viewExecutionLogs(execution)}
                    >
                      查看日志
                    </Button>,
                  ]}
                >
                  <div className="execution-item-content">
                    <div className="execution-header">
                      <Space>
                        <Text strong>{execution.testSuiteName}</Text>
                        <Badge
                          status={getStatusColor(execution.status)}
                          text={getStatusText(execution.status)}
                        />
                      </Space>
                      <Text type="secondary">
                        {new Date(execution.startTime).toLocaleString()}
                      </Text>
                    </div>
                    
                    {execution.progress !== undefined && execution.status === 'RUNNING' && (
                      <div className="execution-progress">
                        <Progress
                          percent={execution.progress}
                          size="small"
                          status="active"
                        />
                      </div>
                    )}
                    
                    {execution.result && (
                      <div className="execution-result">
                        <Text type="secondary">{execution.result}</Text>
                      </div>
                    )}
                  </div>
                </Card>
              ))}
            </div>
          )}
        </div>
      </Card>

      {/* 执行日志模态框 */}
      <Modal
        title="测试执行日志"
        open={showLogs}
        onCancel={() => setShowLogs(false)}
        width="90%"
        style={{ top: 20 }}
        footer={[
          <Button key="close" onClick={() => setShowLogs(false)}>
            关闭
          </Button>,
          currentExecution?.status === 'RUNNING' && (
            <Button
              key="stop"
              danger
              icon={<StopOutlined />}
              onClick={stopCurrentExecution}
            >
              停止执行
            </Button>
          ),
        ].filter(Boolean)}
      >
        {currentExecution && (
          <TestExecutionLogs
            executionId={currentExecution.id}
            onExecutionComplete={handleExecutionComplete}
          />
        )}
      </Modal>
    </div>
  );
};

export default TestExecutionManagement;
