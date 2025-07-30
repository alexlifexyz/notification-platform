#!/bin/bash

# 邮件通知API测试脚本
# 用于测试邮件发送功能

echo "=== 邮件通知API测试 ==="
echo

# 服务器地址
SERVER_URL="http://localhost:8080"

# 测试1: 发送欢迎邮件
echo "1. 测试发送欢迎邮件..."
curl -X POST "${SERVER_URL}/api/v1/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test_email_welcome_'$(date +%s)'",
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

# 测试2: 发送密码重置邮件
echo "2. 测试发送密码重置邮件..."
curl -X POST "${SERVER_URL}/api/v1/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test_email_reset_'$(date +%s)'",
    "templateCode": "EMAIL_PASSWORD_RESET",
    "recipient": {
      "type": "individual",
      "id": "user_002",
      "contactInfo": {
        "userName": "李四",
        "email": "lisi@example.com"
      }
    },
    "templateParams": {
      "resetLink": "https://platform.company.com/reset-password?token=abc123xyz"
    }
  }' \
  -w "\n状态码: %{http_code}\n" \
  -s

echo
echo "----------------------------------------"
echo

# 测试3: 发送订单确认邮件
echo "3. 测试发送订单确认邮件..."
curl -X POST "${SERVER_URL}/api/v1/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test_email_order_'$(date +%s)'",
    "templateCode": "EMAIL_ORDER_CONFIRMATION",
    "recipient": {
      "type": "individual",
      "id": "user_003",
      "contactInfo": {
        "userName": "王五",
        "email": "wangwu@example.com"
      }
    },
    "templateParams": {
      "userName": "王五",
      "orderNo": "ORD20240130001",
      "amount": "299.00",
      "orderTime": "2024-01-30 14:30:00"
    }
  }' \
  -w "\n状态码: %{http_code}\n" \
  -s

echo
echo "----------------------------------------"
echo

# 测试4: 测试无效邮箱地址
echo "4. 测试无效邮箱地址..."
curl -X POST "${SERVER_URL}/api/v1/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "test_email_invalid_'$(date +%s)'",
    "templateCode": "EMAIL_WELCOME",
    "recipient": {
      "type": "individual",
      "id": "user_004",
      "contactInfo": {
        "userName": "测试用户",
        "email": ""
      }
    },
    "templateParams": {
      "userName": "测试用户",
      "companyName": "企业通知平台"
    }
  }' \
  -w "\n状态码: %{http_code}\n" \
  -s

echo
echo "=== 测试完成 ==="
echo
echo "说明:"
echo "- 如果服务器未启动，会显示连接错误"
echo "- 如果邮件配置正确，会显示发送成功"
echo "- 如果邮件配置未设置，会使用Mock模式"
echo "- 检查服务器日志可以看到详细的发送过程"
echo
echo "启动服务器命令:"
echo "cd notification-service && java -jar target/notification-service-1.0.0.jar"
