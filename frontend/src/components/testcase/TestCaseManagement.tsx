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
  // 移除selectedType状态，测试用例不再有类型字段
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

  // 监听编辑状态变化，设置表单值
  useEffect(() => {
    if (editingCase && modalVisible) {
      // 处理标签数据
      const tagsArray = editingCase.tags && typeof editingCase.tags === 'string' 
        ? editingCase.tags.split(',').map((tag: string) => tag.trim()).filter((tag: string) => tag) 
        : [];
      
      console.log('useEffect - 编辑测试用例 - 原始标签:', editingCase.tags);
      console.log('useEffect - 编辑测试用例 - 处理后的标签数组:', tagsArray);
      
      form.setFieldsValue({
        name: editingCase.name,
        description: editingCase.description,
        priority: editingCase.priority,
        status: editingCase.status,
        config: editingCase.config,
        tags: tagsArray,
      });
    }
  }, [editingCase, modalVisible, form]);

  // 处理创建/编辑测试用例
  const handleSubmit = async (values: TestCaseForm) => {
    try {
      console.log('表单提交数据:', values);
      console.log('标签字段值:', values.tags);
      
      // 处理tags字段：如果是数组则转换为逗号分隔的字符串
      const processedValues = {
        ...values,
        tags: Array.isArray(values.tags) ? values.tags.join(',') : values.tags
      };
      
      console.log('处理后的提交数据:', processedValues);
      
      if (editingCase) {
        await testCaseAPI.update(editingCase.id, processedValues);
        message.success('测试用例更新成功');
      } else {
        await testCaseAPI.create(processedValues);
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

  // 移除单个测试用例执行功能，避免数据污染和系统状态混乱
  // 测试用例应该通过测试套件统一执行，确保环境隔离和执行顺序

  // 打开编辑模态框
  const openEditModal = (testCase: TestCase) => {
    setEditingCase(testCase);
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
    const matchesPriority = !selectedPriority || testCase.priority === selectedPriority;
    return matchesSearch && matchesPriority;
  });

  // 移除getTypeIcon函数，测试用例不再有类型字段

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
      render: (text: string) => (
        <Text strong>{text}</Text>
      ),
    },
    // 移除类型列，测试用例类型由所属的测试套件决定
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
          {/* 移除单个测试用例执行按钮，避免数据污染风险 */}
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
            {/* 移除类型选择器，测试用例不再有类型字段 */}
            <Col xs={24} sm={12} md={8}>
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
                  // 移除setSelectedType，测试用例不再有类型字段
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
            <Col span={24}>
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
                  console.log('getValueProps 被调用，输入值:', value, '类型:', typeof value);
                  
                  // 将字符串转换为数组用于显示
                  if (!value) {
                    console.log('getValueProps - 值为空，返回空数组');
                    return { value: [] };
                  }
                  if (typeof value === 'string') {
                    const result = value.split(',').map((tag: string) => tag.trim()).filter((tag: string) => tag);
                    console.log('getValueProps - 字符串转换结果:', result);
                    return { value: result };
                  }
                  if (Array.isArray(value)) {
                    console.log('getValueProps - 已经是数组，直接返回:', value);
                    return { value };
                  }
                  console.log('getValueProps - 未知类型，返回空数组');
                  return { value: [] };
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