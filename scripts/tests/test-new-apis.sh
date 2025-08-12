#!/bin/bash

# 测试新增的API端点
# 确保notification-admin-bff服务在localhost:8081运行

BASE_URL="http://localhost:8081/notification-admin"

echo "=== 测试新增的API端点 ==="
echo

# 1. 测试渠道管理API
echo "1. 测试渠道管理API"
echo "1.1 获取所有渠道"
curl -s -X GET "${BASE_URL}/api/admin/channels" \
  -H "Content-Type: application/json" | jq '.'
echo

echo "1.2 获取特定渠道详情"
curl -s -X GET "${BASE_URL}/api/admin/channels/IN_APP" \
  -H "Content-Type: application/json" | jq '.'
echo

echo "1.3 创建新渠道"
curl -s -X POST "${BASE_URL}/api/admin/channels" \
  -H "Content-Type: application/json" \
  -d '{
    "channelCode": "WECHAT",
    "channelName": "微信通知",
    "isEnabled": true
  }' | jq '.'
echo

# 2. 测试收件人管理API
echo "2. 测试收件人管理API"
echo "2.1 查询收件人列表"
curl -s -X POST "${BASE_URL}/api/admin/recipients/query" \
  -H "Content-Type: application/json" \
  -d '{
    "current": 1,
    "size": 10,
    "groupCode": "DEV_TEAM"
  }' | jq '.'
echo

echo "2.2 创建收件人"
curl -s -X POST "${BASE_URL}/api/admin/recipients" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test_recipient_001",
    "userName": "测试收件人",
    "phone": "13900139001",
    "email": "test@company.com",
    "groupCode": "TEST_GROUP",
    "preferredChannels": ["IN_APP", "EMAIL"],
    "isEnabled": true
  }' | jq '.'
echo

echo "2.3 获取收件人详情"
curl -s -X GET "${BASE_URL}/api/admin/recipients/test_recipient_001" \
  -H "Content-Type: application/json" | jq '.'
echo

# 3. 测试站内信管理API
echo "3. 测试站内信管理API"
echo "3.1 查询站内信列表"
curl -s -X POST "${BASE_URL}/api/admin/in-app-messages/query" \
  -H "Content-Type: application/json" \
  -d '{
    "current": 1,
    "size": 10,
    "userId": "test001"
  }' | jq '.'
echo

echo "3.2 获取站内信统计"
curl -s -X GET "${BASE_URL}/api/admin/in-app-messages/statistics?userId=test001" \
  -H "Content-Type: application/json" | jq '.'
echo

# 4. 测试现有API是否仍然工作
echo "4. 测试现有API"
echo "4.1 查询模板列表"
curl -s -X POST "${BASE_URL}/api/admin/templates/query" \
  -H "Content-Type: application/json" \
  -d '{
    "current": 1,
    "size": 5
  }' | jq '.'
echo

echo "4.2 查询收件人组列表"
curl -s -X POST "${BASE_URL}/api/admin/recipient-groups/query" \
  -H "Content-Type: application/json" \
  -d '{
    "current": 1,
    "size": 5
  }' | jq '.'
echo

echo "4.3 查询通知记录"
curl -s -X POST "${BASE_URL}/api/admin/notifications/query" \
  -H "Content-Type: application/json" \
  -d '{
    "current": 1,
    "size": 5
  }' | jq '.'
echo

echo "=== API测试完成 ==="
