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
  Table,
  Tooltip,
  DatePicker,
  Select,
  Input,
  Empty,
} from 'antd';
import {
  BarChartOutlined,
  FileTextOutlined,
  DownloadOutlined,
  EyeOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  ExclamationCircleOutlined,
  FilterOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import { testReportAPI } from '../services/api';
import { TestReport } from '../types';
import './ReportDashboard.css';
import { Dayjs } from 'dayjs';

const { Title, Text } = Typography;
const { Option } = Select;
const { RangePicker } = DatePicker;
const { TabPane } = Tabs;

const ReportDashboard: React.FC = () => {
  const [reports, setReports] = useState<TestReport[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedReport, setSelectedReport] = useState<TestReport | null>(null);
  const [dateRange, setDateRange] = useState<[Dayjs | null, Dayjs | null]>([null, null]);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [searchText, setSearchText] = useState('');

  // 获取测试报告列表
  const fetchReports = async () => {
    setLoading(true);
    try {
      const response = await testReportAPI.getAll();
      setReports(response.data);
    } catch (error) {
      message.error('获取测试报告列表失败');
      console.error('获取测试报告列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 初始化数据
  useEffect(() => {
    fetchReports();
  }, []);

  // 处理查看报告详情
  const handleViewReport = (report: TestReport) => {
    setSelectedReport(report);
  };

  // 处理下载报告
  const handleDownloadReport = (report: TestReport) => {
    try {
      // 这里应该调用下载API
      message.success('报告下载已开始');
      console.log('下载报告:', report.id);
    } catch (error) {
      message.error('报告下载失败');
      console.error('报告下载失败:', error);
    }
  };

  // 处理返回列表
  const handleBackToList = () => {
    setSelectedReport(null);
  };

  // 获取状态图标
  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PASSED':
        return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
      case 'FAILED':
        return <CloseCircleOutlined style={{ color: '#ff4d4f' }} />;
      case 'SKIPPED':
        return <ExclamationCircleOutlined style={{ color: '#faad14' }} />;
      case 'RUNNING':
        return <ClockCircleOutlined style={{ color: '#1890ff' }} />;
      default:
        return <ClockCircleOutlined />;
    }
  };

  // 获取状态颜色
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'PASSED':
        return 'success';
      case 'FAILED':
        return 'error';
      case 'SKIPPED':
        return 'warning';
      case 'RUNNING':
        return 'processing';
      default:
        return 'default';
    }
  };

  // 过滤报告
  const filteredReports = reports.filter(report => {
    const matchesSearch = report.name.toLowerCase().includes(searchText.toLowerCase());
    const matchesStatus = !statusFilter || (report as any).status === statusFilter;
    const matchesDate = !dateRange[0] || !dateRange[1] || (
      new Date(report.createdAt) >= dateRange[0].toDate() &&
      new Date(report.createdAt) <= dateRange[1].toDate()
    );
    return matchesSearch && matchesStatus && matchesDate;
  });

  // 计算统计信息
  const getStats = () => {
    const total = reports.length;
    const passed = reports.filter(r => (r as any).status === 'PASSED').length;
    const failed = reports.filter(r => (r as any).status === 'FAILED').length;
    const skipped = reports.filter(r => (r as any).status === 'SKIPPED').length;
    const successRate = total > 0 ? ((passed / total) * 100).toFixed(1) : '0';

    return { total, passed, failed, skipped, successRate };
  };

  const stats = getStats();

  // 表格列定义
  const columns = [
    {
      title: '报告名称',
      dataIndex: 'name',
      key: 'name',
      render: (text: string, record: TestReport) => (
        <Space>
          <FileTextOutlined />
          <Text strong>{text}</Text>
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status: string) => (
        <Badge 
          status={getStatusColor(status) as any}
          text={status}
        />
      ),
    },
    {
      title: '通过率',
      dataIndex: 'passRate',
      key: 'passRate',
      render: (passRate: number) => (
        <Progress
          percent={passRate}
          size="small"
          status={passRate >= 80 ? 'success' : passRate >= 60 ? 'normal' : 'exception'}
        />
      ),
    },
    {
      title: '总测试数',
      dataIndex: 'totalTests',
      key: 'totalTests',
      render: (total: number, record: TestReport) => (
        <Space>
          <Text>{total}</Text>
          <Text type="secondary">
            (通过: {record.passedTests}, 失败: {record.failedTests})
          </Text>
        </Space>
      ),
    },
    {
      title: '平均响应时间',
      dataIndex: 'averageResponseTime',
      key: 'averageResponseTime',
      render: (time: number) => `${time}ms`,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text: string) => new Date(text).toLocaleString(),
    },
    {
      title: '操作',
      key: 'actions',
      width: 150,
      render: (_: any, record: TestReport) => (
        <Space>
          <Tooltip title="查看详情">
            <Button
              type="text"
              icon={<EyeOutlined />}
              onClick={() => handleViewReport(record)}
            />
          </Tooltip>
          <Tooltip title="下载报告">
            <Button
              type="text"
              icon={<DownloadOutlined />}
              onClick={() => handleDownloadReport(record)}
            />
          </Tooltip>
        </Space>
      ),
    },
  ];

  if (selectedReport) {
    return (
      <div className="report-dashboard">
        <div className="page-header">
          <Button onClick={handleBackToList}>
            返回列表
          </Button>
          <Title level={2}>
            <FileTextOutlined /> 报告详情
          </Title>
        </div>

        <Card className="report-detail-card">
          <div className="report-summary">
            <Row gutter={[24, 24]}>
              <Col xs={24} sm={6}>
                <Statistic
                  title="总测试数"
                  value={selectedReport.totalTests}
                  prefix={<FileTextOutlined />}
                />
              </Col>
              <Col xs={24} sm={6}>
                <Statistic
                  title="通过数"
                  value={selectedReport.passedTests}
                  valueStyle={{ color: '#52c41a' }}
                  prefix={<CheckCircleOutlined />}
                />
              </Col>
              <Col xs={24} sm={6}>
                <Statistic
                  title="失败数"
                  value={selectedReport.failedTests}
                  valueStyle={{ color: '#ff4d4f' }}
                  prefix={<CloseCircleOutlined />}
                />
              </Col>
              <Col xs={24} sm={6}>
                <Statistic
                  title="通过率"
                  value={selectedReport.passRate}
                  suffix="%"
                  valueStyle={{ color: '#1890ff' }}
                />
              </Col>
            </Row>
          </div>

          <Divider />

          <div className="report-details">
            <Title level={4}>测试详情</Title>
            <Table
              dataSource={selectedReport.details}
              columns={[
                {
                  title: '测试用例',
                  dataIndex: 'testCaseName',
                  key: 'testCaseName',
                },
                {
                  title: '状态',
                  dataIndex: 'status',
                  key: 'status',
                  render: (status: string) => (
                    <Badge 
                      status={getStatusColor(status) as any}
                      text={status}
                    />
                  ),
                },
                {
                  title: '响应时间',
                  dataIndex: 'responseTime',
                  key: 'responseTime',
                  render: (time: number) => `${time}ms`,
                },
                {
                  title: '错误信息',
                  dataIndex: 'errorMessage',
                  key: 'errorMessage',
                  render: (text: string) => text || '-',
                },
              ]}
              pagination={false}
              size="small"
            />
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="report-dashboard">
      <div className="page-header">
        <Title level={2}>
          <BarChartOutlined /> 报告中心
        </Title>
        <Space>
          <Button
            icon={<ReloadOutlined />}
            onClick={fetchReports}
          >
            刷新
          </Button>
        </Space>
      </div>

      {/* 统计卡片 */}
      <Row gutter={[24, 24]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={6}>
          <Card className="stat-card">
            <Statistic
              title="总报告数"
              value={stats.total}
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card className="stat-card">
            <Statistic
              title="成功报告"
              value={stats.passed}
              valueStyle={{ color: '#52c41a' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card className="stat-card">
            <Statistic
              title="失败报告"
              value={stats.failed}
              valueStyle={{ color: '#ff4d4f' }}
              prefix={<CloseCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={6}>
          <Card className="stat-card">
            <Statistic
              title="成功率"
              value={stats.successRate}
              suffix="%"
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 报告列表 */}
      <Card className="report-list-card">
        <div className="report-filters">
          <Row gutter={[16, 16]} align="middle">
            <Col xs={24} sm={8} md={6}>
              <RangePicker
                placeholder={['开始日期', '结束日期']}
                value={dateRange}
                onChange={(dates) => setDateRange(dates || [null, null])}
                style={{ width: '100%' }}
              />
            </Col>
            <Col xs={24} sm={8} md={6}>
              <Select
                placeholder="选择状态"
                value={statusFilter}
                onChange={setStatusFilter}
                style={{ width: '100%' }}
                allowClear
              >
                <Option value="PASSED">通过</Option>
                <Option value="FAILED">失败</Option>
                <Option value="SKIPPED">跳过</Option>
                <Option value="RUNNING">运行中</Option>
              </Select>
            </Col>
            <Col xs={24} sm={8} md={8}>
              <Input
                placeholder="搜索报告名称"
                prefix={<SearchOutlined />}
                value={searchText}
                onChange={(e: any) => setSearchText(e.target.value)}
                allowClear
              />
            </Col>
            <Col xs={24} sm={24} md={4}>
              <Space>
                <Button icon={<FilterOutlined />}>
                  筛选
                </Button>
                <Button onClick={() => {
                  setSearchText('');
                  setStatusFilter('');
                  setDateRange([null, null]);
                  fetchReports();
                }}>
                  重置
                </Button>
              </Space>
            </Col>
          </Row>
        </div>

        <Divider />

        <Tabs defaultActiveKey="list">
          <TabPane tab="报告列表" key="list">
            <Table
              columns={columns}
              dataSource={filteredReports}
              rowKey="id"
              loading={loading}
              pagination={{
                pageSize: 10,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total: number) => `共 ${total} 个报告`,
              }}
              className="report-table"
            />
          </TabPane>
          <TabPane tab="图表视图" key="chart">
            <div className="chart-container">
              <Empty description="图表功能开发中..." />
            </div>
          </TabPane>
        </Tabs>
      </Card>
    </div>
  );
};

export default ReportDashboard;