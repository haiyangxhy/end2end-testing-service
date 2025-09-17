import requests
import json

# 测试创建测试套件
url = "http://localhost:8180/api/test-suites"

# 测试数据
test_suite_data = {
    "name": "API测试套件",
    "description": "用于测试API功能",
    "type": "API"
}

# 发送POST请求创建测试套件
response = requests.post(
    url,
    headers={"Content-Type": "application/json"},
    data=json.dumps(test_suite_data)
)

print(f"Status Code: {response.status_code}")
print(f"Response: {response.json()}")

# 获取所有测试套件
get_response = requests.get(url)
print(f"Get all test suites Status Code: {get_response.status_code}")
print(f"Get all test suites Response: {get_response.json()}")