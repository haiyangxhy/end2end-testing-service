import React, { useState, useEffect } from 'react';
import {
  Form,
  Input,
  Select,
  Button,
  Card,
  Row,
  Col,
  Collapse,
  Space,
  InputNumber,
  Switch,
  Divider,
  Typography,
  message,
  Tooltip,
  Tag,
  Modal,
} from 'antd';
import {
  PlusOutlined,
  DeleteOutlined,
  InfoCircleOutlined,
  CodeOutlined,
  SettingOutlined,
} from '@ant-design/icons';
import { TestCaseConfig, TestAssertion } from '../../types';
import './TestCaseConfigForm.css';

const { Option } = Select;
const { TextArea } = Input;
const { Panel } = Collapse;
const { Title, Text } = Typography;

interface TestCaseConfigFormProps {
  value?: TestCaseConfig;
  onChange?: (value: TestCaseConfig) => void;
  disabled?: boolean;
}

const TestCaseConfigForm: React.FC<TestCaseConfigFormProps> = ({
  value,
  onChange,
  disabled = false,
}) => {
  const [form] = Form.useForm();
  const [config, setConfig] = useState<TestCaseConfig>(value || getDefaultConfig());
  const [showJsonEditor, setShowJsonEditor] = useState(false);
  const [jsonValue, setJsonValue] = useState('');

  // 默认配置
  function getDefaultConfig(): TestCaseConfig {
    return {
      method: 'GET',
      endpoint: '',
      headers: {},
      params: {},
      body: null,
      assertions: [],
      extract: {},
      timeout: 30000,
      retries: 0,
    };
  }

  // 初始化表单
  useEffect(() => {
    if (value) {
      setConfig(value);
      form.setFieldsValue(value);
    }
  }, [value, form]);

  // 处理配置变化
  const handleConfigChange = (changedValues: any, allValues: any) => {
    const newConfig = { ...config, ...allValues };
    setConfig(newConfig);
    onChange?.(newConfig);
  };

  // 添加请求头
  const addHeader = () => {
    const newHeaders = { ...config.headers, '': '' };
    const newConfig = { ...config, headers: newHeaders };
    setConfig(newConfig);
    onChange?.(newConfig);
    form.setFieldsValue({ headers: newHeaders });
  };

  // 删除请求头
  const removeHeader = (key: string) => {
    const newHeaders = { ...config.headers };
    delete newHeaders[key];
    const newConfig = { ...config, headers: newHeaders };
    setConfig(newConfig);
    onChange?.(newConfig);
    form.setFieldsValue({ headers: newHeaders });
  };

  // 更新请求头
  const updateHeader = (oldKey: string, newKey: string, newValue: string) => {
    const newHeaders = { ...config.headers };
    if (oldKey !== newKey) {
      delete newHeaders[oldKey];
    }
    newHeaders[newKey] = newValue;
    const newConfig = { ...config, headers: newHeaders };
    setConfig(newConfig);
    onChange?.(newConfig);
    form.setFieldsValue({ headers: newHeaders });
  };

  // 添加参数
  const addParam = () => {
    const newParams = { ...config.params, '': '' };
    const newConfig = { ...config, params: newParams };
    setConfig(newConfig);
    onChange?.(newConfig);
    form.setFieldsValue({ params: newParams });
  };

  // 删除参数
  const removeParam = (key: string) => {
    const newParams = { ...config.params };
    delete newParams[key];
    const newConfig = { ...config, params: newParams };
    setConfig(newConfig);
    onChange?.(newConfig);
    form.setFieldsValue({ params: newParams });
  };

  // 更新参数
  const updateParam = (oldKey: string, newKey: string, newValue: any) => {
    const newParams = { ...config.params };
    if (oldKey !== newKey) {
      delete newParams[oldKey];
    }
    newParams[newKey] = newValue;
    const newConfig = { ...config, params: newParams };
    setConfig(newConfig);
    onChange?.(newConfig);
    form.setFieldsValue({ params: newParams });
  };

  // 添加断言
  const addAssertion = () => {
    const newAssertion: TestAssertion = {
      type: 'statusCode',
      expected: 200,
      operator: 'EQUALS',
    };
    const newAssertions = [...(config.assertions || []), newAssertion];
    const newConfig = { ...config, assertions: newAssertions };
    setConfig(newConfig);
    onChange?.(newConfig);
    form.setFieldsValue({ assertions: newAssertions });
  };

  // 删除断言
  const removeAssertion = (index: number) => {
    const newAssertions = [...(config.assertions || [])];
    newAssertions.splice(index, 1);
    const newConfig = { ...config, assertions: newAssertions };
    setConfig(newConfig);
    onChange?.(newConfig);
    form.setFieldsValue({ assertions: newAssertions });
  };

  // 更新断言
  const updateAssertion = (index: number, field: keyof TestAssertion, value: any) => {
    const newAssertions = [...(config.assertions || [])];
    newAssertions[index] = { ...newAssertions[index], [field]: value };
    const newConfig = { ...config, assertions: newAssertions };
    setConfig(newConfig);
    onChange?.(newConfig);
    form.setFieldsValue({ assertions: newAssertions });
  };

  // 添加提取规则
  const addExtract = () => {
    const newExtract = { ...config.extract, '': '' };
    const newConfig = { ...config, extract: newExtract };
    setConfig(newConfig);
    onChange?.(newConfig);
    form.setFieldsValue({ extract: newExtract });
  };

  // 删除提取规则
  const removeExtract = (key: string) => {
    const newExtract = { ...config.extract };
    delete newExtract[key];
    const newConfig = { ...config, extract: newExtract };
    setConfig(newConfig);
    onChange?.(newConfig);
    form.setFieldsValue({ extract: newExtract });
  };

  // 更新提取规则
  const updateExtract = (oldKey: string, newKey: string, newValue: string) => {
    const newExtract = { ...config.extract };
    if (oldKey !== newKey) {
      delete newExtract[oldKey];
    }
    newExtract[newKey] = newValue;
    const newConfig = { ...config, extract: newExtract };
    setConfig(newConfig);
    onChange?.(newConfig);
    form.setFieldsValue({ extract: newExtract });
  };

  // 打开JSON编辑器
  const openJsonEditor = () => {
    setJsonValue(JSON.stringify(config, null, 2));
    setShowJsonEditor(true);
  };

  // 应用JSON配置
  const applyJsonConfig = () => {
    try {
      const parsedConfig = JSON.parse(jsonValue);
      setConfig(parsedConfig);
      onChange?.(parsedConfig);
      form.setFieldsValue(parsedConfig);
      setShowJsonEditor(false);
      message.success('配置已应用');
    } catch (error) {
      message.error('JSON格式错误');
    }
  };

  // 渲染键值对编辑器
  const renderKeyValueEditor = (
    items: Record<string, any>,
    onAdd: () => void,
    onRemove: (key: string) => void,
    onUpdate: (oldKey: string, newKey: string, newValue: any) => void,
    keyPlaceholder: string,
    valuePlaceholder: string,
    valueType: 'text' | 'number' = 'text'
  ) => {
    return (
      <div className="key-value-editor">
        {Object.entries(items).map(([key, val], index) => (
          <Row key={index} gutter={8} style={{ marginBottom: 8 }}>
            <Col span={8}>
              <Input
                placeholder={keyPlaceholder}
                value={key}
                onChange={(e) => {
                  const newKey = e.target.value;
                  onUpdate(key, newKey, val);
                }}
                disabled={disabled}
              />
            </Col>
            <Col span={14}>
              {valueType === 'number' ? (
                <InputNumber
                  placeholder={valuePlaceholder}
                  value={val}
                  onChange={(value) => onUpdate(key, key, value)}
                  style={{ width: '100%' }}
                  disabled={disabled}
                />
              ) : (
                <Input
                  placeholder={valuePlaceholder}
                  value={val}
                  onChange={(e) => onUpdate(key, key, e.target.value)}
                  disabled={disabled}
                />
              )}
            </Col>
            <Col span={2}>
              <Button
                type="text"
                danger
                icon={<DeleteOutlined />}
                onClick={() => onRemove(key)}
                disabled={disabled}
              />
            </Col>
          </Row>
        ))}
        <Button
          type="dashed"
          icon={<PlusOutlined />}
          onClick={onAdd}
          disabled={disabled}
          style={{ width: '100%' }}
        >
          添加
        </Button>
      </div>
    );
  };

  return (
    <div className="test-case-config-form">
      <Form
        form={form}
        layout="vertical"
        onValuesChange={handleConfigChange}
        disabled={disabled}
      >
        <Card
          title={
            <Space>
              <SettingOutlined />
              <span>测试用例配置</span>
              <Button
                type="link"
                icon={<CodeOutlined />}
                onClick={openJsonEditor}
                disabled={disabled}
              >
                JSON编辑器
              </Button>
            </Space>
          }
          extra={
            <Space>
              <Text type="secondary">支持变量替换: {'${token}'}, {'${variableName}'}</Text>
            </Space>
          }
        >
          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                label="HTTP方法"
                name="method"
                rules={[{ required: true, message: '请选择HTTP方法' }]}
              >
                <Select>
                  <Option value="GET">GET</Option>
                  <Option value="POST">POST</Option>
                  <Option value="PUT">PUT</Option>
                  <Option value="DELETE">DELETE</Option>
                  <Option value="PATCH">PATCH</Option>
                </Select>
              </Form.Item>
            </Col>
            <Col span={16}>
              <Form.Item
                label="请求端点"
                name="endpoint"
                rules={[{ required: true, message: '请输入请求端点' }]}
              >
                <Input placeholder="/api/endpoint" />
              </Form.Item>
            </Col>
          </Row>

          <Collapse defaultActiveKey={['headers', 'params']}>
            <Panel header="请求头配置" key="headers">
              {renderKeyValueEditor(
                config.headers || {},
                addHeader,
                removeHeader,
                updateHeader,
                'Header名称',
                'Header值'
              )}
            </Panel>

            <Panel header="请求参数配置" key="params">
              {renderKeyValueEditor(
                config.params || {},
                addParam,
                removeParam,
                updateParam,
                '参数名',
                '参数值'
              )}
            </Panel>

            <Panel header="请求体配置" key="body">
              <Form.Item name="body">
                <TextArea
                  rows={6}
                  placeholder="JSON格式的请求体，支持变量替换"
                  style={{ fontFamily: 'monospace' }}
                />
              </Form.Item>
            </Panel>

            <Panel header="断言配置" key="assertions">
              <div className="assertions-editor">
                {(config.assertions || []).map((assertion, index) => (
                  <Card key={index} size="small" style={{ marginBottom: 8 }}>
                    <Row gutter={8}>
                      <Col span={6}>
                        <Select
                          value={assertion.type}
                          onChange={(value) => updateAssertion(index, 'type', value)}
                          disabled={disabled}
                        >
                          <Option value="statusCode">状态码</Option>
                          <Option value="contains">包含文本</Option>
                          <Option value="jsonPath">JSON路径</Option>
                          <Option value="responseTime">响应时间</Option>
                        </Select>
                      </Col>
                      <Col span={6}>
                        <Select
                          value={assertion.operator}
                          onChange={(value) => updateAssertion(index, 'operator', value)}
                          disabled={disabled}
                        >
                          <Option value="EQUALS">等于</Option>
                          <Option value="NOT_EQUALS">不等于</Option>
                          <Option value="CONTAINS">包含</Option>
                          <Option value="NOT_CONTAINS">不包含</Option>
                          <Option value="GREATER_THAN">大于</Option>
                          <Option value="LESS_THAN">小于</Option>
                        </Select>
                      </Col>
                      <Col span={8}>
                        <Input
                          placeholder="期望值"
                          value={assertion.expected}
                          onChange={(e) => updateAssertion(index, 'expected', e.target.value)}
                          disabled={disabled}
                        />
                      </Col>
                      <Col span={4}>
                        <Button
                          type="text"
                          danger
                          icon={<DeleteOutlined />}
                          onClick={() => removeAssertion(index)}
                          disabled={disabled}
                        />
                      </Col>
                    </Row>
                  </Card>
                ))}
                <Button
                  type="dashed"
                  icon={<PlusOutlined />}
                  onClick={addAssertion}
                  disabled={disabled}
                  style={{ width: '100%' }}
                >
                  添加断言
                </Button>
              </div>
            </Panel>

            <Panel header="数据提取配置" key="extract">
              {renderKeyValueEditor(
                config.extract || {},
                addExtract,
                removeExtract,
                updateExtract,
                '变量名',
                'JSONPath表达式'
              )}
              <Text type="secondary">
                使用JSONPath语法提取响应数据，如: $.data.id, $.items[0].name
              </Text>
            </Panel>

            <Panel header="高级配置" key="advanced">
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item label="超时时间(毫秒)" name="timeout">
                    <InputNumber
                      min={1000}
                      max={300000}
                      style={{ width: '100%' }}
                    />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item label="重试次数" name="retries">
                    <InputNumber
                      min={0}
                      max={5}
                      style={{ width: '100%' }}
                    />
                  </Form.Item>
                </Col>
              </Row>
            </Panel>
          </Collapse>
        </Card>
      </Form>

      {/* JSON编辑器模态框 */}
      <Modal
        title="JSON配置编辑器"
        open={showJsonEditor}
        onOk={applyJsonConfig}
        onCancel={() => setShowJsonEditor(false)}
        width={800}
        okText="应用"
        cancelText="取消"
      >
        <TextArea
          value={jsonValue}
          onChange={(e) => setJsonValue(e.target.value)}
          rows={20}
          style={{ fontFamily: 'monospace' }}
        />
      </Modal>
    </div>
  );
};

export default TestCaseConfigForm;
