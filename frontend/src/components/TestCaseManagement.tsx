import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  message,
  Popconfirm,
  Tag,
  Tooltip,
  Typography,
  Row,
  Col,
  Divider,
  Tabs,
  Badge,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  FileTextOutlined,
  SearchOutlined,
  FilterOutlined,
  BugOutlined,
  ApiOutlined,
  DesktopOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import { testCaseAPI } from '../services/api';
import { TestCase, TestCaseForm } from '../types';
import './TestCaseManagement.css';

const { Title, Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;
const { TabPane } = Tabs;

const TestCaseManagement: React.FC = () => {
  const [testCases, setTestCases] = useState<TestCase[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingCase, setEditingCase] = useState<TestCase | null>(null);
  const [searchText, setSearchText] = useState('');
  const [selectedType, setSelectedType] = useState<string>('');
  const [selectedPriority, setSelectedPriority] = useState<string>('');
  const [form] = Form.useForm();

  // 获取测试用例列表
  const fetchTestCases = async () => {
    setLoading(true);
    try {
      const response = await testCaseAPI.getAll();
      setTestCases(response.data);
    } catch (error) {
      message.error('获取测试用例列表失败');
      console.error('获取测试用例列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 初始化数据
  useEffect(() => {
    fetchTestCases();
  }, []);

  // 处理创建/编辑测试用例
  const handleSubmit = async (values: TestCaseForm) => {
    try {
      if (editingCase) {
        await testCaseAPI.update(editingCase.id, values);
        message.success('测试用例更新成功');
      } else {
        await testCaseAPI.create(values);
        message.success('测试用例创建成功');
      }
      setModalVisible(false);
      form.resetFields();
      setEditingCase(null);
      fetchTestCases();
    } catch (error) {
      message.error(editingCase ? '测试用例更新失败' : '测试用例创建失败');
      console.error('测试用例操作失败:', error);
    }
  };

  // 处理删除测试用例
  const handleDelete = async (id: string) => {
    try {
      await testCaseAPI.delete(id);
      message.success('测试用例删除成功');
      fetchTestCases();
    } catch (error) {
      message.error('测试用例删除失败');
      console.error('测试用例删除失败:', error);
    }
  };

  // 处理执行测试用例
  const handleExecute = async (id: string) => {
    try {
      // 这里应该调用执行API
      message.success('测试用例执行已启动');
      console.log('执行测试用例:', id);
    } catch (error) {
      message.error('测试用例执行失败');
      console.error('测试用例执行失败:', error);
    }
  };

  // 打开编辑模态框
  const openEditModal = (testCase: TestCase) => {
    setEditingCase(testCase);
    form.setFieldsValue({
      name: testCase.name,
      description: testCase.description,
      type: testCase.type,
      priority: testCase.priority,
      status: testCase.status,
      config: testCase.config,
      tags: testCase.tags && typeof testCase.tags === 'string' ? testCase.tags.split(',').map((tag: string) => tag.trim()).filter((tag: string) => tag) : [],
    });
    setModalVisible(true);
  };

  // 打开创建模态框
  const openCreateModal = () => {
    setEditingCase(null);
    form.setFieldsValue({
      type: 'API',
      priority: 'MEDIUM',
      status: 'ACTIVE',
      tags: [],
    });
    setModalVisible(true);
  };

  // 关闭模态框
  const handleModalClose = () => {
    setModalVisible(false);
    form.resetFields();
    setEditingCase(null);
  };

  // 过滤测试用例
  const filteredTestCases = testCases.filter(testCase => {
    const matchesSearch = testCase.name.toLowerCase().includes(searchText.toLowerCase()) ||
                         testCase.description?.toLowerCase().includes(searchText.toLowerCase());
    const matchesType = !selectedType || testCase.type === selectedType;
    const matchesPriority = !selectedPriority || testCase.priority === selectedPriority;
    return matchesSearch && matchesType && matchesPriority;
  });

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

  // 获取优先级颜色
  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'CRITICAL':
        return 'red';
      case 'HIGH':
        return 'orange';
      case 'MEDIUM':
        return 'blue';
      case 'LOW':
        return 'green';
      default:
        return 'default';
    }
  };

  // 表格列定义
  const columns = [
    {
      title: '用例名称',
      dataIndex: 'name',
      key: 'name',
      render: (text: string, record: TestCase) => (
        <Space>
          {getTypeIcon(record.type)}
          <Text strong>{text}</Text>
        </Space>
      ),
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type: string) => {
        const typeColors = {
          API: 'blue',
          UI: 'green',
          BUSINESS: 'orange',
        };
        return (
          <Tag color={typeColors[type as keyof typeof typeColors]}>
            {type}
          </Tag>
        );
      },
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      key: 'priority',
      render: (priority: string) => (
        <Badge 
          color={getPriorityColor(priority)} 
          text={priority}
        />
      ),
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      render: (text: string) => (
        <Tooltip title={text}>
          <Text type="secondary">{text || '-'}</Text>
        </Tooltip>
      ),
    },
    {
      title: '状态',
      dataIndex: 'isActive',
      key: 'isActive',
      render: (isActive: boolean) => (
        <Badge 
          status={isActive ? 'success' : 'default'} 
          text={isActive ? '启用' : '禁用'}
        />
      ),
    },
    {
      title: '标签',
      dataIndex: 'tags',
      key: 'tags',
      render: (tags: string) => {
        if (!tags) return '-';
        const tagArray = tags.split(',').map(tag => tag.trim()).filter(tag => tag);
        return (
          <Space wrap>
            {tagArray.slice(0, 2).map(tag => (
              <Tag key={tag}>{tag}</Tag>
            ))}
            {tagArray.length > 2 && (
              <Tag>+{tagArray.length - 2}</Tag>
            )}
          </Space>
        );
      },
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
      width: 200,
      render: (_: any, record: TestCase) => (
        <Space>
          <Tooltip title="编辑">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => openEditModal(record)}
            />
          </Tooltip>
          <Tooltip title="执行">
            <Button
              type="text"
              icon={<PlayCircleOutlined />}
              onClick={() => handleExecute(record.id)}
            />
          </Tooltip>
          <Popconfirm
            title="确定要删除这个测试用例吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Tooltip title="删除">
              <Button
                type="text"
                danger
                icon={<DeleteOutlined />}
              />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="test-case-management">
      <div className="page-header">
        <Title level={2}>
          <BugOutlined /> 测试用例管理
        </Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={openCreateModal}
        >
          创建测试用例
        </Button>
      </div>
      
      <Card className="test-case-card">
        <div className="test-case-filters">
          <Row gutter={[16, 16]} align="middle">
            <Col xs={24} sm={12} md={6}>
              <Select
                placeholder="选择类型"
                value={selectedType || undefined}
                onChange={setSelectedType}
                style={{ width: '100%' }}
                allowClear
              >
                <Option value="API">API</Option>
                <Option value="UI">UI</Option>
                <Option value="BUSINESS">业务</Option>
              </Select>
            </Col>
            <Col xs={24} sm={12} md={6}>
              <Select
                placeholder="选择优先级"
                value={selectedPriority || undefined}
                onChange={setSelectedPriority}
                style={{ width: '100%' }}
                allowClear
              >
                <Option value="CRITICAL">严重</Option>
                <Option value="HIGH">高</Option>
                <Option value="MEDIUM">中</Option>
                <Option value="LOW">低</Option>
              </Select>
            </Col>
            <Col xs={24} sm={12} md={8}>
              <Input
                placeholder="搜索用例名称或描述"
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
                  setSelectedType('');
                  setSelectedPriority('');
                  fetchTestCases();
                }}>
                  重置
                </Button>
              </Space>
            </Col>
          </Row>
        </div>

        <Divider />

        <Tabs defaultActiveKey="list">
          <TabPane tab="用例列表" key="list">
            <Table
              columns={columns}
              dataSource={filteredTestCases}
              rowKey="id"
              loading={loading}
              pagination={{
                pageSize: 10,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total: number) => `共 ${total} 个测试用例`,
              }}
              className="test-case-table"
            />
          </TabPane>
        </Tabs>
      </Card>

      {/* 创建/编辑测试用例模态框 */}
      <Modal
        title={editingCase ? '编辑测试用例' : '创建测试用例'}
        open={modalVisible}
        onCancel={handleModalClose}
        onOk={() => form.submit()}
        width={800}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          className="test-case-form"
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="name"
                label="用例名称"
                rules={[
                  { required: true, message: '请输入用例名称' },
                  { max: 100, message: '用例名称不能超过100个字符' },
                ]}
              >
                <Input placeholder="请输入用例名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="type"
                label="用例类型"
                rules={[{ required: true, message: '请选择用例类型' }]}
              >
                <Select placeholder="请选择用例类型">
                  <Option value="API">API</Option>
                  <Option value="UI">UI</Option>
                  <Option value="BUSINESS">业务</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="priority"
                label="优先级"
                rules={[{ required: true, message: '请选择优先级' }]}
              >
                <Select placeholder="请选择优先级">
                  <Option value="CRITICAL">严重</Option>
                  <Option value="HIGH">高</Option>
                  <Option value="MEDIUM">中</Option>
                  <Option value="LOW">低</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="tags"
                label="标签"
                getValueFromEvent={(value) => {
                  // 将数组转换为逗号分隔的字符串
                  if (Array.isArray(value)) {
                    return value.join(',');
                  }
                  if (typeof value === 'string') {
                    return value;
                  }
                  return '';
                }}
                getValueProps={(value) => {
                  // 将字符串转换为数组用于显示
                  if (!value || typeof value !== 'string') {
                    return { value: [] };
                  }
                  return {
                    value: value.split(',').map((tag: string) => tag.trim()).filter((tag: string) => tag)
                  };
                }}
              >
                <Select
                  mode="tags"
                  placeholder="请输入或选择标签"
                  style={{ width: '100%' }}
                  tokenSeparators={[',']}
                  allowClear
                  options={[
                    { value: 'API测试', label: 'API测试' },
                    { value: 'UI测试', label: 'UI测试' },
                    { value: '冒烟测试', label: '冒烟测试' },
                    { value: '回归测试', label: '回归测试' },
                    { value: '性能测试', label: '性能测试' },
                    { value: '安全测试', label: '安全测试' },
                    { value: '集成测试', label: '集成测试' },
                    { value: '单元测试', label: '单元测试' },
                    { value: '端到端测试', label: '端到端测试' },
                    { value: '兼容性测试', label: '兼容性测试' },
                  ]}
                  notFoundContent="暂无预设标签，可直接输入"
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="description"
            label="描述"
            rules={[
              { max: 500, message: '描述不能超过500个字符' },
            ]}
          >
            <TextArea
              rows={3}
              placeholder="请输入用例描述"
            />
          </Form.Item>

          <Form.Item
            name="config"
            label="配置"
            rules={[
              { required: true, message: '请输入用例配置' },
            ]}
            tooltip="JSON格式的测试配置"
          >
            <TextArea
              rows={6}
              placeholder="请输入JSON格式的测试配置"
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default TestCaseManagement;