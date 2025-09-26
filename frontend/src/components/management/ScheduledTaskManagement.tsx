import React, { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  Select,
  Switch,
  Space,
  Popconfirm,
  message,
  Card,
  Tag,
  Tooltip,
  Divider
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PlayCircleOutlined,
  PauseCircleOutlined,
  ClockCircleOutlined
} from '@ant-design/icons';
import { ScheduledTask, ScheduledTaskForm, TestSuite, TestEnvironment } from '../types';
import { scheduledTaskAPI } from '../services/api';
import './ScheduledTaskManagement.css';

const { Option } = Select;
const { TextArea } = Input;

const ScheduledTaskManagement: React.FC = () => {
  const [tasks, setTasks] = useState<ScheduledTask[]>([]);
  const [testSuites, setTestSuites] = useState<TestSuite[]>([]);
  const [testEnvironments, setTestEnvironments] = useState<TestEnvironment[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingTask, setEditingTask] = useState<ScheduledTask | null>(null);
  const [form] = Form.useForm();

  // 预定义的cron表达式选项
  const cronOptions = [
    { value: '0 0 2 * * ?', label: '每天凌晨2点' },
    { value: '0 0 */6 * * ?', label: '每6小时' },
    { value: '0 0 0 * * ?', label: '每天午夜' },
    { value: '0 0 */1 * * ?', label: '每小时' },
    { value: '0 */30 * * * ?', label: '每30分钟' },
    { value: '0 0 9 * * MON-FRI', label: '工作日早上9点' },
    { value: '0 0 18 * * MON-FRI', label: '工作日下午6点' }
  ];

  useEffect(() => {
    fetchTasks();
    fetchTestSuites();
    fetchTestEnvironments();
  }, []);

  const fetchTasks = async () => {
    try {
      setLoading(true);
      const response = await scheduledTaskAPI.getAll();
      setTasks(response.data);
    } catch (error) {
      message.error('获取定时任务列表失败');
      console.error('获取定时任务列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchTestSuites = async () => {
    try {
      const response = await scheduledTaskAPI.getTestSuites();
      setTestSuites(response.data);
    } catch (error) {
      console.error('获取测试套件列表失败:', error);
    }
  };

  const fetchTestEnvironments = async () => {
    try {
      const response = await scheduledTaskAPI.getTestEnvironments();
      setTestEnvironments(response.data);
    } catch (error) {
      console.error('获取测试环境列表失败:', error);
    }
  };

  const handleCreate = () => {
    setEditingTask(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (task: ScheduledTask) => {
    setEditingTask(task);
    form.setFieldsValue({
      name: task.name,
      description: task.description,
      suiteId: task.suiteId,
      environmentId: task.environmentId,
      cronExpression: task.cronExpression,
      isActive: task.isActive
    });
    setModalVisible(true);
  };

  const handleSubmit = async (values: ScheduledTaskForm) => {
    try {
      if (editingTask) {
        await scheduledTaskAPI.update(editingTask.id, values);
        message.success('定时任务更新成功');
      } else {
        await scheduledTaskAPI.create(values);
        message.success('定时任务创建成功');
      }
      setModalVisible(false);
      form.resetFields();
      setEditingTask(null);
      fetchTasks();
    } catch (error) {
      message.error(editingTask ? '定时任务更新失败' : '定时任务创建失败');
      console.error('定时任务操作失败:', error);
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await scheduledTaskAPI.delete(id);
      message.success('定时任务删除成功');
      fetchTasks();
    } catch (error) {
      message.error('定时任务删除失败');
      console.error('删除定时任务失败:', error);
    }
  };

  const handleToggle = async (id: string, isActive: boolean) => {
    try {
      await scheduledTaskAPI.toggle(id, isActive);
      message.success(`定时任务已${isActive ? '启用' : '禁用'}`);
      fetchTasks();
    } catch (error) {
      message.error('切换定时任务状态失败');
      console.error('切换定时任务状态失败:', error);
    }
  };

  const getCronLabel = (cronExpression: string) => {
    const option = cronOptions.find(opt => opt.value === cronExpression);
    return option ? option.label : cronExpression;
  };

  const getStatusTag = (isActive: boolean) => {
    return isActive ? (
      <Tag color="green">启用</Tag>
    ) : (
      <Tag color="red">禁用</Tag>
    );
  };

  const formatDateTime = (dateTime?: string) => {
    if (!dateTime) return '-';
    return new Date(dateTime).toLocaleString('zh-CN');
  };

  const columns = [
    {
      title: '任务名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
      ellipsis: true
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      width: 200,
      ellipsis: true,
      render: (text: string) => text || '-'
    },
    {
      title: '测试套件',
      dataIndex: 'suiteId',
      key: 'suiteId',
      width: 150,
      render: (suiteId: string) => {
        const suite = testSuites.find(s => s.id === suiteId);
        return suite ? suite.name : suiteId;
      }
    },
    {
      title: '测试环境',
      dataIndex: 'environmentId',
      key: 'environmentId',
      width: 150,
      render: (environmentId: string) => {
        const env = testEnvironments.find(e => e.id === environmentId);
        return env ? env.name : environmentId;
      }
    },
    {
      title: '执行计划',
      dataIndex: 'cronExpression',
      key: 'cronExpression',
      width: 150,
      render: (cronExpression: string) => (
        <Tooltip title={cronExpression}>
          {getCronLabel(cronExpression)}
        </Tooltip>
      )
    },
    {
      title: '状态',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 80,
      render: (isActive: boolean) => getStatusTag(isActive)
    },
    {
      title: '最后执行',
      dataIndex: 'lastRun',
      key: 'lastRun',
      width: 150,
      render: (lastRun: string) => formatDateTime(lastRun)
    },
    {
      title: '下次执行',
      dataIndex: 'nextRun',
      key: 'nextRun',
      width: 150,
      render: (nextRun: string) => formatDateTime(nextRun)
    },
    {
      title: '操作',
      key: 'actions',
      width: 200,
      render: (_: any, record: ScheduledTask) => (
        <Space size="small">
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            icon={record.isActive ? <PauseCircleOutlined /> : <PlayCircleOutlined />}
            onClick={() => handleToggle(record.id, !record.isActive)}
          >
            {record.isActive ? '禁用' : '启用'}
          </Button>
          <Popconfirm
            title="确定要删除这个定时任务吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              danger
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ];

  return (
    <div className="scheduled-task-management">
      <Card>
        <div className="header">
          <div className="title">
            <ClockCircleOutlined className="title-icon" />
            <span>定时任务管理</span>
          </div>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleCreate}
          >
            创建定时任务
          </Button>
        </div>

        <Divider />

        <Table
          columns={columns}
          dataSource={tasks}
          rowKey="id"
          loading={loading}
          pagination={{
            pageSize: 10,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`
          }}
          scroll={{ x: 1200 }}
        />
      </Card>

      <Modal
        title={editingTask ? '编辑定时任务' : '创建定时任务'}
        open={modalVisible}
        onCancel={() => {
          setModalVisible(false);
          form.resetFields();
          setEditingTask(null);
        }}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Form.Item
            name="name"
            label="任务名称"
            rules={[{ required: true, message: '请输入任务名称' }]}
          >
            <Input placeholder="请输入任务名称" />
          </Form.Item>

          <Form.Item
            name="description"
            label="描述"
          >
            <TextArea
              placeholder="请输入任务描述"
              rows={3}
            />
          </Form.Item>

          <Form.Item
            name="suiteId"
            label="测试套件"
            rules={[{ required: true, message: '请选择测试套件' }]}
          >
            <Select
              placeholder="请选择测试套件"
              showSearch
              filterOption={(input, option) => {
                const children = option?.children as any;
                if (typeof children === 'string') {
                  return children.toLowerCase().includes(input.toLowerCase());
                }
                return false;
              }}
            >
              {testSuites.map((suite) => (
                <Option key={suite.id} value={suite.id}>
                  {suite.name}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="environmentId"
            label="测试环境"
            rules={[{ required: true, message: '请选择测试环境' }]}
          >
            <Select
              placeholder="请选择测试环境"
              showSearch
              filterOption={(input, option) => {
                const children = option?.children as any;
                if (typeof children === 'string') {
                  return children.toLowerCase().includes(input.toLowerCase());
                }
                return false;
              }}
            >
              {testEnvironments.map((env) => (
                <Option key={env.id} value={env.id}>
                  {env.name}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="cronExpression"
            label="执行计划"
            rules={[{ required: true, message: '请选择执行计划' }]}
          >
            <Select
              placeholder="请选择执行计划"
              showSearch
              filterOption={(input, option) => {
                const children = option?.children as any;
                if (typeof children === 'string') {
                  return children.toLowerCase().includes(input.toLowerCase());
                }
                return false;
              }}
            >
              {cronOptions.map((option) => (
                <Option key={option.value} value={option.value}>
                  {option.label}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            name="isActive"
            label="状态"
            valuePropName="checked"
          >
            <Switch
              checkedChildren="启用"
              unCheckedChildren="禁用"
            />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit">
                {editingTask ? '更新' : '创建'}
              </Button>
              <Button onClick={() => {
                setModalVisible(false);
                form.resetFields();
                setEditingTask(null);
              }}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ScheduledTaskManagement;
