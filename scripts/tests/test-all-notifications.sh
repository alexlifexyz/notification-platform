#!/bin/bash

# 全渠道通知测试脚本
# 测试邮件、短信、IM、站内信所有通知功能

echo "=== 全渠道通知功能测试 ==="
echo

# 服务器地址
SERVER_URL="http://localhost:8080"

# 测试1: 发送邮件通知
echo "1. 测试邮件通知..."
curl -X POST "${SERVER_URL}/api/v1/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test_email_'$(date +%s)'",
    "templateCode": "EMAIL_WELCOME",
    "recipient": {
      "type": "individual",
      "id": "user_001",
      "contactInfo": {
        "userName": "张三",
        "email": "zhangsan@example.com"
      }
    },
    "templateParams": {
      "userName": "张三",
      "companyName": "企业通知平台"
    }
  }' \
  -w "\n状态码: %{http_code}\n" \
  -s

echo
echo "----------------------------------------"
echo

# 测试2: 发送短信通知
echo "2. 测试短信通知..."
curl -X POST "${SERVER_URL}/api/v1/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test_sms_'$(date +%s)'",
    "templateCode": "SMS_VERIFY_CODE",
    "recipient": {
      "type": "individual",
      "id": "user_002",
      "contactInfo": {
        "userName": "李四",
        "phone": "13800138001"
      }
    },
    "templateParams": {
      "code": "123456"
    }
  }' \
  -w "\n状态码: %{http_code}\n" \
  -s

echo
echo "----------------------------------------"
echo

# 测试3: 发送IM通知
echo "3. 测试IM通知..."
curl -X POST "${SERVER_URL}/api/v1/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test_im_'$(date +%s)'",
    "templateCode": "IM_SYSTEM_ALERT",
    "recipient": {
      "type": "individual",
      "id": "user_003",
      "contactInfo": {
        "userName": "王五",
        "imAccount": "wangwu"
      }
    },
    "templateParams": {
      "alertLevel": "高",
      "alertMessage": "系统CPU使用率超过90%",
      "alertTime": "2024-01-30 15:30:00",
      "affectedServices": "用户服务、订单服务"
    }
  }' \
  -w "\n状态码: %{http_code}\n" \
  -s

echo
echo "----------------------------------------"
echo

# 测试4: 发送站内信通知
echo "4. 测试站内信通知..."
curl -X POST "${SERVER_URL}/api/v1/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test_inapp_'$(date +%s)'",
    "templateCode": "USER_REGISTER_WELCOME",
    "recipient": {
      "type": "individual",
      "id": "user_004",
      "contactInfo": {
        "userName": "赵六"
      }
    },
    "templateParams": {
      "userName": "赵六"
    }
  }' \
  -w "\n状态码: %{http_code}\n" \
  -s

echo
echo "----------------------------------------"
echo

# 测试5: 多渠道组合通知（发送给组）
echo "5. 测试组通知（多渠道）..."
curl -X POST "${SERVER_URL}/api/v1/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test_group_'$(date +%s)'",
    "templateCode": "SYSTEM_MAINTENANCE",
    "recipient": {
      "type": "group",
      "id": "ADMIN_GROUP"
    },
    "templateParams": {
      "startTime": "2024-01-30 22:00:00",
      "duration": "2小时"
    }
  }' \
  -w "\n状态码: %{http_code}\n" \
  -s

echo
echo "----------------------------------------"
echo

# 测试6: 测试错误处理（无效手机号）
echo "6. 测试错误处理（无效手机号）..."
curl -X POST "${SERVER_URL}/api/v1/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test_error_'$(date +%s)'",
    "templateCode": "SMS_VERIFY_CODE",
    "recipient": {
      "type": "individual",
      "id": "user_error",
      "contactInfo": {
        "userName": "错误测试",
        "phone": "invalid_phone"
      }
    },
    "templateParams": {
      "code": "123456"
    }
  }' \
  -w "\n状态码: %{http_code}\n" \
  -s

echo
echo "=== 测试完成 ==="
echo
echo "功能说明:"
echo "✅ 邮件通知 - 支持HTML和纯文本邮件"
echo "✅ 短信通知 - 支持阿里云和腾讯云短信"
echo "✅ IM通知 - 支持企业微信和钉钉"
echo "✅ 站内信通知 - 直接存储到数据库"
echo "✅ 组通知 - 支持发送给用户组"
echo "✅ 错误处理 - 完整的参数验证和错误处理"
echo
echo "配置说明:"
echo "- 邮件: 需要配置SMTP服务器信息"
echo "- 短信: 需要配置短信服务商密钥"
echo "- IM: 需要配置企业微信或钉钉应用信息"
echo "- 站内信: 无需额外配置，直接可用"
echo
echo "查看详细日志:"
echo "tail -f logs/notification-service.log"
