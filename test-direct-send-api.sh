#!/bin/bash

# 测试直接发送通知API的脚本

echo "=== 测试直接发送通知API ==="

# 服务地址
SERVICE_URL="http://localhost:8080/notification-service"

# 测试1: 直接发送站内信
echo "1. 测试直接发送站内信..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "direct_test_001",
    "channelCodes": ["IN_APP"],
    "subject": "直接发送测试消息",
    "content": "这是一条不使用模板配置的直接发送消息，用于测试新功能。",
    "recipient": {
      "type": "individual",
      "id": "test001",
      "contactInfo": {
        "userName": "测试用户1"
      }
    }
  }' | jq '.'

echo -e "\n"

# 测试2: 直接发送邮件
echo "2. 测试直接发送邮件..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "direct_test_002",
    "channelCodes": ["EMAIL"],
    "subject": "直接发送邮件测试",
    "content": "这是一封直接发送的测试邮件，不依赖任何模板配置。",
    "recipient": {
      "type": "individual",
      "id": "test002",
      "contactInfo": {
        "userName": "测试用户2",
        "email": "test@example.com"
      }
    }
  }' | jq '.'

echo -e "\n"

# 测试3: 多渠道同时发送
echo "3. 测试多渠道同时发送（站内信+邮件）..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "direct_test_003",
    "channelCodes": ["IN_APP", "EMAIL"],
    "subject": "多渠道发送测试",
    "content": "这是一条同时通过站内信和邮件发送的测试消息。",
    "recipient": {
      "type": "individual",
      "id": "test003",
      "contactInfo": {
        "userName": "测试用户3",
        "email": "test3@example.com"
      }
    }
  }' | jq '.'

echo -e "\n"

# 测试4: 测试不支持的渠道
echo "4. 测试不支持的渠道..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "direct_test_004",
    "channelCodes": ["INVALID_CHANNEL"],
    "subject": "测试主题",
    "content": "测试内容",
    "recipient": {
      "type": "individual",
      "id": "test004"
    }
  }' | jq '.'

echo -e "\n"

# 测试5: 测试幂等性
echo "5. 测试幂等性（重复发送相同requestId）..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "direct_test_001",
    "channelCodes": ["IN_APP"],
    "subject": "重复发送测试",
    "content": "这应该被幂等性机制拦截",
    "recipient": {
      "type": "individual",
      "id": "test001"
    }
  }' | jq '.'

echo -e "\n=== 测试完成 ==="
