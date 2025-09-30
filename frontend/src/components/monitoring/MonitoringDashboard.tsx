import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Progress,
  Table,
  Button,
  Space,
  Tabs,
  Spin,
  Tooltip,
  Typography,
} from 'antd';
import {
  PlayCircleOutlined,
  PauseCircleOutlined,
  ReloadOutlined,
  DownloadOutlined,
  EyeOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { monitoringAPI } from '../../services/api';
import { RealTimeData, StatisticsReport, TrendReport, PerformanceReport } from '../../types';
import './MonitoringDashboard.css';

const { Title, Text } = Typography;
const { TabPane } = Tabs;

const MonitoringDashboard: React.FC = () => {
  const [realTimeData, setRealTimeData] = useState<RealTimeData | null>(null);
  const [statistics, setStatistics] = useState<StatisticsReport | null>(null);
  const [trendData, setTrendData] = useState<TrendReport | null>(null);
  const [performanceData, setPerformanceData] = useState<PerformanceReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  // 获取实时数据
  const fetchRealTimeData = async () => {
    try {
      const response = await monitoringAPI.getRealTimeData();
      setRealTimeData(response.data);
    } catch (error) {
      console.error('获取实时数据失败:', error);
    }
  };

  // 获取统计数据
  const fetchStatistics = async () => {
    try {
      const response = await monitoringAPI.getStatistics();
      setStatistics(response.data);
    } catch (error) {
      console.error('获取统计数据失败:', error);
    }
  };

  // 获取趋势数据
  const fetchTrendData = async () => {
    try {
      const response = await monitoringAPI.generateTrendReport('default', 7);
      setTrendData(response.data);
    } catch (error) {
      console.error('获取趋势数据失败:', error);
    }
  };

  // 获取性能数据
  const fetchPerformanceData = async () => {
    try {
      const response = await monitoringAPI.generatePerformanceReport('default', 7);
      setPerformanceData(response.data);
    } catch (error) {
      console.error('获取性能数据失败:', error);
    }
  };

  // 刷新所有数据
  const refreshAll = async () => {
    setRefreshing(true);
    try {
      await Promise.all([
        fetchRealTimeData(),
        fetchStatistics(),
        fetchTrendData(),
        fetchPerformanceData(),
      ]);
    } finally {
      setRefreshing(false);
    }
  };

  // 初始化数据
  useEffect(() => {
    const initData = async () => {
      setLoading(true);
      await refreshAll();
      setLoading(false);
    };
    initData();

    // 设置定时刷新
    const interval = setInterval(fetchRealTimeData, 5000);
    return () => clearInterval(interval);
  }, []);

  // 处理测试执行操作
  const handleTestAction = (action: 'start' | 'stop' | 'restart', testId: string) => {
    console.log(`${action} test:`, testId);
    // 这里应该调用相应的API
  };

  // 处理报告导出
  const handleExportReport = (format: 'json' | 'csv') => {
    console.log(`导出报告格式:`, format);
    // 这里应该调用相应的API
  };

  // 准备图表数据
  const prepareTrendChartData = () => {
    if (!trendData?.dailyStats) return [];
    
    return Object.entries(trendData.dailyStats).map(([date, stats]: [string, any]) => ({
      date,
      total: stats.total,
      passed: stats.passed,
      failed: stats.failed,
      successRate: stats.total > 0 ? (stats.passed / stats.total) * 100 : 0,
    }));
  };

  const preparePerformanceChartData = () => {
    if (!performanceData) return [];
    
    return [
      { name: '最小执行时间', value: performanceData.minExecutionTime },
      { name: '最大执行时间', value: performanceData.maxExecutionTime },
      { name: '平均执行时间', value: performanceData.avgExecutionTime },
    ];
  };

  const trendChartData = prepareTrendChartData();
  const performanceChartData = preparePerformanceChartData();

  if (loading) {
    return (
      <div className="monitoring-dashboard-loading">
        <Spin size="large" />
        <Text>加载监控数据中...</Text>
      </div>
    );
  }

  return (
    <div className="monitoring-dashboard">
      <div className="dashboard-header">
        <Title level={2}>监控仪表板</Title>
        <Space>
          <Button
            type="primary"
            icon={<ReloadOutlined />}
            loading={refreshing}
            onClick={refreshAll}
          >
            刷新
          </Button>
          <Button icon={<DownloadOutlined />} onClick={() => handleExportReport('csv')}>
            导出报告
          </Button>
        </Space>
      </div>

      {/* 实时状态卡片 */}
      <Row gutter={[16, 16]} className="dashboard-cards">
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="正在运行"
              value={realTimeData?.runningTests || 0}
              prefix={<PlayCircleOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="最近完成"
              value={realTimeData?.recentCompleted || 0}
              prefix={<CheckCircleOutlined />}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="最近失败"
              value={realTimeData?.recentFailed || 0}
              prefix={<CloseCircleOutlined />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card>
            <Statistic
              title="今日成功率"
              value={statistics?.today.successRate || 0}
              suffix="%"
              prefix={<ClockCircleOutlined />}
              valueStyle={{ 
                color: (statistics?.today.successRate || 0) > 80 ? '#52c41a' : '#ff4d4f' 
              }}
            />
          </Card>
        </Col>
      </Row>

      {/* 详细内容区域 */}
      <Tabs defaultActiveKey="overview" className="dashboard-tabs">
        <TabPane tab="总览" key="overview">
          <Row gutter={[16, 16]}>
            {/* 趋势图表 */}
            <Col xs={24} lg={16}>
              <Card title="测试执行趋势" className="chart-card">
                <ResponsiveContainer width="100%" height={300}>
                  <AreaChart data={trendChartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <RechartsTooltip />
                    <Legend />
                    <Area
                      type="monotone"
                      dataKey="passed"
                      stackId="1"
                      stroke="#52c41a"
                      fill="#52c41a"
                      name="通过"
                    />
                    <Area
                      type="monotone"
                      dataKey="failed"
                      stackId="1"
                      stroke="#ff4d4f"
                      fill="#ff4d4f"
                      name="失败"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </Card>
            </Col>

            {/* 成功率图表 */}
            <Col xs={24} lg={8}>
              <Card title="成功率趋势" className="chart-card">
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={trendChartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis domain={[0, 100]} />
                    <RechartsTooltip formatter={(value: any) => [`${value}%`, '成功率']} />
                    <Line
                      type="monotone"
                      dataKey="successRate"
                      stroke="#1890ff"
                      strokeWidth={2}
                      dot={{ fill: '#1890ff' }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </Card>
            </Col>
          </Row>
        </TabPane>

        <TabPane tab="实时监控" key="realtime">
          <Row gutter={[16, 16]}>
            {/* 正在运行的测试 */}
            <Col xs={24} lg={12}>
              <Card title="正在运行的测试" className="realtime-card">
                {realTimeData?.runningTestDetails?.length ? (
                  <Table
                    dataSource={realTimeData.runningTestDetails}
                    columns={[
                      {
                        title: '测试ID',
                        dataIndex: 'id',
                        key: 'id',
                        render: (text: any) => <Text code>{text}</Text>,
                      },
                      {
                        title: '套件ID',
                        dataIndex: 'suiteId',
                        key: 'suiteId',
                        render: (text: any) => <Text code>{text}</Text>,
                      },
                      {
                        title: '运行时间',
                        dataIndex: 'duration',
                        key: 'duration',
                        render: (value: any) => `${Math.floor(value / 1000)}s`,
                      },
                      {
                        title: '操作',
                        key: 'actions',
                        render: (_: any, record: any) => (
                          <Space>
                            <Tooltip title="停止">
                              <Button
                                type="text"
                                icon={<PauseCircleOutlined />}
                                onClick={() => handleTestAction('stop', record.id)}
                              />
                            </Tooltip>
                            <Tooltip title="查看详情">
                              <Button
                                type="text"
                                icon={<EyeOutlined />}
                                onClick={() => handleTestAction('restart', record.id)}
                              />
                            </Tooltip>
                          </Space>
                        ),
                      },
                    ]}
                    pagination={false}
                    size="small"
                  />
                ) : (
                  <div className="empty-state">
                    <Text type="secondary">当前没有正在运行的测试</Text>
                  </div>
                )}
              </Card>
            </Col>

            {/* 性能指标 */}
            <Col xs={24} lg={12}>
              <Card title="性能指标" className="performance-card">
                <ResponsiveContainer width="100%" height={200}>
                  <BarChart data={performanceChartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <RechartsTooltip formatter={(value: any) => [`${value}ms`, '执行时间']} />
                    <Bar dataKey="value" fill="#1890ff" />
                  </BarChart>
                </ResponsiveContainer>
              </Card>
            </Col>
          </Row>
        </TabPane>

        <TabPane tab="统计报告" key="statistics">
          <Row gutter={[16, 16]}>
            {/* 今日统计 */}
            <Col xs={24} md={8}>
              <Card title="今日统计" className="statistics-card">
                <Space direction="vertical" size="large" style={{ width: '100%' }}>
                  <div>
                    <Text strong>总测试数: </Text>
                    <Text>{statistics?.today.total || 0}</Text>
                  </div>
                  <div>
                    <Text strong>通过: </Text>
                    <Text style={{ color: '#52c41a' }}>{statistics?.today.passed || 0}</Text>
                  </div>
                  <div>
                    <Text strong>失败: </Text>
                    <Text style={{ color: '#ff4d4f' }}>{statistics?.today.failed || 0}</Text>
                  </div>
                  <div>
                    <Progress
                      percent={statistics?.today.successRate || 0}
                      strokeColor={{
                        '0%': '#ff4d4f',
                        '50%': '#faad14',
                        '100%': '#52c41a',
                      }}
                    />
                  </div>
                </Space>
              </Card>
            </Col>

            {/* 本周统计 */}
            <Col xs={24} md={8}>
              <Card title="本周统计" className="statistics-card">
                <Space direction="vertical" size="large" style={{ width: '100%' }}>
                  <div>
                    <Text strong>总测试数: </Text>
                    <Text>{statistics?.week.total || 0}</Text>
                  </div>
                  <div>
                    <Text strong>通过: </Text>
                    <Text style={{ color: '#52c41a' }}>{statistics?.week.passed || 0}</Text>
                  </div>
                  <div>
                    <Text strong>失败: </Text>
                    <Text style={{ color: '#ff4d4f' }}>{statistics?.week.failed || 0}</Text>
                  </div>
                  <div>
                    <Progress
                      percent={statistics?.week.successRate || 0}
                      strokeColor={{
                        '0%': '#ff4d4f',
                        '50%': '#faad14',
                        '100%': '#52c41a',
                      }}
                    />
                  </div>
                </Space>
              </Card>
            </Col>

            {/* 总体统计 */}
            <Col xs={24} md={8}>
              <Card title="总体统计" className="statistics-card">
                <Space direction="vertical" size="large" style={{ width: '100%' }}>
                  <div>
                    <Text strong>总测试数: </Text>
                    <Text>{statistics?.overall.total || 0}</Text>
                  </div>
                  <div>
                    <Text strong>通过: </Text>
                    <Text style={{ color: '#52c41a' }}>{statistics?.overall.passed || 0}</Text>
                  </div>
                  <div>
                    <Text strong>失败: </Text>
                    <Text style={{ color: '#ff4d4f' }}>{statistics?.overall.failed || 0}</Text>
                  </div>
                  <div>
                    <Progress
                      percent={statistics?.overall.successRate || 0}
                      strokeColor={{
                        '0%': '#ff4d4f',
                        '50%': '#faad14',
                        '100%': '#52c41a',
                      }}
                    />
                  </div>
                </Space>
              </Card>
            </Col>
          </Row>
        </TabPane>
      </Tabs>
    </div>
  );
};

export default MonitoringDashboard;
