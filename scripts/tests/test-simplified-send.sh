#!/bin/bash

# 测试简化后的发送接口功能

echo "=== 测试简化后的发送接口功能 ==="

# 服务地址
SERVICE_URL="http://localhost:8080/notification-service"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 测试1: 简化后的单用户发送
print_info "1. 测试简化后的单用户发送..."
curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "simple_single_001",
    "channelCodes": ["IN_APP", "EMAIL"],
    "subject": "简化接口单用户测试",
    "content": "这是使用简化接口发送给单个用户的测试消息。",
    "users": [
      {
        "userId": "user001",
        "userName": "张三",
        "email": "zhangsan@example.com"
      }
    ]
  }' | jq '.'

echo -e "\n"

# 测试2: 简化后的多用户发送（核心功能）
print_info "2. 测试简化后的多用户发送（核心功能）..."
curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "simple_multi_001",
    "channelCodes": ["IN_APP", "EMAIL"],
    "subject": "简化接口多用户测试",
    "content": "这是使用简化接口发送给多个用户的测试消息，展示了多用户发送的便利性。",
    "users": [
      {
        "userId": "user001",
        "userName": "张三",
        "email": "zhangsan@example.com"
      },
      {
        "userId": "user002", 
        "userName": "李四",
        "email": "lisi@example.com"
      },
      {
        "userId": "user003",
        "userName": "王五", 
        "email": "wangwu@example.com"
      }
    ]
  }' | jq '.'

echo -e "\n"

# 测试3: 简化后的组发送
print_info "3. 测试简化后的组发送..."
curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "simple_group_001",
    "channelCodes": ["IN_APP"],
    "subject": "简化接口组发送测试",
    "content": "这是使用简化接口发送给用户组的测试消息。",
    "groupCode": "ALL_EMPLOYEES"
  }' | jq '.'

echo -e "\n"

# 测试4: 简化后的模板发送（单用户）
print_info "4. 测试简化后的模板发送（单用户）..."
curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "simple_template_001",
    "templateCode": "USER_WELCOME",
    "templateParams": {
      "userName": "赵六",
      "welcomeMessage": "欢迎使用简化接口！"
    },
    "users": [
      {
        "userId": "user006",
        "userName": "赵六",
        "email": "zhaoliu@example.com"
      }
    ]
  }' | jq '.'

echo -e "\n"

# 测试5: 简化后的模板发送（多用户）
print_info "5. 测试简化后的模板发送（多用户）..."
curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "simple_template_multi_001",
    "templateCode": "USER_WELCOME",
    "templateParams": {
      "welcomeMessage": "批量欢迎使用简化接口！"
    },
    "users": [
      {
        "userId": "user007",
        "userName": "孙七",
        "email": "sunqi@example.com"
      },
      {
        "userId": "user008",
        "userName": "周八",
        "email": "zhouba@example.com"
      }
    ]
  }' | jq '.'

echo -e "\n"

# 测试6: 异步简化发送
print_info "6. 测试异步简化发送..."
ASYNC_RESPONSE=$(curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send-direct-async" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "simple_async_001",
    "channelCodes": ["IN_APP", "EMAIL"],
    "subject": "异步简化发送测试",
    "content": "这是使用异步简化接口发送的测试消息。",
    "users": [
      {
        "userId": "user009",
        "userName": "吴九",
        "email": "wujiu@example.com"
      },
      {
        "userId": "user010",
        "userName": "郑十",
        "email": "zhengshi@example.com"
      }
    ]
  }')

echo "$ASYNC_RESPONSE" | jq '.'

# 提取requestId用于状态查询
REQUEST_ID=$(echo "$ASYNC_RESPONSE" | jq -r '.requestId')
if [ "$REQUEST_ID" != "null" ] && [ "$REQUEST_ID" != "" ]; then
    print_success "异步任务提交成功: $REQUEST_ID"
    
    # 等待3秒后查询状态
    print_info "等待3秒后查询任务状态..."
    sleep 3
    
    print_info "查询任务状态: $REQUEST_ID"
    curl -s -X GET "${SERVICE_URL}/api/v1/notifications/async/status/${REQUEST_ID}" | jq '.'
fi

echo -e "\n"

# 测试7: 参数验证测试
print_info "7. 测试参数验证..."

print_info "7.1 测试缺少用户信息的请求..."
curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "validation_001",
    "channelCodes": ["EMAIL"],
    "subject": "验证测试",
    "content": "这个请求应该失败，因为没有用户信息"
  }' | jq '.'

echo -e "\n"

print_info "7.2 测试EMAIL渠道但用户没有邮箱的请求..."
curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "validation_002",
    "channelCodes": ["EMAIL"],
    "subject": "验证测试",
    "content": "这个请求应该失败，因为用户没有邮箱",
    "users": [
      {
        "userId": "user_no_email",
        "userName": "无邮箱用户"
      }
    ]
  }' | jq '.'

echo -e "\n"

# 测试8: 大批量用户发送
print_info "8. 测试大批量用户发送（10个用户）..."
curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send-direct" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "batch_simple_001",
    "channelCodes": ["IN_APP"],
    "subject": "批量发送测试",
    "content": "这是一条批量发送给10个用户的测试消息。",
    "users": [
      {"userId": "batch_user_01", "userName": "批量用户01"},
      {"userId": "batch_user_02", "userName": "批量用户02"},
      {"userId": "batch_user_03", "userName": "批量用户03"},
      {"userId": "batch_user_04", "userName": "批量用户04"},
      {"userId": "batch_user_05", "userName": "批量用户05"},
      {"userId": "batch_user_06", "userName": "批量用户06"},
      {"userId": "batch_user_07", "userName": "批量用户07"},
      {"userId": "batch_user_08", "userName": "批量用户08"},
      {"userId": "batch_user_09", "userName": "批量用户09"},
      {"userId": "batch_user_10", "userName": "批量用户10"}
    ]
  }' | jq '.'

echo -e "\n=== 简化发送接口功能测试完成 ==="

echo -e "\n=== 测试结果说明 ==="
print_info "简化发送接口的优势："
echo "1. 去除复杂嵌套 - 不再需要RecipientInfo和ContactInfo的复杂结构"
echo "2. 多用户发送支持 - 一次请求可发送给多个用户"
echo "3. 信息一一对应 - 用户信息封装在UserInfo对象中，避免索引错乱"
echo "4. 智能参数验证 - 根据渠道自动验证必要的联系方式"
echo "5. 便捷构造方法 - 提供常用组合的快捷构造"
echo ""
print_success "简化后的接口特点："
echo "- 扁平化结构：users字段直接包含用户信息"
echo "- 多用户支持：一次调用发送给多个用户"
echo "- 组发送支持：使用groupCode字段"
echo "- 同时支持同步和异步发送"
echo "- 统一的UserInfo结构：BaseNotificationRequest.UserInfo"
echo ""
print_warning "使用建议："
echo "- 推荐使用新的简化格式"
echo "- 大批量用户建议分批发送"
echo "- 注意根据渠道提供对应的联系方式"
