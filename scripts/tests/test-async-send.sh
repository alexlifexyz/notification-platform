#!/bin/bash

# 测试异步发送功能的脚本

echo "=== 测试异步发送功能 ==="

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

# 测试1: 异步模板发送
print_info "1. 测试异步模板发送..."
ASYNC_RESPONSE=$(curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send-async" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "async_template_001",
    "templateCode": "USER_WELCOME",
    "recipient": {
      "type": "individual",
      "id": "async_user_001",
      "contactInfo": {
        "userName": "异步测试用户",
        "email": "async@example.com"
      }
    },
    "templateParams": {
      "userName": "异步测试用户",
      "welcomeMessage": "欢迎使用异步通知功能！"
    }
  }')

echo "$ASYNC_RESPONSE" | jq '.'

# 提取requestId用于后续状态查询
REQUEST_ID=$(echo "$ASYNC_RESPONSE" | jq -r '.requestId')

if [ "$REQUEST_ID" != "null" ] && [ "$REQUEST_ID" != "" ]; then
    print_success "异步任务提交成功: $REQUEST_ID"
    
    # 等待2秒后查询状态
    print_info "等待2秒后查询任务状态..."
    sleep 2
    
    # 查询任务状态
    print_info "查询任务状态: $REQUEST_ID"
    curl -s -X GET "${SERVICE_URL}/api/v1/notifications/async/status/${REQUEST_ID}" | jq '.'
else
    print_error "异步任务提交失败"
fi

echo -e "\n"

# 测试2: 异步直接发送
print_info "2. 测试异步直接发送..."
ASYNC_DIRECT_RESPONSE=$(curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send-direct-async" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "async_direct_001",
    "channelCodes": ["IN_APP", "EMAIL"],
    "subject": "异步直接发送测试",
    "content": "这是一条异步直接发送的测试消息，不使用模板配置。",
    "recipient": {
      "type": "individual",
      "id": "async_user_002",
      "contactInfo": {
        "userName": "异步直接测试用户",
        "email": "async_direct@example.com"
      }
    }
  }')

echo "$ASYNC_DIRECT_RESPONSE" | jq '.'

# 提取requestId用于后续状态查询
DIRECT_REQUEST_ID=$(echo "$ASYNC_DIRECT_RESPONSE" | jq -r '.requestId')

if [ "$DIRECT_REQUEST_ID" != "null" ] && [ "$DIRECT_REQUEST_ID" != "" ]; then
    print_success "异步直接发送任务提交成功: $DIRECT_REQUEST_ID"
    
    # 等待3秒后查询状态
    print_info "等待3秒后查询任务状态..."
    sleep 3
    
    # 查询任务状态
    print_info "查询任务状态: $DIRECT_REQUEST_ID"
    curl -s -X GET "${SERVICE_URL}/api/v1/notifications/async/status/${DIRECT_REQUEST_ID}" | jq '.'
else
    print_error "异步直接发送任务提交失败"
fi

echo -e "\n"

# 测试3: 批量异步发送
print_info "3. 测试批量异步发送..."
for i in {1..5}; do
    BATCH_REQUEST_ID="async_batch_$(printf "%03d" $i)"
    
    print_info "提交批量任务 $i/5: $BATCH_REQUEST_ID"
    
    curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send-async" \
      -H "Content-Type: application/json" \
      -d "{
        \"requestId\": \"${BATCH_REQUEST_ID}\",
        \"templateCode\": \"USER_WELCOME\",
        \"recipient\": {
          \"type\": \"individual\",
          \"id\": \"batch_user_${i}\",
          \"contactInfo\": {
            \"userName\": \"批量用户${i}\",
            \"email\": \"batch${i}@example.com\"
          }
        },
        \"templateParams\": {
          \"userName\": \"批量用户${i}\",
          \"welcomeMessage\": \"这是第${i}个批量异步通知！\"
        }
      }" > /dev/null
    
    if [ $? -eq 0 ]; then
        print_success "批量任务 $i 提交成功"
    else
        print_error "批量任务 $i 提交失败"
    fi
    
    # 避免过快提交
    sleep 0.5
done

echo -e "\n"

# 测试4: 任务取消
print_info "4. 测试任务取消功能..."
CANCEL_REQUEST_ID="async_cancel_001"

# 提交一个任务
print_info "提交待取消的任务: $CANCEL_REQUEST_ID"
curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send-async" \
  -H "Content-Type: application/json" \
  -d "{
    \"requestId\": \"${CANCEL_REQUEST_ID}\",
    \"templateCode\": \"USER_WELCOME\",
    \"recipient\": {
      \"type\": \"individual\",
      \"id\": \"cancel_user\",
      \"contactInfo\": {
        \"userName\": \"待取消用户\",
        \"email\": \"cancel@example.com\"
      }
    },
    \"templateParams\": {
      \"userName\": \"待取消用户\",
      \"welcomeMessage\": \"这个任务将被取消\"
    }
  }" > /dev/null

# 立即尝试取消
print_info "尝试取消任务: $CANCEL_REQUEST_ID"
CANCEL_RESPONSE=$(curl -s -X DELETE "${SERVICE_URL}/api/v1/notifications/async/cancel/${CANCEL_REQUEST_ID}")
echo "取消结果: $CANCEL_RESPONSE"

# 查询取消后的状态
print_info "查询取消后的任务状态: $CANCEL_REQUEST_ID"
curl -s -X GET "${SERVICE_URL}/api/v1/notifications/async/status/${CANCEL_REQUEST_ID}" | jq '.'

echo -e "\n"

# 测试5: 错误处理
print_info "5. 测试错误处理..."
print_info "发送无效模板的异步请求..."
ERROR_RESPONSE=$(curl -s -X POST "${SERVICE_URL}/api/v1/notifications/send-async" \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "async_error_001",
    "templateCode": "INVALID_TEMPLATE",
    "recipient": {
      "type": "individual",
      "id": "error_user",
      "contactInfo": {
        "userName": "错误测试用户"
      }
    },
    "templateParams": {}
  }')

echo "$ERROR_RESPONSE" | jq '.'

ERROR_REQUEST_ID=$(echo "$ERROR_RESPONSE" | jq -r '.requestId')
if [ "$ERROR_REQUEST_ID" != "null" ] && [ "$ERROR_REQUEST_ID" != "" ]; then
    # 等待任务执行完成
    sleep 3
    
    print_info "查询错误任务状态: $ERROR_REQUEST_ID"
    curl -s -X GET "${SERVICE_URL}/api/v1/notifications/async/status/${ERROR_REQUEST_ID}" | jq '.'
fi

echo -e "\n=== 异步发送功能测试完成 ==="

echo -e "\n=== 测试结果说明 ==="
print_info "异步发送功能测试要点："
echo "1. 异步任务提交后立即返回，不等待实际发送完成"
echo "2. 返回的taskStatus应该是'SUBMITTED'"
echo "3. 可以通过状态查询接口跟踪任务进度"
echo "4. 支持任务取消功能（如果任务还未开始执行）"
echo "5. 错误任务会在状态查询中显示失败信息"
echo ""
print_warning "注意事项："
echo "- 异步任务的实际执行时间取决于线程池配置和系统负载"
echo "- 任务状态会在内存中保留24小时（可配置）"
echo "- 生产环境建议配置适当的线程池大小和队列容量"
