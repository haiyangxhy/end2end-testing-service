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
  Switch,
  message,
  Popconfirm,
  Tag,
  Tooltip,
  Typography,
  Row,
  Col,
  Divider,
  Tabs,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CopyOutlined,
  SearchOutlined,
  FilterOutlined,
  KeyOutlined,
  EnvironmentOutlined,
  LockOutlined,
  UnlockOutlined,
} from '@ant-design/icons';
import { variableAPI, environmentAPI } from '../services/api';
import { GlobalVariable, VariableForm, TestEnvironment } from '../types';
import './VariableManagement.css';

const { Title, Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;
const { TabPane } = Tabs;

const VariableManagement: React.FC = () => {
  const [variables, setVariables] = useState<GlobalVariable[]>([]);
  const [environments, setEnvironments] = useState<TestEnvironment[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingVariable, setEditingVariable] = useState<GlobalVariable | null>(null);
  const [selectedEnvironment, setSelectedEnvironment] = useState<string>('');
  const [searchText, setSearchText] = useState('');
  const [form] = Form.useForm();

  // 获取变量列表
  const fetchVariables = async (environmentId?: string) => {
    setLoading(true);
    try {
      const response = await variableAPI.getAll(environmentId);
      setVariables(response.data);
    } catch (error) {
      message.error('获取变量列表失败');
      console.error('获取变量列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 获取环境列表
  const fetchEnvironments = async () => {
    try {
      const response = await environmentAPI.getAll();
      setEnvironments(response.data);
    } catch (error) {
      console.error('获取环境列表失败:', error);
    }
  };

  // 初始化数据
  useEffect(() => {
    fetchEnvironments();
    fetchVariables();
  }, []);

  // 处理环境切换
  const handleEnvironmentChange = (environmentId: string) => {
    setSelectedEnvironment(environmentId);
    fetchVariables(environmentId);
  };

  // 处理创建/编辑变量
  const handleSubmit = async (values: VariableForm) => {
    try {
      if (editingVariable) {
        await variableAPI.update(editingVariable.id, values);
        message.success('变量更新成功');
      } else {
        await variableAPI.create(values);
        message.success('变量创建成功');
      }
      setModalVisible(false);
      form.resetFields();
      setEditingVariable(null);
      fetchVariables(selectedEnvironment);
    } catch (error) {
      message.error(editingVariable ? '变量更新失败' : '变量创建失败');
      console.error('变量操作失败:', error);
    }
  };

  // 处理删除变量
  const handleDelete = async (id: string) => {
    try {
      await variableAPI.delete(id);
      message.success('变量删除成功');
      fetchVariables(selectedEnvironment);
    } catch (error) {
      message.error('变量删除失败');
      console.error('变量删除失败:', error);
    }
  };

  // 处理复制变量
  const handleCopy = async (variableId: string, targetEnvironmentId: string) => {
    try {
      await variableAPI.copy({ variableId, targetEnvironmentId });
      message.success('变量复制成功');
      fetchVariables(selectedEnvironment);
    } catch (error) {
      message.error('变量复制失败');
      console.error('变量复制失败:', error);
    }
  };

  // 处理变量替换测试
  const handleReplaceTest = async (text: string) => {
    try {
      const response = await variableAPI.replace({
        text,
        environmentId: selectedEnvironment,
      });
      message.success('变量替换测试成功');
      console.log('替换结果:', response.data);
    } catch (error) {
      message.error('变量替换测试失败');
      console.error('变量替换测试失败:', error);
    }
  };

  // 打开编辑模态框
  const openEditModal = (variable: GlobalVariable) => {
    setEditingVariable(variable);
    form.setFieldsValue({
      name: variable.name,
      value: variable.value,
      description: variable.description,
      environmentId: variable.environmentId,
      variableType: variable.variableType,
      isEncrypted: variable.isEncrypted,
    });
    setModalVisible(true);
  };

  // 打开创建模态框
  const openCreateModal = () => {
    setEditingVariable(null);
    form.setFieldsValue({
      environmentId: selectedEnvironment,
      variableType: 'STRING',
      isEncrypted: false,
    });
    setModalVisible(true);
  };

  // 关闭模态框
  const handleModalClose = () => {
    setModalVisible(false);
    form.resetFields();
    setEditingVariable(null);
  };

  // 过滤变量
  const filteredVariables = variables.filter(variable =>
    variable.name.toLowerCase().includes(searchText.toLowerCase()) ||
    variable.description?.toLowerCase().includes(searchText.toLowerCase())
  );

  // 表格列定义
  const columns = [
    {
      title: '变量名',
      dataIndex: 'name',
      key: 'name',
      render: (text: string, record: GlobalVariable) => (
        <Space>
          <KeyOutlined />
          <Text strong={record.isEncrypted} code={record.isEncrypted}>
            {text}
          </Text>
          {record.isEncrypted && (
            <Tag color="red" icon={<LockOutlined />}>
              加密
            </Tag>
          )}
        </Space>
      ),
    },
    {
      title: '值',
      dataIndex: 'value',
      key: 'value',
      ellipsis: true,
      render: (text: string, record: GlobalVariable) => (
        <Tooltip title={record.isEncrypted ? '***加密值***' : text}>
          <Text type="secondary">
            {record.isEncrypted ? '***加密值***' : text}
          </Text>
        </Tooltip>
      ),
    },
    {
      title: '类型',
      dataIndex: 'variableType',
      key: 'variableType',
      render: (type: string) => {
        const typeColors = {
          STRING: 'blue',
          NUMBER: 'green',
          BOOLEAN: 'orange',
          JSON: 'purple',
        };
        return (
          <Tag color={typeColors[type as keyof typeof typeColors]}>
            {type}
          </Tag>
        );
      },
    },
    {
      title: '环境',
      dataIndex: 'environmentId',
      key: 'environmentId',
      render: (environmentId: string) => {
        const environment = environments.find(env => env.id === environmentId);
        return (
          <Space>
            <EnvironmentOutlined />
            <Text>{environment?.name || environmentId}</Text>
          </Space>
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
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (text: string) => new Date(text).toLocaleString(),
    },
    {
      title: '操作',
      key: 'actions',
      width: 200,
      render: (_: any, record: GlobalVariable) => (
        <Space>
          <Tooltip title="编辑">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => openEditModal(record)}
            />
          </Tooltip>
          <Tooltip title="复制到其他环境">
            <Button
              type="text"
              icon={<CopyOutlined />}
              onClick={() => {
                // 这里应该打开一个选择环境的模态框
                console.log('复制变量:', record.id);
              }}
            />
          </Tooltip>
          <Popconfirm
            title="确定要删除这个变量吗？"
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
    <div className="variable-management">
      <div className="page-header">
        <Title level={2}>
          <KeyOutlined /> 变量管理
        </Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={openCreateModal}
        >
          创建变量
        </Button>
      </div>

      <Card className="variable-card">
        <div className="variable-filters">
          <Row gutter={[16, 16]} align="middle">
            <Col xs={24} sm={12} md={8}>
              <Select
                placeholder="选择环境"
                value={selectedEnvironment}
                onChange={handleEnvironmentChange}
                style={{ width: '100%' }}
                allowClear
              >
                {environments.map(env => (
                  <Option key={env.id} value={env.id}>
                    <Space>
                      <EnvironmentOutlined />
                      {env.name}
                    </Space>
                  </Option>
                ))}
              </Select>
            </Col>
            <Col xs={24} sm={12} md={8}>
              <Input
                placeholder="搜索变量名或描述"
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
                  setSelectedEnvironment('');
                  fetchVariables();
                }}>
                  重置
                </Button>
              </Space>
            </Col>
          </Row>
        </div>

        <Divider />

        <div className="variable-stats">
          <Row gutter={[16, 16]}>
            <Col xs={24} sm={8}>
              <div className="stat-item">
                <Text type="secondary">总变量数</Text>
                <Title level={3} style={{ margin: 0 }}>
                  {variables.length}
                </Title>
              </div>
            </Col>
            <Col xs={24} sm={8}>
              <div className="stat-item">
                <Text type="secondary">加密变量</Text>
                <Title level={3} style={{ margin: 0, color: '#ff4d4f' }}>
                  {variables.filter(v => v.isEncrypted).length}
                </Title>
              </div>
            </Col>
            <Col xs={24} sm={8}>
              <div className="stat-item">
                <Text type="secondary">当前环境变量</Text>
                <Title level={3} style={{ margin: 0, color: '#1890ff' }}>
                  {variables.filter(v => v.environmentId === selectedEnvironment).length}
                </Title>
              </div>
            </Col>
          </Row>
        </div>

        <Divider />

        <Tabs defaultActiveKey="list">
          <TabPane tab="变量列表" key="list">
            <Table
              columns={columns}
              dataSource={filteredVariables}
              rowKey="id"
              loading={loading}
              pagination={{
                pageSize: 10,
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total: number) => `共 ${total} 个变量`,
              }}
              className="variable-table"
            />
          </TabPane>
          <TabPane tab="变量替换测试" key="replace">
            <div className="replace-test">
              <Form layout="vertical">
                <Form.Item label="测试文本">
                  <TextArea
                    rows={6}
                    placeholder="输入包含变量的文本，如：Hello ${username}，今天是 ${date}"
                  />
                </Form.Item>
                <Form.Item>
                  <Button
                    type="primary"
                    onClick={() => {
                      const text = (document.querySelector('.replace-test textarea') as HTMLTextAreaElement)?.value;
                      if (text) {
                        handleReplaceTest(text);
                      }
                    }}
                  >
                    测试替换
                  </Button>
                </Form.Item>
              </Form>
            </div>
          </TabPane>
        </Tabs>
      </Card>

      {/* 创建/编辑变量模态框 */}
      <Modal
        title={editingVariable ? '编辑变量' : '创建变量'}
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
          className="variable-form"
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="name"
                label="变量名"
                rules={[
                  { required: true, message: '请输入变量名' },
                  { max: 50, message: '变量名不能超过50个字符' },
                  { pattern: /^[a-zA-Z_][a-zA-Z0-9_]*$/, message: '变量名只能包含字母、数字和下划线，且不能以数字开头' },
                ]}
              >
                <Input placeholder="请输入变量名" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="variableType"
                label="变量类型"
                rules={[{ required: true, message: '请选择变量类型' }]}
              >
                <Select placeholder="请选择变量类型">
                  <Option value="STRING">字符串</Option>
                  <Option value="NUMBER">数字</Option>
                  <Option value="BOOLEAN">布尔值</Option>
                  <Option value="JSON">JSON</Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="value"
            label="变量值"
            rules={[
              { required: true, message: '请输入变量值' },
            ]}
          >
            <TextArea
              rows={4}
              placeholder="请输入变量值"
            />
          </Form.Item>

          <Form.Item
            name="description"
            label="描述"
            rules={[
              { max: 200, message: '描述不能超过200个字符' },
            ]}
          >
            <Input placeholder="请输入变量描述" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="environmentId"
                label="所属环境"
                rules={[{ required: true, message: '请选择所属环境' }]}
              >
                <Select placeholder="请选择所属环境">
                  {environments.map(env => (
                    <Option key={env.id} value={env.id}>
                      {env.name}
                    </Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="isEncrypted"
                label="是否加密"
                valuePropName="checked"
              >
                <Switch
                  checkedChildren={<LockOutlined />}
                  unCheckedChildren={<UnlockOutlined />}
                />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Modal>
    </div>
  );
};

export default VariableManagement;
