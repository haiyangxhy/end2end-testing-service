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
  Table,
  Tag,
  Tooltip,
  InputNumber,
  Switch,
  Modal,
  Select,
  Input,
  Empty,
  Popconfirm,
} from 'antd';
import {
  EditOutlined,
  DeleteOutlined,
  PlusOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  SettingOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import { testCaseAPI, testSuiteCaseAPI } from '../../services/api';
import { TestCase, TestSuite, TestSuiteCase } from '../../types';
import './TestSuiteCaseManagement.css';

const { Title, Text } = Typography;
const { Option } = Select;

// 使用从 types 导入的 TestSuiteCase 接口

interface TestSuiteCaseManagementProps {
  testSuite: TestSuite;
  onClose: () => void;
  onSave: (testSuiteCases: TestSuiteCase[]) => void;
  onRefresh?: () => void;
}

const TestSuiteCaseManagement: React.FC<TestSuiteCaseManagementProps> = ({
  testSuite,
  onClose,
  onSave,
  onRefresh,
}) => {
  const [testSuiteCases, setTestSuiteCases] = useState<TestSuiteCase[]>([]);
  const [availableTestCases, setAvailableTestCases] = useState<TestCase[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [selectedTestCase, setSelectedTestCase] = useState<string>('');
  const [searchText, setSearchText] = useState('');

  // 获取可用的测试用例
  const fetchAvailableTestCases = async () => {
    try {
      const response = await testCaseAPI.getAll();
      setAvailableTestCases(response.data);
    } catch (error) {
      message.error('获取测试用例列表失败');
      console.error('获取测试用例列表失败:', error);
    }
  };

  // 获取测试套件关联的测试用例
  const fetchTestSuiteCases = async () => {
    try {
      setLoading(true);
      const response = await testSuiteCaseAPI.getBySuiteIdWithDetails(testSuite.id);
      setTestSuiteCases(response.data);
    } catch (error) {
      message.error('获取测试套件用例失败');
      console.error('获取测试套件用例失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 初始化数据
  useEffect(() => {
    fetchAvailableTestCases();
    fetchTestSuiteCases();
  }, [testSuite]);

  // 添加测试用例到套件
  const handleAddTestCase = async () => {
    if (!selectedTestCase) {
      message.warning('请选择要添加的测试用例');
      return;
    }

    const testCase = availableTestCases.find(tc => tc.id === selectedTestCase);
    if (!testCase) {
      message.error('测试用例不存在');
      return;
    }

    // 检查是否已经添加
    if (testSuiteCases.some(tsc => tsc.testCaseId === selectedTestCase)) {
      message.warning('该测试用例已经添加到套件中');
      return;
    }

    try {
      setLoading(true);
      const response = await testSuiteCaseAPI.addTestCaseToSuite({
        suiteId: testSuite.id,
        testCaseId: selectedTestCase
      });
      
      // 重新获取测试套件用例列表
      await fetchTestSuiteCases();
      
      // 通知父组件刷新数据
      onRefresh?.();
      
      setSelectedTestCase('');
      setModalVisible(false);
      message.success('测试用例添加成功');
    } catch (error) {
      message.error('添加测试用例失败');
      console.error('添加测试用例失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 删除测试用例
  const handleDeleteTestCase = async (id: string) => {
    try {
      setLoading(true);
      await testSuiteCaseAPI.removeFromSuite(id);
      
      // 重新获取测试套件用例列表
      await fetchTestSuiteCases();
      
      // 通知父组件刷新数据
      onRefresh?.();
      
      message.success('测试用例删除成功');
    } catch (error) {
      message.error('删除测试用例失败');
      console.error('删除测试用例失败:', error);
    } finally {
      setLoading(false);
    }
  };

  // 更新执行顺序
  const handleUpdateOrder = async (id: string, newOrder: number) => {
    try {
      await testSuiteCaseAPI.updateExecutionOrder(id, newOrder);
      
      // 重新获取测试套件用例列表
      await fetchTestSuiteCases();
    } catch (error) {
      message.error('更新执行顺序失败');
      console.error('更新执行顺序失败:', error);
    }
  };

  // 上移测试用例
  const handleMoveUp = async (index: number) => {
    if (index > 0) {
      const currentCase = testSuiteCases[index];
      const previousCase = testSuiteCases[index - 1];
      
      try {
        // 交换执行顺序
        await Promise.all([
          testSuiteCaseAPI.updateExecutionOrder(currentCase.id, previousCase.executionOrder),
          testSuiteCaseAPI.updateExecutionOrder(previousCase.id, currentCase.executionOrder)
        ]);
        
        // 重新获取测试套件用例列表
        await fetchTestSuiteCases();
      } catch (error) {
        message.error('移动测试用例失败');
        console.error('移动测试用例失败:', error);
      }
    }
  };

  // 下移测试用例
  const handleMoveDown = async (index: number) => {
    if (index < testSuiteCases.length - 1) {
      const currentCase = testSuiteCases[index];
      const nextCase = testSuiteCases[index + 1];
      
      try {
        // 交换执行顺序
        await Promise.all([
          testSuiteCaseAPI.updateExecutionOrder(currentCase.id, nextCase.executionOrder),
          testSuiteCaseAPI.updateExecutionOrder(nextCase.id, currentCase.executionOrder)
        ]);
        
        // 重新获取测试套件用例列表
        await fetchTestSuiteCases();
      } catch (error) {
        message.error('移动测试用例失败');
        console.error('移动测试用例失败:', error);
      }
    }
  };

  // 切换启用状态
  const handleToggleEnabled = async (id: string) => {
    try {
      await testSuiteCaseAPI.toggleEnabled(id);
      
      // 重新获取测试套件用例列表
      await fetchTestSuiteCases();
    } catch (error) {
      message.error('切换启用状态失败');
      console.error('切换启用状态失败:', error);
    }
  };

  // 获取优先级颜色
  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'CRITICAL': return 'red';
      case 'HIGH': return 'orange';
      case 'MEDIUM': return 'blue';
      case 'LOW': return 'green';
      default: return 'default';
    }
  };

  // 移除getTypeIcon函数，测试用例不再有类型字段

  // 过滤可用的测试用例
  const filteredTestCases = availableTestCases.filter(tc => 
    tc.name.toLowerCase().includes(searchText.toLowerCase()) &&
    !testSuiteCases.some(tsc => tsc.testCaseId === tc.id)
  );

  // 表格列定义
  const columns = [
    {
      title: '执行顺序',
      dataIndex: 'executionOrder',
      key: 'executionOrder',
      width: 100,
      render: (order: number, record: TestSuiteCase, index: number) => (
        <Space>
          <InputNumber
            min={1}
            max={testSuiteCases.length}
            value={order}
            onChange={(value) => handleUpdateOrder(record.id, value || 1)}
            style={{ width: 60 }}
          />
          <Space direction="vertical" size="small">
            <Button
              type="text"
              size="small"
              icon={<ArrowUpOutlined />}
              onClick={() => handleMoveUp(index)}
              disabled={index === 0}
            />
            <Button
              type="text"
              size="small"
              icon={<ArrowDownOutlined />}
              onClick={() => handleMoveDown(index)}
              disabled={index === testSuiteCases.length - 1}
            />
          </Space>
        </Space>
      ),
    },
    {
      title: '测试用例',
      dataIndex: 'testCase',
      key: 'testCase',
      render: (testCase: TestCase) => (
        <Space>
          <Text strong>{testCase.name}</Text>
          <Tag color={getPriorityColor(testCase.priority)}>
            {testCase.priority}
          </Tag>
        </Space>
      ),
    },
    // 移除类型列，测试用例不再有类型字段
    {
      title: '优先级',
      dataIndex: ['testCase', 'priority'],
      key: 'priority',
      render: (priority: string) => (
        <Tag color={getPriorityColor(priority)}>
          {priority}
        </Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'isEnabled',
      key: 'isEnabled',
      render: (isEnabled: boolean, record: TestSuiteCase) => (
        <Switch
          checked={isEnabled}
          onChange={() => handleToggleEnabled(record.id)}
        />
      ),
    },
    {
      title: '操作',
      key: 'actions',
      width: 100,
      render: (_: any, record: TestSuiteCase) => (
        <Popconfirm
          title="确定要删除这个测试用例吗？"
          onConfirm={() => handleDeleteTestCase(record.id)}
          okText="确定"
          cancelText="取消"
        >
          <Button
            type="text"
            danger
            icon={<DeleteOutlined />}
          />
        </Popconfirm>
      ),
    },
  ];

  return (
    <div className="test-suite-case-management">
      <div className="page-header">
        <Title level={3}>
          <SettingOutlined /> 管理测试用例 - {testSuite.name}
        </Title>
        <Space>
          <Button onClick={onClose}>
            返回
          </Button>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setModalVisible(true)}
          >
            添加测试用例
          </Button>
        </Space>
      </div>

      <Card className="test-suite-case-card">
        <div className="execution-order-info">
          <Text type="secondary">
            测试用例将按照以下顺序执行：1. 优先级（CRITICAL → HIGH → MEDIUM → LOW）2. 执行顺序（数字越小越先执行）3. 创建时间
          </Text>
        </div>

        <Divider />

        <Table
          columns={columns}
          dataSource={testSuiteCases}
          rowKey="id"
          pagination={false}
          loading={loading}
          className="test-suite-case-table"
          locale={{ emptyText: <Empty description="暂无测试用例" /> }}
        />
      </Card>

      {/* 添加测试用例模态框 */}
      <Modal
        title="添加测试用例"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        onOk={handleAddTestCase}
        confirmLoading={loading}
        width={600}
      >
        <div className="add-test-case-form">
          <Input
            placeholder="搜索测试用例"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            style={{ marginBottom: 16 }}
          />
          
          <Select
            placeholder="选择测试用例"
            value={selectedTestCase}
            onChange={setSelectedTestCase}
            style={{ width: '100%' }}
            showSearch
            optionFilterProp="children"
          >
            {filteredTestCases.map(tc => (
              <Option key={tc.id} value={tc.id}>
                <Space>
                  <Text>{tc.name}</Text>
                  <Tag color={getPriorityColor(tc.priority)}>
                    {tc.priority}
                  </Tag>
                </Space>
              </Option>
            ))}
          </Select>
        </div>
      </Modal>
    </div>
  );
};

export default TestSuiteCaseManagement;
