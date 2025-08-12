#!/bin/bash

# 测试多渠道直接发送的幂等性功能

echo "=== 测试多渠道直接发送幂等性 ==="

# 服务地址
SERVICE_URL="http://localhost:8080/notification-service"

# 测试1: 首次多渠道发送
echo "1. 首次多渠道发送（站内信+邮件）..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "idempotency_test_001",
    "channelCodes": ["IN_APP", "EMAIL"],
    "subject": "幂等性测试消息",
    "content": "这是用于测试多渠道幂等性的消息。",
    "recipient": {
      "type": "individual",
      "id": "user001",
      "contactInfo": {
        "userName": "测试用户1",
        "email": "user001@example.com"
      }
    }
  }' | jq '.'

echo -e "\n等待3秒...\n"
sleep 3

# 测试2: 重复相同的多渠道发送请求（应该返回幂等结果）
echo "2. 重复相同的多渠道发送请求（测试幂等性）..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "idempotency_test_001",
    "channelCodes": ["IN_APP", "EMAIL"],
    "subject": "幂等性测试消息（重复）",
    "content": "这个内容应该被忽略，因为请求ID已存在。",
    "recipient": {
      "type": "individual",
      "id": "user001",
      "contactInfo": {
        "userName": "测试用户1",
        "email": "user001@example.com"
      }
    }
  }' | jq '.'

echo -e "\n等待3秒...\n"
sleep 3

# 测试3: 相同requestId但不同渠道组合（部分幂等）
echo "3. 相同requestId但添加新渠道（部分幂等性测试）..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "idempotency_test_001",
    "channelCodes": ["IN_APP", "EMAIL", "SMS"],
    "subject": "添加短信渠道",
    "content": "这次添加了短信渠道，应该只发送短信，其他渠道跳过。",
    "recipient": {
      "type": "individual",
      "id": "user001",
      "contactInfo": {
        "userName": "测试用户1",
        "email": "user001@example.com",
        "phone": "13800138001"
      }
    }
  }' | jq '.'

echo -e "\n等待3秒...\n"
sleep 3

# 测试4: 新的requestId，多渠道发送
echo "4. 新的requestId，多渠道发送..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "idempotency_test_002",
    "channelCodes": ["IN_APP", "SMS"],
    "subject": "新请求多渠道测试",
    "content": "这是一个新的请求ID，应该正常发送所有渠道。",
    "recipient": {
      "type": "individual",
      "id": "user002",
      "contactInfo": {
        "userName": "测试用户2",
        "phone": "13800138002"
      }
    }
  }' | jq '.'

echo -e "\n等待3秒...\n"
sleep 3

# 测试5: 重复测试4的请求（完全幂等）
echo "5. 重复测试4的请求（完全幂等）..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "idempotency_test_002",
    "channelCodes": ["IN_APP", "SMS"],
    "subject": "重复请求",
    "content": "这个请求应该被完全跳过。",
    "recipient": {
      "type": "individual",
      "id": "user002",
      "contactInfo": {
        "userName": "测试用户2",
        "phone": "13800138002"
      }
    }
  }' | jq '.'

echo -e "\n=== 幂等性测试完成 ==="

echo -e "\n=== 预期结果说明 ==="
echo "测试1: 首次发送，应该成功发送站内信和邮件，results包含2条结果"
echo "测试2: 重复请求，应该跳过所有渠道，results为空数组"
echo "测试3: 部分新渠道，应该跳过已发送的渠道，只发送新的短信渠道，results包含1条结果"
echo "测试4: 新请求ID，应该正常发送所有渠道，results包含2条结果"
echo "测试5: 重复新请求，应该跳过所有渠道，results为空数组"
echo ""
echo "关键验证点："
echo "- 相同requestId+channelCode组合不会重复发送"
echo "- 相同requestId但不同channelCode可以发送"
echo "- 已处理的渠道直接跳过，不在响应结果中返回"
echo "- 只有本次实际发送的渠道才会在results中返回"
