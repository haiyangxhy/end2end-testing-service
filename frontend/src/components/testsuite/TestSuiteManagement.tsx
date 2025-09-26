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
  FolderOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import { testSuiteAPI, testCaseAPI } from '../services/api';
import { TestSuite, TestSuiteForm, TestCase } from '../types';
import TestSuiteCaseManagement from './TestSuiteCaseManagement';
import './TestSuiteManagement.css';

const { Title, Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;
const { TabPane } = Tabs;

const TestSuiteManagement: React.FC = () => {
  const [testSuites, setTestSuites] = useState<TestSuite[]>([]);
  const [testCases, setTestCases] = useState<TestCase[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingSuite, setEditingSuite] = useState<TestSuite | null>(null);
  const [searchText, setSearchText] = useState('');
  const [selectedType, setSelectedType] = useState<string>('');
  const [form] = Form.useForm();
  const [showCaseManagement, setShowCaseManagement] = useState(false);
  const [selectedSuite, setSelectedSuite] = useState<TestSuite | null>(null);

  // 获取测试套件列表
  const fetchTestSuites = async () => {
    setLoading(true);
    try {
      const response = await testSuiteAPI.getAll();
      setTestSuites(response.data);
    } catch (error) {
      message.error('获取测试套件列表失败');
      console.error('获取测试套件列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 获取测试用例列表
  const fetchTestCases = async () => {
    try {
      const response = await testCaseAPI.getAll();
      setTestCases(response.data);
    } catch (error) {
      message.error('获取测试用例列表失败');
      console.error('获取测试用例列表失败:', error);
    }
  };

  // 初始化数据
  useEffect(() => {
    fetchTestSuites();
    fetchTestCases();
  }, []);

  // 处理创建/编辑测试套件
  const handleSubmit = async (values: TestSuiteForm) => {
    try {
      // 将testCases数组转换为testSuiteCases格式
      const testSuiteCases = values.testCases?.map((testCaseId, index) => ({
        id: '', // 后端会生成
        suiteId: editingSuite?.id || '', // 创建时为空，更新时使用现有ID
        testCaseId: testCaseId,
        executionOrder: index + 1,
        isEnabled: true,
        createdAt: new Date().toISOString()
      })) || [];

      const submitData = {
        ...values,
        testSuiteCases: testSuiteCases
      };

      console.log('提交的测试套件数据:', submitData);

      if (editingSuite) {
        await testSuiteAPI.update(editingSuite.id, submitData);
        message.success('测试套件更新成功');
      } else {
        await testSuiteAPI.create(submitData);
        message.success('测试套件创建成功');
      }
      setModalVisible(false);
      form.resetFields();
      setEditingSuite(null);
      fetchTestSuites();
    } catch (error) {
      message.error(editingSuite ? '测试套件更新失败' : '测试套件创建失败');
      console.error('测试套件操作失败:', error);
    }
  };

  // 处理删除测试套件
  const handleDelete = async (id: string) => {
    try {
      await testSuiteAPI.delete(id);
      message.success('测试套件删除成功');
      fetchTestSuites();
    } catch (error) {
      message.error('测试套件删除失败');
      console.error('测试套件删除失败:', error);
    }
  };

  // 处理执行测试套件
  const handleExecute = async (id: string) => {
    try {
      // 这里应该调用执行API
      message.success('测试套件执行已启动');
      console.log('执行测试套件:', id);
    } catch (error) {
      message.error('测试套件执行失败');
      console.error('测试套件执行失败:', error);
    }
  };

  // 打开编辑模态框
  const openEditModal = (suite: TestSuite) => {
    setEditingSuite(suite);
    
    // 从testSuiteCases中提取testCaseId列表
    const testCaseIds = suite.testSuiteCases?.map(tc => tc.testCaseId) || [];
    
    form.setFieldsValue({
      name: suite.name,
      description: suite.description,
      type: suite.type,
      testCases: testCaseIds,
    });
    setModalVisible(true);
  };

  // 打开创建模态框
  const openCreateModal = () => {
    setEditingSuite(null);
    form.setFieldsValue({
      type: 'API',
      testCases: [],
    });
    setModalVisible(true);
  };

  // 关闭模态框
  const handleModalClose = () => {
    setModalVisible(false);
    form.resetFields();
    setEditingSuite(null);
  };

  // 管理测试用例
  const handleManageCases = (testSuite: TestSuite) => {
    setSelectedSuite(testSuite);
    setShowCaseManagement(true);
  };

  // 关闭测试用例管理
  const handleCloseCaseManagement = () => {
    setShowCaseManagement(false);
    setSelectedSuite(null);
  };

  // 过滤测试套件
  const filteredTestSuites = testSuites.filter(suite => {
    const matchesSearch = suite.name.toLowerCase().includes(searchText.toLowerCase()) ||
                         suite.description?.toLowerCase().includes(searchText.toLowerCase());
    const matchesType = !selectedType || suite.type === selectedType;
    return matchesSearch && matchesType;
  });

  // 表格列定义
  const columns = [
    {
      title: '套件名称',
      dataIndex: 'name',
      key: 'name',
      render: (text: string, record: TestSuite) => (
        <Space>
          <FolderOutlined />
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
      title: '测试用例数',
      dataIndex: 'testCases',
      key: 'testCases',
      render: (testCases: string[]) => (
        <Space>
          <FileTextOutlined />
          <Text>{testCases?.length || 0}</Text>
        </Space>
      ),
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
      width: 250,
      render: (_: any, record: TestSuite) => (
        <Space>
          <Tooltip title="管理测试用例">
            <Button
              type="text"
              icon={<SettingOutlined />}
              onClick={() => handleManageCases(record)}
            />
          </Tooltip>
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
            title="确定要删除这个测试套件吗？"
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

  // 如果显示测试用例管理，则渲染测试用例管理组件
  if (showCaseManagement && selectedSuite) {
    return (
      <TestSuiteCaseManagement
        testSuite={selectedSuite}
        onClose={handleCloseCaseManagement}
        onSave={async (testSuiteCases) => {
          try {
            // 更新测试套件，包含测试用例配置
            const updatedSuite = {
              ...selectedSuite,
              testSuiteCases: testSuiteCases
            };
            
            await testSuiteAPI.update(selectedSuite.id, updatedSuite);
            message.success('测试用例配置保存成功');
            handleCloseCaseManagement();
            // 刷新列表
            fetchTestSuites();
          } catch (error) {
            message.error('保存测试用例配置失败');
            console.error('保存测试用例配置失败:', error);
          }
        }}
      />
    );
  }

  return (
    <div className="test-suite-management">
      <div className="page-header">
        <Title level={2}>
          <FolderOutlined /> 测试套件管理
        </Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={openCreateModal}
        >
          创建测试套件
        </Button>
      </div>
      
      <Card className="test-suite-card">
        <div className="test-suite-filters">
          <Row gutter={[16, 16]} align="middle">
            <Col xs={24} sm={12} md={8}>
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
            <Col xs={24} sm={12} md={8}>
              <Input
                placeholder="搜索套件名称或描述"
                prefix={<SearchOutlined />}
                value={searchText}
                onChange={(e: any) => setSearchText(e.target.value)}
                allowClear
              />
            </Col>
            <Col xs={24} sm={24} md={8}>
              <Space>
                <Button icon={<FilterOutlined />}>
                  筛选
                </Button>
                <Button onClick={() => {
                  setSearchText('');
                  setSelectedType('');
                  fetchTestSuites();
                }}>
                  重置
                </Button>
              </Space>
            </Col>
          </Row>
        </div>

        <Divider />

        <Tabs defaultActiveKey="list">
          <TabPane tab="套件列表" key="list">
            <Table
              columns={columns}
              dataSource={filteredTestSuites}
              rowKey="id"
              loading={loading}
              pagination={{
                pageSize: 10,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total: number) => `共 ${total} 个测试套件`,
              }}
              className="test-suite-table"
            />
          </TabPane>
        </Tabs>
      </Card>

      {/* 创建/编辑测试套件模态框 */}
      <Modal
        title={editingSuite ? '编辑测试套件' : '创建测试套件'}
        open={modalVisible}
        onCancel={handleModalClose}
        onOk={() => form.submit()}
        width={600}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          className="test-suite-form"
        >
          <Form.Item
            name="name"
            label="套件名称"
            rules={[
              { required: true, message: '请输入套件名称' },
              { max: 100, message: '套件名称不能超过100个字符' },
            ]}
          >
            <Input placeholder="请输入套件名称" />
          </Form.Item>

          <Form.Item
            name="description"
            label="描述"
            rules={[
              { max: 500, message: '描述不能超过500个字符' },
            ]}
          >
            <TextArea
              rows={4}
              placeholder="请输入套件描述"
            />
          </Form.Item>

          <Form.Item
            name="type"
            label="套件类型"
            rules={[{ required: true, message: '请选择套件类型' }]}
          >
            <Select placeholder="请选择套件类型">
              <Option value="API">API</Option>
              <Option value="UI">UI</Option>
              <Option value="BUSINESS">业务</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="testCases"
            label="测试用例"
            tooltip="选择要包含在此套件中的测试用例"
          >
            <Select
              mode="multiple"
              placeholder="请选择测试用例"
              style={{ width: '100%' }}
              loading={loading}
              showSearch
              filterOption={(input, option) => {
                // 直接基于测试用例数据搜索，避免复杂的类型处理
                const testCaseId = option?.value as string;
                const testCase = testCases.find(tc => tc.id === testCaseId);
                if (testCase) {
                  const searchText = `${testCase.name} ${testCase.priority}`.toLowerCase();
                  return searchText.includes(input.toLowerCase());
                }
                return false;
              }}
            >
              {testCases.map((testCase) => (
                <Option key={testCase.id} value={testCase.id}>
                  {testCase.name} - {testCase.priority}
                </Option>
              ))}
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default TestSuiteManagement;