import React, { useState, useEffect } from 'react';
import {
  Card,
  Button,
  Space,
  message,
  Typography,
  Row,
  Col,
  Divider,
  Tabs,
  Badge,
  Progress,
  Statistic,
  List,
  Tag,
  Empty,
} from 'antd';
import {
  PlayCircleOutlined,
  StopOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  ReloadOutlined,
  FileTextOutlined,
  ApiOutlined,
  DesktopOutlined,
  SettingOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import { testSuiteAPI, testExecutionAPI, environmentAPI } from '../../services/api';
import { TestSuite, TestExecution, TestEnvironment } from '../../types';
import './TestExecution.css';

const { Title, Text } = Typography;
const { TabPane } = Tabs;

const TestExecutionComponent: React.FC = () => {
  const [testSuites, setTestSuites] = useState<TestSuite[]>([]);
  const [executions, setExecutions] = useState<TestExecution[]>([]);
  const [currentExecution, setCurrentExecution] = useState<TestExecution | null>(null);
  const [loading, setLoading] = useState(false);
  const [executing, setExecuting] = useState(false);
  const [selectedSuite, setSelectedSuite] = useState<string>('');
  const [selectedEnvironment, setSelectedEnvironment] = useState<string>('');
  const [testEnvironments, setTestEnvironments] = useState<TestEnvironment[]>([]);

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
        setTestEnvironments([response.data]);
      } else {
        // 如果没有激活环境，获取所有环境供用户选择
        const allResponse = await environmentAPI.getAll();
        setTestEnvironments(allResponse.data);
        message.warning('没有激活的测试环境，请手动选择');
      }
    } catch (error) {
      console.error('获取激活环境失败:', error);
      // 如果获取激活环境失败，尝试获取所有环境
      try {
        const response = await environmentAPI.getAll();
        setTestEnvironments(response.data);
        message.warning('获取激活环境失败，请手动选择测试环境');
      } catch (fallbackError) {
        message.error('获取测试环境失败');
      }
    }
  };

  // 获取执行历史
  const fetchExecutions = async () => {
    try {
      const response = await testExecutionAPI.getAll();
      setExecutions(response.data);
    } catch (error) {
      message.error('获取执行历史失败');
      console.error('获取执行历史失败:', error);
    }
  };

  // 初始化数据
  useEffect(() => {
    fetchTestSuites();
    fetchActiveEnvironment();
    fetchExecutions();
  }, []);

  // 执行测试
  const handleExecute = async () => {
    if (!selectedSuite) {
      message.warning('请选择要执行的测试套件');
      return;
    }
    if (!selectedEnvironment) {
      message.warning('请选择测试环境');
      return;
    }

    try {
      setExecuting(true);
      const response = await testExecutionAPI.execute({ suiteId: selectedSuite, environmentId: selectedEnvironment });
      setCurrentExecution(response.data);
      message.success('测试执行已启动');
      
      // 开始轮询执行状态
      pollExecutionStatus(response.data.id);
    } catch (error) {
      message.error('测试执行启动失败');
      console.error('测试执行启动失败:', error);
      setExecuting(false);
    }
  };

  // 停止执行
  const handleStop = async () => {
    if (!currentExecution) return;

    try {
      await testExecutionAPI.stop(currentExecution.id);
      setCurrentExecution(null);
      setExecuting(false);
      message.success('测试执行已停止');
      fetchExecutions();
    } catch (error) {
      message.error('停止测试执行失败');
      console.error('停止测试执行失败:', error);
    }
  };

  // 轮询执行状态
  const pollExecutionStatus = async (executionId: string) => {
    const pollInterval = setInterval(async () => {
      try {
        const response = await testExecutionAPI.getById(executionId);
        const execution = response.data;
        setCurrentExecution(execution);

        if (execution.status === 'COMPLETED' || execution.status === 'FAILED') {
          clearInterval(pollInterval);
          setExecuting(false);
          setCurrentExecution(null);
          fetchExecutions();
          message.success('测试执行完成');
        }
      } catch (error) {
        console.error('轮询执行状态失败:', error);
        clearInterval(pollInterval);
        setExecuting(false);
      }
    }, 2000);
  };

  // 获取状态图标
  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PENDING':
        return <ClockCircleOutlined style={{ color: '#faad14' }} />;
      case 'RUNNING':
        return <ThunderboltOutlined style={{ color: '#1890ff' }} />;
      case 'COMPLETED':
        return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
      case 'FAILED':
        return <CloseCircleOutlined style={{ color: '#ff4d4f' }} />;
      default:
        return <ClockCircleOutlined />;
    }
  };

  // 获取状态颜色
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PENDING':
        return 'warning';
      case 'RUNNING':
        return 'processing';
      case 'COMPLETED':
        return 'success';
      case 'FAILED':
        return 'error';
      default:
        return 'default';
    }
  };

  // 获取类型图标
  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'API':
        return <ApiOutlined />;
      case 'UI':
        return <DesktopOutlined />;
      case 'BUSINESS':
        return <SettingOutlined />;
      default:
        return <FileTextOutlined />;
    }
  };

  // 计算统计信息
  const getStats = () => {
    const total = executions.length;
    const completed = executions.filter(e => e.status === 'COMPLETED').length;
    const failed = executions.filter(e => e.status === 'FAILED').length;
    const running = executions.filter(e => e.status === 'RUNNING').length;
    const successRate = total > 0 ? ((completed / total) * 100).toFixed(1) : '0';

    return { total, completed, failed, running, successRate };
  };

  const stats = getStats();

  return (
    <div className="test-execution">
      <div className="page-header">
        <Title level={2}>
          <PlayCircleOutlined /> 测试执行
        </Title>
        <Space>
          <Button
            icon={<ReloadOutlined />}
            onClick={fetchExecutions}
          >
            刷新
          </Button>
        </Space>
      </div>

      <Row gutter={[24, 24]}>
        {/* 执行控制面板 */}
        <Col xs={24} lg={12}>
          <Card title="执行控制" className="execution-control-card">
            <div className="execution-control">
              <div className="suite-selection">
                <Text strong>选择测试套件：</Text>
                <div className="suite-list">
                  {testSuites.map(suite => (
                    <div
                      key={suite.id}
                      className={`suite-item ${selectedSuite === suite.id ? 'selected' : ''}`}
                      onClick={() => setSelectedSuite(suite.id)}
                    >
                      <Space>
                        {getTypeIcon(suite.type)}
                        <Text>{suite.name}</Text>
                        <Tag color="blue">{suite.type}</Tag>
                      </Space>
                    </div>
                  ))}
                </div>
              </div>

              <div className="environment-selection">
                <Text strong>测试环境：</Text>
                <div className="environment-list">
                  {testEnvironments.length === 1 && selectedEnvironment ? (
                    // 只有一个环境时，显示当前环境信息
                    <div className="environment-item selected">
                      <Space>
                        <DesktopOutlined />
                        <Text>{testEnvironments[0].name}</Text>
                        <Tag color="green">{testEnvironments[0].type}</Tag>
                        <Tag color="blue">已激活</Tag>
                      </Space>
                    </div>
                  ) : (
                    // 多个环境时，显示选择列表
                    testEnvironments.map(env => (
                      <div
                        key={env.id}
                        className={`environment-item ${selectedEnvironment === env.id ? 'selected' : ''}`}
                        onClick={() => setSelectedEnvironment(env.id)}
                      >
                        <Space>
                          <DesktopOutlined />
                          <Text>{env.name}</Text>
                          <Tag color="green">{env.type}</Tag>
                          {env.isActive && <Tag color="blue">已激活</Tag>}
                        </Space>
                      </div>
                    ))
                  )}
                </div>
              </div>

              <Divider />

              <div className="execution-actions">
                <Space>
                  <Button
                    type="primary"
                    icon={<PlayCircleOutlined />}
                    onClick={handleExecute}
                    disabled={!selectedSuite || !selectedEnvironment || executing}
                    loading={executing}
                  >
                    {executing ? '执行中...' : '开始执行'}
                  </Button>
                  <Button
                    icon={<StopOutlined />}
                    onClick={handleStop}
                    disabled={!currentExecution}
                    danger
                  >
                    停止执行
                  </Button>
                </Space>
              </div>

              {currentExecution && (
                <div className="current-execution">
                  <Divider />
                  <Text strong>当前执行：</Text>
                  <div className="execution-info">
                    <Space>
                      {getStatusIcon(currentExecution.status)}
                      <Text>{currentExecution.testSuiteName}</Text>
                      <Badge 
                        status={getStatusColor(currentExecution.status) as any}
                        text={currentExecution.status}
                      />
                    </Space>
                    {currentExecution.progress !== undefined && (
                      <Progress
                        percent={currentExecution.progress}
                        status={currentExecution.status === 'FAILED' ? 'exception' : 'active'}
                        style={{ marginTop: 8 }}
                      />
                    )}
                  </div>
                </div>
              )}
            </div>
          </Card>
        </Col>

        {/* 执行统计 */}
        <Col xs={24} lg={12}>
          <Card title="执行统计" className="execution-stats-card">
            <Row gutter={[16, 16]}>
              <Col xs={12} sm={6}>
                <Statistic
                  title="总执行次数"
                  value={stats.total}
                  prefix={<FileTextOutlined />}
                />
              </Col>
              <Col xs={12} sm={6}>
                <Statistic
                  title="成功次数"
                  value={stats.completed}
                  valueStyle={{ color: '#52c41a' }}
                  prefix={<CheckCircleOutlined />}
                />
              </Col>
              <Col xs={12} sm={6}>
                <Statistic
                  title="失败次数"
                  value={stats.failed}
                  valueStyle={{ color: '#ff4d4f' }}
                  prefix={<CloseCircleOutlined />}
                />
              </Col>
              <Col xs={12} sm={6}>
                <Statistic
                  title="成功率"
                  value={stats.successRate}
                  suffix="%"
                  valueStyle={{ color: '#1890ff' }}
                />
              </Col>
            </Row>
          </Card>
        </Col>
      </Row>

      <Row gutter={[24, 24]} style={{ marginTop: 24 }}>
        {/* 执行历史 */}
        <Col xs={24}>
          <Card title="执行历史" className="execution-history-card">
            <Tabs defaultActiveKey="list">
              <TabPane tab="执行列表" key="list">
                <List
                  dataSource={executions}
                  renderItem={(execution) => (
                    <List.Item
                      actions={[
                        <Button
                          type="text"
                          icon={<FileTextOutlined />}
                          onClick={() => {
                            // 查看详情
                            console.log('查看执行详情:', execution.id);
                          }}
                        >
                          查看详情
                        </Button>
                      ]}
                    >
                      <List.Item.Meta
                        avatar={getStatusIcon(execution.status)}
                        title={
                          <Space>
                            <Text strong>{execution.testSuiteName}</Text>
                            <Badge 
                              status={getStatusColor(execution.status) as any}
                              text={execution.status}
                            />
                          </Space>
                        }
                        description={
                          <Space direction="vertical" size="small">
                            <Text type="secondary">
                              开始时间: {execution.startTime ? new Date(execution.startTime).toLocaleString() : '未知'}
                            </Text>
                            {execution.endTime && (
                              <Text type="secondary">
                                结束时间: {new Date(execution.endTime).toLocaleString()}
                              </Text>
                            )}
                            {execution.progress !== undefined && (
                              <Progress
                                percent={execution.progress}
                                size="small"
                                status={execution.status === 'FAILED' ? 'exception' : 'success'}
                              />
                            )}
                          </Space>
                        }
                      />
                    </List.Item>
                  )}
                  locale={{ emptyText: <Empty description="暂无执行记录" /> }}
                />
              </TabPane>
            </Tabs>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default TestExecutionComponent;