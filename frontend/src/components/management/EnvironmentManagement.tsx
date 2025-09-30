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
  Switch,
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  CheckCircleOutlined,
  EnvironmentOutlined,
} from '@ant-design/icons';
import { environmentAPI, getErrorMessage } from '../../services/api';
import { TestEnvironment, EnvironmentForm } from '../../types';
import './EnvironmentManagement.css';

const { Title, Text } = Typography;
const { Option } = Select;
const { TextArea } = Input;

const EnvironmentManagement: React.FC = () => {
  const [environments, setEnvironments] = useState<TestEnvironment[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingEnvironment, setEditingEnvironment] = useState<TestEnvironment | null>(null);
  const [form] = Form.useForm();

  // 获取环境列表
  const fetchEnvironments = async () => {
    setLoading(true);
    try {
      const response = await environmentAPI.getAll();
      setEnvironments(response.data);
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      message.error(errorMessage);
      console.error('获取环境列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 初始化数据
  useEffect(() => {
    fetchEnvironments();
  }, []);

  // 监听表单值变化
  const onValuesChange = (changedValues: any, allValues: any) => {
    if (changedValues.hasOwnProperty('isActive')) {
      console.log('DEBUG: Frontend - isActive changed:', changedValues.isActive);
      console.log('DEBUG: Frontend - All form values:', allValues);
    }
  };

  // 处理创建/编辑环境
  const handleSubmit = async (values: EnvironmentForm) => {
    try {
      console.log('DEBUG: Frontend - Form values:', values);
      console.log('DEBUG: Frontend - isActive value:', values.isActive);
      console.log('DEBUG: Frontend - isActive type:', typeof values.isActive);
      
      if (editingEnvironment) {
        await environmentAPI.update(editingEnvironment.id, values);
        message.success('环境更新成功');
      } else {
        await environmentAPI.create(values);
        message.success('环境创建成功');
      }
      setModalVisible(false);
      form.resetFields();
      setEditingEnvironment(null);
      fetchEnvironments();
    } catch (error: any) {
      const errorMessage = error.response?.data?.error || error.message || '操作失败';
      message.error(`${editingEnvironment ? '环境更新失败' : '环境创建失败'}: ${errorMessage}`);
      console.error('环境操作失败:', error);
    }
  };

  // 处理删除环境
  const handleDelete = async (id: string) => {
    try {
      await environmentAPI.delete(id);
      message.success('环境删除成功');
      fetchEnvironments();
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      message.error(errorMessage);
      console.error('环境删除失败:', error);
    }
  };

  // 处理激活环境
  const handleActivate = async (id: string) => {
    try {
      await environmentAPI.activate(id);
      message.success('环境激活成功');
      fetchEnvironments();
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      message.error(errorMessage);
      console.error('环境激活失败:', error);
    }
  };

  // 处理验证环境
  const handleValidate = async (id: string) => {
    try {
      const res = await environmentAPI.validate(id);
      if(res.data.valid){
        message.success('环境验证成功');
      }else{
        const errorMessage = getErrorMessage(res);
        message.error(errorMessage);
      }
    } catch (error) {
      const errorMessage = getErrorMessage(error);
      message.error(errorMessage);
      console.error('环境验证失败:', error);
    }
  };

  // 打开编辑模态框
  const openEditModal = (environment: TestEnvironment) => {
    setEditingEnvironment(environment);
    setModalVisible(true);
    
    // 在下一个事件循环中设置表单值，确保Modal已经渲染
    setTimeout(() => {
      const formValues = {
        name: environment.name,
        description: environment.description,
        apiBaseUrl: environment.apiBaseUrl,
        uiBaseUrl: environment.uiBaseUrl,
        databaseConfig: environment.databaseConfig,
        authConfig: environment.authConfig,
        isActive: environment.isActive || false, // 确保是布尔值
      };
      console.log('DEBUG: Frontend - Setting form values:', formValues);
      console.log('DEBUG: Frontend - Original environment.isActive:', environment.isActive);
      console.log('DEBUG: Frontend - Form isActive value:', formValues.isActive);
      form.setFieldsValue(formValues);
    }, 0);
  };

  // 打开创建模态框
  const openCreateModal = () => {
    setEditingEnvironment(null);
    form.resetFields();
    setModalVisible(true);
    
    // 设置默认值，确保isActive有默认值
    setTimeout(() => {
      form.setFieldsValue({
        isActive: false // 创建时默认为非活跃状态
      });
    }, 0);
  };

  // 关闭模态框
  const handleModalClose = () => {
    setModalVisible(false);
    form.resetFields();
    setEditingEnvironment(null);
  };

  // 表格列定义
  const columns = [
    {
      title: '环境名称',
      dataIndex: 'name',
      key: 'name',
      render: (text: string, record: TestEnvironment) => (
        <Space>
          <EnvironmentOutlined />
          <Text strong={record.isActive}>{text}</Text>
          {record.isActive && (
            <Tag color="green" icon={<CheckCircleOutlined />}>
              活跃
            </Tag>
          )}
        </Space>
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
      title: 'API地址',
      dataIndex: 'apiBaseUrl',
      key: 'apiBaseUrl',
      ellipsis: true,
      render: (text: string) => (
        <Tooltip title={text}>
          <Text code>{text || '-'}</Text>
        </Tooltip>
      ),
    },
    {
      title: 'UI地址',
      dataIndex: 'uiBaseUrl',
      key: 'uiBaseUrl',
      ellipsis: true,
      render: (text: string) => (
        <Tooltip title={text}>
          <Text code>{text || '-'}</Text>
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
      render: (_: any, record: TestEnvironment) => (
        <Space>
          <Tooltip title="编辑">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => openEditModal(record)}
            />
          </Tooltip>
          <Tooltip title="验证">
            <Button
              type="text"
              icon={<CheckCircleOutlined />}
              onClick={() => handleValidate(record.id)}
            />
          </Tooltip>
          {!record.isActive && (
            <Tooltip title="激活">
              <Button
                type="text"
                icon={<PlayCircleOutlined />}
                onClick={() => handleActivate(record.id)}
              />
            </Tooltip>
          )}
          <Popconfirm
            title="确定要删除这个环境吗？"
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
    <div className="environment-management">
      <div className="page-header">
        <Title level={2}>
          <EnvironmentOutlined /> 环境管理
        </Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={openCreateModal}
        >
          创建环境
        </Button>
      </div>

      <Card className="environment-card">
        <div className="environment-stats">
          <Row gutter={[16, 16]}>
            <Col xs={24} sm={8}>
              <div className="stat-item">
                <Text type="secondary">总环境数</Text>
                <Title level={3} style={{ margin: 0 }}>
                  {environments.length}
                </Title>
              </div>
            </Col>
            <Col xs={24} sm={8}>
              <div className="stat-item">
                <Text type="secondary">活跃环境</Text>
                <Title level={3} style={{ margin: 0, color: '#52c41a' }}>
                  {environments.filter(env => env.isActive).length}
                </Title>
              </div>
            </Col>
            <Col xs={24} sm={8}>
              <div className="stat-item">
                <Text type="secondary">非活跃环境</Text>
                <Title level={3} style={{ margin: 0, color: '#faad14' }}>
                  {environments.filter(env => !env.isActive).length}
                </Title>
              </div>
            </Col>
          </Row>
        </div>

        <Divider />

        <Table
          columns={columns}
          dataSource={environments}
          rowKey="id"
          loading={loading}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total: number) => `共 ${total} 个环境`,
          }}
          className="environment-table"
        />
      </Card>

      {/* 创建/编辑环境模态框 */}
      <Modal
        title={editingEnvironment ? '编辑环境' : '创建环境'}
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
          onValuesChange={onValuesChange}
          className="environment-form"
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="name"
                label="环境名称"
                rules={[
                  { required: true, message: '请输入环境名称' },
                  { max: 50, message: '环境名称不能超过50个字符' },
                ]}
              >
                <Input placeholder="请输入环境名称" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="description"
                label="描述"
                rules={[
                  { max: 200, message: '描述不能超过200个字符' },
                ]}
              >
                <Input placeholder="请输入环境描述" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="apiBaseUrl"
                label="API基础地址"
                rules={[
                  { type: 'url', message: '请输入有效的URL' },
                ]}
              >
                <Input placeholder="https://api.example.com" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="uiBaseUrl"
                label="UI基础地址"
                rules={[
                  { type: 'url', message: '请输入有效的URL' },
                ]}
              >
                <Input placeholder="https://ui.example.com" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="databaseConfig"
            label="数据库配置"
          >
            <TextArea
              rows={4}
              placeholder="请输入数据库配置（JSON格式）"
            />
          </Form.Item>

          <Form.Item
            name="authConfig"
            label="认证配置"
          >
            <TextArea
              rows={4}
              placeholder="请输入认证配置（JSON格式）"
            />
          </Form.Item>

          <Form.Item
            name="isActive"
            label="设为活跃环境"
            valuePropName="checked"
            getValueFromEvent={(checked) => {
              console.log('DEBUG: Frontend - Switch onChange:', checked);
              return checked;
            }}
          >
            <Switch 
              checkedChildren="是" 
              unCheckedChildren="否"
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default EnvironmentManagement;
