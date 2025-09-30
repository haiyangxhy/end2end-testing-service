import React, { useState, useEffect, useRef } from 'react';
import {
  Card,
  List,
  Typography,
  Tag,
  Space,
  Button,
  Tooltip,
  Collapse,
  Badge,
  Divider,
  Empty,
  Spin,
  Alert,
} from 'antd';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  StopOutlined,
  ReloadOutlined,
  ClearOutlined,
  InfoCircleOutlined,
  ExclamationCircleOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons';
import { testExecutionAPI } from '../../services/api';
import { TestExecution, TestExecutionLog } from '../../types';
import './TestExecutionLogs.css';

const { Title, Text, Paragraph } = Typography;
const { Panel } = Collapse;

interface TestExecutionLogsProps {
  executionId: string;
  onExecutionComplete?: (execution: TestExecution) => void;
}

const TestExecutionLogs: React.FC<TestExecutionLogsProps> = ({
  executionId,
  onExecutionComplete,
}) => {
  const [execution, setExecution] = useState<TestExecution | null>(null);
  const [logs, setLogs] = useState<TestExecutionLog[]>([]);
  const [loading, setLoading] = useState(false);
  const [eventSource, setEventSource] = useState<EventSource | null>(null);
  const [isStreaming, setIsStreaming] = useState(false);
  const [autoScroll, setAutoScroll] = useState(true);
  const logsEndRef = useRef<HTMLDivElement>(null);

  // 获取执行详情
  const fetchExecution = async () => {
    try {
      const response = await testExecutionAPI.getById(executionId);
      setExecution(response.data);
    } catch (error) {
      console.error('获取执行详情失败:', error);
    }
  };

  // 获取历史日志
  const fetchLogs = async () => {
    try {
      setLoading(true);
      const response = await testExecutionAPI.getLogs(executionId);
      setLogs(response.data);
    } catch (error) {
      console.error('获取执行日志失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 开始实时日志流
  const startLogStreaming = () => {
    if (eventSource) {
      eventSource.close();
    }

    const es = testExecutionAPI.streamLogs(executionId);
    setEventSource(es);
    setIsStreaming(true);

    es.onopen = () => {
      console.log('SSE连接已建立');
    };

    es.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        
        if (event.type === 'log') {
          setLogs(prevLogs => [...prevLogs, data]);
        } else if (event.type === 'complete') {
          setExecution(data);
          setIsStreaming(false);
          onExecutionComplete?.(data);
        }
      } catch (error) {
        console.error('解析SSE数据失败:', error);
      }
    };

    es.onerror = (error) => {
      console.error('SSE连接错误:', error);
      setIsStreaming(false);
    };
  };

  // 停止日志流
  const stopLogStreaming = () => {
    if (eventSource) {
      eventSource.close();
      setEventSource(null);
      setIsStreaming(false);
    }
  };

  // 停止测试执行
  const stopExecution = async () => {
    try {
      await testExecutionAPI.stop(executionId);
      stopLogStreaming();
      await fetchExecution();
    } catch (error) {
      console.error('停止执行失败:', error);
    }
  };

  // 清空日志
  const clearLogs = () => {
    setLogs([]);
  };

  // 自动滚动到底部
  useEffect(() => {
    if (autoScroll && logsEndRef.current) {
      logsEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [logs, autoScroll]);

  // 初始化
  useEffect(() => {
    fetchExecution();
    fetchLogs();
    
    // 如果执行还在运行，开始实时流
    if (execution && execution.status === 'RUNNING') {
      startLogStreaming();
    }

    return () => {
      stopLogStreaming();
    };
  }, [executionId]);

  // 获取日志级别颜色
  const getLogLevelColor = (level: string) => {
    switch (level) {
      case 'ERROR': return 'red';
      case 'WARN': return 'orange';
      case 'INFO': return 'blue';
      case 'DEBUG': return 'green';
      default: return 'default';
    }
  };

  // 获取日志级别图标
  const getLogLevelIcon = (level: string) => {
    switch (level) {
      case 'ERROR': return <CloseCircleOutlined />;
      case 'WARN': return <ExclamationCircleOutlined />;
      case 'INFO': return <InfoCircleOutlined />;
      case 'DEBUG': return <CheckCircleOutlined />;
      default: return <InfoCircleOutlined />;
    }
  };

  // 格式化时间
  const formatTime = (timestamp: string) => {
    return new Date(timestamp).toLocaleTimeString();
  };

  // 格式化持续时间
  const formatDuration = (duration: number) => {
    if (duration < 1000) {
      return `${duration}ms`;
    } else {
      return `${(duration / 1000).toFixed(2)}s`;
    }
  };

  // 渲染请求/响应数据
  const renderData = (data: any, title: string) => {
    if (!data) return null;

    return (
      <Panel header={title} key={title}>
        <pre style={{ 
          background: '#f5f5f5', 
          padding: '12px', 
          borderRadius: '4px',
          overflow: 'auto',
          maxHeight: '300px'
        }}>
          {JSON.stringify(data, null, 2)}
        </pre>
      </Panel>
    );
  };

  return (
    <div className="test-execution-logs">
      <Card>
        <div className="execution-header">
          <Space>
            <Title level={4} style={{ margin: 0 }}>
              测试执行日志
            </Title>
            {execution && (
              <Badge 
                status={execution.status === 'COMPLETED' ? 'success' : 
                       execution.status === 'FAILED' ? 'error' : 
                       execution.status === 'RUNNING' ? 'processing' : 'default'}
                text={execution.status}
              />
            )}
          </Space>
          
          <Space>
            <Button
              type="primary"
              icon={<PlayCircleOutlined />}
              onClick={startLogStreaming}
              disabled={isStreaming || (execution?.status !== 'RUNNING')}
            >
              开始监听
            </Button>
            
            <Button
              icon={<PauseCircleOutlined />}
              onClick={stopLogStreaming}
              disabled={!isStreaming}
            >
              暂停监听
            </Button>
            
            <Button
              danger
              icon={<StopOutlined />}
              onClick={stopExecution}
              disabled={!execution || execution.status !== 'RUNNING'}
            >
              停止执行
            </Button>
            
            <Button
              icon={<ReloadOutlined />}
              onClick={fetchLogs}
              loading={loading}
            >
              刷新
            </Button>
            
            <Button
              icon={<ClearOutlined />}
              onClick={clearLogs}
            >
              清空
            </Button>
          </Space>
        </div>

        <Divider />

        {execution && (
          <div className="execution-info">
            <Space wrap>
              <Text strong>套件名称:</Text>
              <Text>{execution.testSuiteName}</Text>
              
              <Text strong>开始时间:</Text>
              <Text>{new Date(execution.startTime).toLocaleString()}</Text>
              
              {execution.endTime && (
                <>
                  <Text strong>结束时间:</Text>
                  <Text>{new Date(execution.endTime).toLocaleString()}</Text>
                </>
              )}
              
              {execution.duration && (
                <>
                  <Text strong>持续时间:</Text>
                  <Text>{formatDuration(execution.duration)}</Text>
                </>
              )}
              
              {execution.progress !== undefined && (
                <>
                  <Text strong>进度:</Text>
                  <Text>{execution.progress}%</Text>
                </>
              )}
            </Space>
          </div>
        )}

        <Divider />

        <div className="logs-container">
          {loading ? (
            <div style={{ textAlign: 'center', padding: '20px' }}>
              <Spin size="large" />
            </div>
          ) : logs.length === 0 ? (
            <Empty description="暂无日志" />
          ) : (
            <List
              dataSource={logs}
              renderItem={(log) => (
                <List.Item className="log-item">
                  <div className="log-content">
                    <div className="log-header">
                      <Space>
                        <Tag 
                          color={getLogLevelColor(log.level)}
                          icon={getLogLevelIcon(log.level)}
                        >
                          {log.level}
                        </Tag>
                        <Text type="secondary">{formatTime(log.timestamp)}</Text>
                        <Text strong>{log.step}</Text>
                        {log.duration && (
                          <Text type="secondary">
                            耗时: {formatDuration(log.duration)}
                          </Text>
                        )}
                      </Space>
                    </div>
                    
                    <div className="log-message">
                      <Text>{log.message}</Text>
                    </div>
                    
                    {(log.requestData || log.responseData) && (
                      <div className="log-data">
                        <Collapse size="small">
                          {renderData(log.requestData, '请求数据')}
                          {renderData(log.responseData, '响应数据')}
                        </Collapse>
                      </div>
                    )}
                  </div>
                </List.Item>
              )}
            />
          )}
          
          <div ref={logsEndRef} />
        </div>
      </Card>
    </div>
  );
};

export default TestExecutionLogs;
