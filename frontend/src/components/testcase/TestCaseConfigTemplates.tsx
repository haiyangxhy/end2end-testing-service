import React, { useState } from 'react';
import {
  Card,
  Tabs,
  Typography,
  Button,
  Space,
  Tag,
  Divider,
  Alert,
  Collapse,
} from 'antd';
import {
  CopyOutlined,
  InfoCircleOutlined,
  ApiOutlined,
  DatabaseOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import { TestCaseConfig } from '../../types';
import './TestCaseConfigTemplates.css';

const { Title, Text, Paragraph } = Typography;
const { TabPane } = Tabs;
const { Panel } = Collapse;

interface TestCaseConfigTemplatesProps {
  onSelectTemplate?: (config: TestCaseConfig) => void;
}

const TestCaseConfigTemplates: React.FC<TestCaseConfigTemplatesProps> = ({
  onSelectTemplate,
}) => {
  const [activeTab, setActiveTab] = useState('get');

  // 模板配置
  const templates = {
    get: {
      name: 'GET请求模板',
      description: '用于查询数据的GET请求',
      config: {
        method: 'GET',
        endpoint: '/api/users',
        headers: {
          'Authorization': 'Bearer ${token}',
          'Content-Type': 'application/json',
        },
        params: {
          'page': 1,
          'size': 10,
          'keyword': '${searchKeyword}',
        },
        body: null,
        assertions: [
          {
            type: 'statusCode' as const,
            expected: 200,
            operator: 'EQUALS' as const,
          },
          {
            type: 'contains' as const,
            expected: 'success',
            operator: 'CONTAINS' as const,
          },
        ],
        extract: {
          'userId': '$.data.id',
          'userName': '$.data.name',
        },
        timeout: 30000,
        retries: 0,
      } as TestCaseConfig,
    },
    post: {
      name: 'POST请求模板',
      description: '用于创建数据的POST请求',
      config: {
        method: 'POST',
        endpoint: '/api/users',
        headers: {
          'Authorization': 'Bearer ${token}',
          'Content-Type': 'application/json',
        },
        params: {},
        body: {
          'name': '${userName}',
          'email': '${userEmail}',
          'age': 25,
        },
        assertions: [
          {
            type: 'statusCode' as const,
            expected: 201,
            operator: 'EQUALS' as const,
          },
          {
            type: 'jsonPath' as const,
            path: '$.data.id',
            expected: '${userId}',
            operator: 'NOT_EQUALS' as const,
          },
        ],
        extract: {
          'newUserId': '$.data.id',
          'createdAt': '$.data.createdAt',
        },
        timeout: 30000,
        retries: 0,
      } as TestCaseConfig,
    },
    put: {
      name: 'PUT请求模板',
      description: '用于更新数据的PUT请求',
      config: {
        method: 'PUT',
        endpoint: '/api/users/${userId}',
        headers: {
          'Authorization': 'Bearer ${token}',
          'Content-Type': 'application/json',
        },
        params: {},
        body: {
          'name': '${updatedName}',
          'email': '${updatedEmail}',
        },
        assertions: [
          {
            type: 'statusCode' as const,
            expected: 200,
            operator: 'EQUALS' as const,
          },
          {
            type: 'jsonPath' as const,
            path: '$.data.name',
            expected: '${updatedName}',
            operator: 'EQUALS' as const,
          },
        ],
        extract: {
          'updatedAt': '$.data.updatedAt',
        },
        timeout: 30000,
        retries: 0,
      } as TestCaseConfig,
    },
    delete: {
      name: 'DELETE请求模板',
      description: '用于删除数据的DELETE请求',
      config: {
        method: 'DELETE',
        endpoint: '/api/users/${userId}',
        headers: {
          'Authorization': 'Bearer ${token}',
        },
        params: {},
        body: null,
        assertions: [
          {
            type: 'statusCode' as const,
            expected: 204,
            operator: 'EQUALS' as const,
          },
        ],
        extract: {},
        timeout: 30000,
        retries: 0,
      } as TestCaseConfig,
    },
    login: {
      name: '登录认证模板',
      description: '用于用户登录认证的请求',
      config: {
        method: 'POST',
        endpoint: '/api/auth/login',
        headers: {
          'Content-Type': 'application/json',
        },
        params: {},
        body: {
          'username': '${username}',
          'password': '${password}',
        },
        assertions: [
          {
            type: 'statusCode' as const,
            expected: 200,
            operator: 'EQUALS' as const,
          },
          {
            type: 'jsonPath' as const,
            path: '$.data.token',
            expected: '',
            operator: 'NOT_EQUALS' as const,
          },
        ],
        extract: {
          'token': '$.data.token',
          'refreshToken': '$.data.refreshToken',
          'userId': '$.data.user.id',
        },
        timeout: 30000,
        retries: 0,
      } as TestCaseConfig,
    },
  };

  // 复制到剪贴板
  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text).then(() => {
      // 可以添加成功提示
    });
  };

  // 选择模板
  const selectTemplate = (templateKey: string) => {
    const template = templates[templateKey as keyof typeof templates];
    if (template && onSelectTemplate) {
      onSelectTemplate(template.config);
    }
  };

  // 渲染配置示例
  const renderConfigExample = (config: TestCaseConfig) => {
    return (
      <div className="config-example">
        <pre>{JSON.stringify(config, null, 2)}</pre>
      </div>
    );
  };

  // 渲染变量说明
  const renderVariableHelp = () => {
    return (
      <div className="variable-help">
        <Title level={4}>变量说明</Title>
        <Paragraph>
          在配置中可以使用以下变量，系统会自动替换为实际值：
        </Paragraph>
        <div className="variable-list">
          <div className="variable-item">
            <Tag color="blue">{'${token}'}</Tag>
            <Text>认证令牌，从环境认证配置中获取</Text>
          </div>
          <div className="variable-item">
            <Tag color="blue">{'${refreshToken}'}</Tag>
            <Text>刷新令牌，从环境认证配置中获取</Text>
          </div>
          <div className="variable-item">
            <Tag color="blue">{'${variableName}'}</Tag>
            <Text>环境变量，从变量管理中获取</Text>
          </div>
          <div className="variable-item">
            <Tag color="blue">{'${extractedData}'}</Tag>
            <Text>提取的数据，从前面测试用例的响应中提取</Text>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="test-case-config-templates">
      <Card>
        <div className="templates-header">
          <Title level={3}>
            <ApiOutlined /> 测试用例配置模板
          </Title>
          <Text type="secondary">
            选择适合的模板快速创建测试用例配置
          </Text>
        </div>

        <Tabs activeKey={activeTab} onChange={setActiveTab}>
          {Object.entries(templates).map(([key, template]) => (
            <TabPane tab={template.name} key={key}>
              <div className="template-content">
                <div className="template-info">
                  <Title level={4}>{template.name}</Title>
                  <Paragraph>{template.description}</Paragraph>
                  
                  <Space>
                    <Button
                      type="primary"
                      icon={<CheckCircleOutlined />}
                      onClick={() => selectTemplate(key)}
                    >
                      使用此模板
                    </Button>
                    <Button
                      icon={<CopyOutlined />}
                      onClick={() => copyToClipboard(JSON.stringify(template.config, null, 2))}
                    >
                      复制配置
                    </Button>
                  </Space>
                </div>

                <Divider />

                <div className="template-config">
                  <Title level={5}>配置示例</Title>
                  {renderConfigExample(template.config)}
                </div>
              </div>
            </TabPane>
          ))}
        </Tabs>

        <Divider />

        <Collapse>
          <Panel header="变量使用说明" key="variables">
            {renderVariableHelp()}
          </Panel>
          
          <Panel header="断言类型说明" key="assertions">
            <div className="assertion-help">
              <Title level={4}>支持的断言类型</Title>
              <div className="assertion-list">
                <div className="assertion-item">
                  <Tag color="green">statusCode</Tag>
                  <Text>HTTP状态码断言</Text>
                </div>
                <div className="assertion-item">
                  <Tag color="green">contains</Tag>
                  <Text>响应内容包含断言</Text>
                </div>
                <div className="assertion-item">
                  <Tag color="green">jsonPath</Tag>
                  <Text>JSON路径断言</Text>
                </div>
                <div className="assertion-item">
                  <Tag color="green">responseTime</Tag>
                  <Text>响应时间断言</Text>
                </div>
              </div>
            </div>
          </Panel>
          
          <Panel header="数据提取说明" key="extract">
            <div className="extract-help">
              <Title level={4}>JSONPath语法</Title>
              <Paragraph>
                使用JSONPath语法从响应中提取数据：
              </Paragraph>
              <div className="extract-examples">
                <div className="extract-example">
                  <Text code>$.data.id</Text>
                  <Text> - 提取data对象中的id字段</Text>
                </div>
                <div className="extract-example">
                  <Text code>$.items[0].name</Text>
                  <Text> - 提取items数组第一个元素的name字段</Text>
                </div>
                <div className="extract-example">
                  <Text code>$.data.*.id</Text>
                  <Text> - 提取data对象中所有id字段</Text>
                </div>
              </div>
            </div>
          </Panel>
        </Collapse>
      </Card>
    </div>
  );
};

export default TestCaseConfigTemplates;
