import React, { useState, useEffect, useRef } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Progress,
  Timeline,
  Tag,
  Button,
  Space,
  Typography,
  Alert,
  Spin,
  Badge,
  Tooltip,
  Modal,
} from 'antd';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  StopOutlined,
  ReloadOutlined,
  EyeOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import { testExecutionAPI } from '../../services/api';
import { TestExecution, TestExecutionLog } from '../../types';
import TestExecutionLogs from './TestExecutionLogs';
import './TestExecutionMonitor.css';

const { Title, Text } = Typography;

interface TestExecutionMonitorProps {
  executionId: string;
  onExecutionComplete?: (execution: TestExecution) => void;
}

const TestExecutionMonitor: React.FC<TestExecutionMonitorProps> = ({
  executionId,
  onExecutionComplete,
}) => {
  const [execution, setExecution] = useState<TestExecution | null>(null);
  const [logs, setLogs] = useState<TestExecutionLog[]>([]);
  const [loading, setLoading] = useState(false);
  const [showLogs, setShowLogs] = useState(false);
  const [eventSource, setEventSource] = useState<EventSource | null>(null);
  const [isStreaming, setIsStreaming] = useState(false);
  const intervalRef = useRef<NodeJS.Timeout | null>(null);

  // 获取执行详情
  const fetchExecution = async () => {
    try {
      const response = await testExecutionAPI.getById(executionId);
      setExecution(response.data);
      
      // 如果执行完成，停止轮询
      if (response.data.status === 'COMPLETED' || 
          response.data.status === 'FAILED' || 
          response.data.status === 'CANCELLED') {
        if (intervalRef.current) {
          clearInterval(intervalRef.current);
          intervalRef.current = null;
        }
        onExecutionComplete?.(response.data);
      }
    } catch (error) {
      console.error('获取执行详情失败:', error);
    }
  };

  // 获取执行日志
  const fetchLogs = async () => {
    try {
      const response = await testExecutionAPI.getLogs(executionId);
      setLogs(response.data);
    } catch (error) {
      console.error('获取执行日志失败:', error);
    }
  };

  // 开始实时监控
  const startMonitoring = () => {
    if (intervalRef.current) return;
    
    intervalRef.current = setInterval(() => {
      fetchExecution();
      fetchLogs();
    }, 2000); // 每2秒更新一次
  };

  // 停止实时监控
  const stopMonitoring = () => {
    if (intervalRef.current) {
      clearInterval(intervalRef.current);
      intervalRef.current = null;
    }
  };

  // 停止测试执行
  const stopExecution = async () => {
    try {
      await testExecutionAPI.stop(executionId);
      await fetchExecution();
    } catch (error) {
      console.error('停止执行失败:', error);
    }
  };

  // 获取状态颜色
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'FAILED': return 'error';
      case 'RUNNING': return 'processing';
      case 'CANCELLED': return 'warning';
      case 'PENDING': return 'default';
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

  // 获取状态图标
  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED': return <CheckCircleOutlined />;
      case 'FAILED': return <CloseCircleOutlined />;
      case 'RUNNING': return <PlayCircleOutlined />;
      case 'CANCELLED': return <StopOutlined />;
      case 'PENDING': return <ClockCircleOutlined />;
      default: return <ClockCircleOutlined />;
    }
  };

  // 格式化时间
  const formatTime = (timestamp: string) => {
    return new Date(timestamp).toLocaleString();
  };

  // 格式化持续时间
  const formatDuration = (startTime: string, endTime?: string) => {
    const start = new Date(startTime).getTime();
    const end = endTime ? new Date(endTime).getTime() : Date.now();
    const duration = end - start;
    
    if (duration < 1000) {
      return `${duration}ms`;
    } else if (duration < 60000) {
      return `${(duration / 1000).toFixed(1)}s`;
    } else {
      return `${(duration / 60000).toFixed(1)}m`;
    }
  };

  // 获取日志统计
  const getLogStats = () => {
    const total = logs.length;
    const errors = logs.filter(log => log.level === 'ERROR').length;
    const warnings = logs.filter(log => log.level === 'WARN').length;
    const info = logs.filter(log => log.level === 'INFO').length;
    
    return { total, errors, warnings, info };
  };

  // 初始化
  useEffect(() => {
    fetchExecution();
    fetchLogs();
    startMonitoring();

    return () => {
      stopMonitoring();
    };
  }, [executionId]);

  // 清理
  useEffect(() => {
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, []);

  if (!execution) {
    return (
      <div className="test-execution-monitor">
        <Card>
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <Spin size="large" />
            <div style={{ marginTop: '16px' }}>
              <Text>加载执行信息中...</Text>
            </div>
          </div>
        </Card>
      </div>
    );
  }

  const logStats = getLogStats();

  return (
    <div className="test-execution-monitor">
      <Row gutter={[16, 16]}>
        {/* 执行状态卡片 */}
        <Col span={24}>
          <Card>
            <div className="execution-header">
              <div className="execution-title">
                <Space>
                  {getStatusIcon(execution.status)}
                  <Title level={3} style={{ margin: 0 }}>
                    {execution.testSuiteName}
                  </Title>
                  <Badge
                    status={getStatusColor(execution.status)}
                    text={getStatusText(execution.status)}
                  />
                </Space>
              </div>
              
              <Space>
                <Button
                  type="primary"
                  icon={<EyeOutlined />}
                  onClick={() => setShowLogs(true)}
                >
                  查看详细日志
                </Button>
                
                {execution.status === 'RUNNING' && (
                  <Button
                    danger
                    icon={<StopOutlined />}
                    onClick={stopExecution}
                  >
                    停止执行
                  </Button>
                )}
                
                <Button
                  icon={<ReloadOutlined />}
                  onClick={() => {
                    fetchExecution();
                    fetchLogs();
                  }}
                >
                  刷新
                </Button>
              </Space>
            </div>

            <Row gutter={16} style={{ marginTop: 16 }}>
              <Col span={6}>
                <Statistic
                  title="开始时间"
                  value={formatTime(execution.startTime)}
                  valueStyle={{ fontSize: '14px' }}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="持续时间"
                  value={formatDuration(execution.startTime, execution.endTime)}
                  valueStyle={{ fontSize: '14px' }}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="进度"
                  value={execution.progress || 0}
                  suffix="%"
                  valueStyle={{ fontSize: '14px' }}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="日志总数"
                  value={logStats.total}
                  valueStyle={{ fontSize: '14px' }}
                />
              </Col>
            </Row>

            {execution.progress !== undefined && execution.status === 'RUNNING' && (
              <div style={{ marginTop: 16 }}>
                <Progress
                  percent={execution.progress}
                  status="active"
                  strokeColor={{
                    '0%': '#108ee9',
                    '100%': '#87d068',
                  }}
                />
              </div>
            )}

            {execution.result && (
              <Alert
                message={execution.result}
                type={execution.status === 'COMPLETED' ? 'success' : 'error'}
                style={{ marginTop: 16 }}
              />
            )}
          </Card>
        </Col>

        {/* 日志统计 */}
        <Col span={24}>
          <Card title="执行统计">
            <Row gutter={16}>
              <Col span={6}>
                <Statistic
                  title="总日志数"
                  value={logStats.total}
                  prefix={<ClockCircleOutlined />}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="错误日志"
                  value={logStats.errors}
                  valueStyle={{ color: '#cf1322' }}
                  prefix={<CloseCircleOutlined />}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="警告日志"
                  value={logStats.warnings}
                  valueStyle={{ color: '#fa8c16' }}
                  prefix={<ExclamationCircleOutlined />}
                />
              </Col>
              <Col span={6}>
                <Statistic
                  title="信息日志"
                  value={logStats.info}
                  valueStyle={{ color: '#1890ff' }}
                  prefix={<CheckCircleOutlined />}
                />
              </Col>
            </Row>
          </Card>
        </Col>

        {/* 最近日志 */}
        <Col span={24}>
          <Card title="最近日志">
            {logs.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '20px' }}>
                <Text type="secondary">暂无日志</Text>
              </div>
            ) : (
              <Timeline>
                {logs.slice(-10).reverse().map((log, index) => (
                  <Timeline.Item
                    key={log.id}
                    color={
                      log.level === 'ERROR' ? 'red' :
                      log.level === 'WARN' ? 'orange' :
                      log.level === 'INFO' ? 'blue' : 'green'
                    }
                    dot={getStatusIcon(log.level)}
                  >
                    <div className="log-item">
                      <div className="log-header">
                        <Space>
                          <Tag color={
                            log.level === 'ERROR' ? 'red' :
                            log.level === 'WARN' ? 'orange' :
                            log.level === 'INFO' ? 'blue' : 'green'
                          }>
                            {log.level}
                          </Tag>
                          <Text type="secondary">{formatTime(log.timestamp)}</Text>
                          <Text strong>{log.step}</Text>
                        </Space>
                      </div>
                      <div className="log-message">
                        <Text>{log.message}</Text>
                      </div>
                    </div>
                  </Timeline.Item>
                ))}
              </Timeline>
            )}
          </Card>
        </Col>
      </Row>

      {/* 详细日志模态框 */}
      <Modal
        title="详细执行日志"
        open={showLogs}
        onCancel={() => setShowLogs(false)}
        width="90%"
        style={{ top: 20 }}
        footer={[
          <Button key="close" onClick={() => setShowLogs(false)}>
            关闭
          </Button>,
        ]}
      >
        <TestExecutionLogs
          executionId={executionId}
          onExecutionComplete={onExecutionComplete}
        />
      </Modal>
    </div>
  );
};

export default TestExecutionMonitor;
