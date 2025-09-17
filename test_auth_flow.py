import requests
import json

# 测试登录
url = "http://localhost:8180/api/auth/login"
payload = {
    "username": "admin",
    "password": "password"
}
headers = {
    "Content-Type": "application/json"
}

print("Testing login...")
response = requests.post(url, data=json.dumps(payload), headers=headers)

print(f"Status Code: {response.status_code}")
print(f"Response: {response.text}")

if response.status_code == 200:
    token = response.json().get('token')
    print(f"Token: {token}")
    
    # 测试使用token访问受保护的资源
    protected_url = "http://localhost:8180/api/target-system-configs"
    protected_headers = {
        "Authorization": f"Bearer {token}"
    }
    
    print("\nTesting access to protected resource...")
    protected_response = requests.get(protected_url, headers=protected_headers)
    print(f"Protected Resource Status Code: {protected_response.status_code}")
    print(f"Protected Resource Response: {protected_response.text}")
else:
    print("Login failed!")