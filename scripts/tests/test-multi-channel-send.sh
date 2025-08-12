#!/bin/bash

# 测试多渠道直接发送功能的专用脚本

echo "=== 测试多渠道直接发送功能 ==="

# 服务地址
SERVICE_URL="http://localhost:8080/notification-service"

# 测试1: 单渠道发送（站内信）
echo "1. 测试单渠道发送（站内信）..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "multi_test_001",
    "channelCodes": ["IN_APP"],
    "subject": "单渠道测试",
    "content": "这是一条单渠道（站内信）发送的测试消息。",
    "recipient": {
      "type": "individual",
      "id": "user001",
      "contactInfo": {
        "userName": "测试用户1"
      }
    }
  }' | jq '.'

echo -e "\n"

# 测试2: 双渠道发送（站内信+邮件）
echo "2. 测试双渠道发送（站内信+邮件）..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "multi_test_002",
    "channelCodes": ["IN_APP", "EMAIL"],
    "subject": "双渠道重要通知",
    "content": "这是一条通过站内信和邮件同时发送的重要通知，请及时查看。",
    "recipient": {
      "type": "individual",
      "id": "user002",
      "contactInfo": {
        "userName": "测试用户2",
        "email": "user002@example.com"
      }
    }
  }' | jq '.'

echo -e "\n"

# 测试3: 三渠道发送（站内信+邮件+短信）
echo "3. 测试三渠道发送（站内信+邮件+短信）..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "multi_test_003",
    "channelCodes": ["IN_APP", "EMAIL", "SMS"],
    "subject": "紧急通知",
    "content": "系统将在10分钟后进行紧急维护，请立即保存您的工作！",
    "recipient": {
      "type": "individual",
      "id": "user003",
      "contactInfo": {
        "userName": "测试用户3",
        "email": "user003@example.com",
        "phone": "13800138003"
      }
    }
  }' | jq '.'

echo -e "\n"

# 测试4: 全渠道发送（站内信+邮件+短信+IM）
echo "4. 测试全渠道发送（站内信+邮件+短信+IM）..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "multi_test_004",
    "channelCodes": ["IN_APP", "EMAIL", "SMS", "IM"],
    "subject": "全渠道测试通知",
    "content": "这是一条通过所有可用渠道发送的测试通知，用于验证多渠道发送功能。",
    "recipient": {
      "type": "individual",
      "id": "user004",
      "contactInfo": {
        "userName": "测试用户4",
        "email": "user004@example.com",
        "phone": "13800138004",
        "imAccount": "user004_im"
      }
    }
  }' | jq '.'

echo -e "\n"

# 测试5: 组多渠道发送
echo "5. 测试组多渠道发送..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "multi_test_005",
    "channelCodes": ["IN_APP", "EMAIL"],
    "subject": "团队重要通知",
    "content": "团队会议时间调整为明天下午3点，请所有成员准时参加。",
    "recipient": {
      "type": "group",
      "id": "DEV_TEAM"
    }
  }' | jq '.'

echo -e "\n"

# 测试6: 混合有效和无效渠道
echo "6. 测试混合有效和无效渠道..."
curl -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "multi_test_006",
    "channelCodes": ["IN_APP", "INVALID_CHANNEL", "EMAIL"],
    "subject": "混合渠道测试",
    "content": "这个请求包含无效渠道，应该返回错误。",
    "recipient": {
      "type": "individual",
      "id": "user006"
    }
  }' | jq '.'

echo -e "\n=== 多渠道测试完成 ==="

echo -e "\n=== 响应结果说明 ==="
echo "- 成功的多渠道发送会在results数组中包含每个渠道的发送结果"
echo "- 每个结果包含：notificationId, channelCode, recipientId, status, sentAt"
echo "- 如果某个渠道发送失败，不会影响其他渠道的发送"
echo "- 整体状态：SUCCESS（全部成功）、PARTIAL_SUCCESS（部分成功）、FAILED（全部失败）"
