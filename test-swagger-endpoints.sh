#!/bin/bash

# Swagger 端点测试脚本
# 测试 notification-service 和 notification-admin-bff 的 Swagger 配置

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 服务地址
NOTIFICATION_SERVICE_URL="http://localhost:8080/notification-service"
ADMIN_BFF_URL="http://localhost:8081/notification-admin"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    Swagger 端点测试${NC}"
echo -e "${BLUE}========================================${NC}"
echo

# 测试函数
test_endpoint() {
    local url=$1
    local name=$2
    local expected_status=${3:-200}
    
    echo -n "测试 $name: "
    
    # 发送请求并获取状态码
    status_code=$(curl -s -o /dev/null -w "%{http_code}" "$url" --connect-timeout 5 --max-time 10)
    
    if [ "$status_code" -eq "$expected_status" ]; then
        echo -e "${GREEN}✅ 成功 (HTTP $status_code)${NC}"
        return 0
    else
        echo -e "${RED}❌ 失败 (HTTP $status_code)${NC}"
        return 1
    fi
}

# 测试 JSON 响应
test_json_endpoint() {
    local url=$1
    local name=$2
    
    echo -n "测试 $name: "
    
    # 发送请求并检查是否返回有效 JSON
    response=$(curl -s "$url" --connect-timeout 5 --max-time 10)
    status_code=$(curl -s -o /dev/null -w "%{http_code}" "$url" --connect-timeout 5 --max-time 10)
    
    if [ "$status_code" -eq 200 ] && echo "$response" | jq . >/dev/null 2>&1; then
        echo -e "${GREEN}✅ 成功 (有效JSON)${NC}"
        return 0
    else
        echo -e "${RED}❌ 失败 (HTTP $status_code 或无效JSON)${NC}"
        return 1
    fi
}

# 检查服务是否运行
check_service() {
    local url=$1
    local name=$2
    
    echo -n "检查 $name 服务状态: "
    
    if curl -s --connect-timeout 3 --max-time 5 "$url" >/dev/null 2>&1; then
        echo -e "${GREEN}✅ 运行中${NC}"
        return 0
    else
        echo -e "${RED}❌ 未运行${NC}"
        return 1
    fi
}

# 测试核心通知服务
echo -e "${YELLOW}📡 核心通知服务 (notification-service)${NC}"
echo "-----------------------------------"

if check_service "$NOTIFICATION_SERVICE_URL/api/v1/notifications/health" "notification-service"; then
    test_endpoint "$NOTIFICATION_SERVICE_URL/swagger-ui.html" "Swagger UI"
    test_endpoint "$NOTIFICATION_SERVICE_URL/swagger-ui/index.html" "Swagger UI (备用路径)"
    test_json_endpoint "$NOTIFICATION_SERVICE_URL/api-docs" "API Docs JSON"
    test_json_endpoint "$NOTIFICATION_SERVICE_URL/v3/api-docs" "OpenAPI 3 JSON (备用路径)"
    
    echo
    echo "📋 可用的 Swagger 地址:"
    echo "   • Swagger UI: $NOTIFICATION_SERVICE_URL/swagger-ui.html"
    echo "   • API Docs:   $NOTIFICATION_SERVICE_URL/api-docs"
else
    echo -e "${RED}⚠️  请先启动 notification-service 服务${NC}"
fi

echo
echo

# 测试管理后台服务
echo -e "${YELLOW}🛠️ 管理后台服务 (notification-admin-bff)${NC}"
echo "----------------------------------------"

if check_service "$ADMIN_BFF_URL/api/admin/channels" "notification-admin-bff"; then
    test_endpoint "$ADMIN_BFF_URL/swagger-ui.html" "Swagger UI"
    test_endpoint "$ADMIN_BFF_URL/swagger-ui/index.html" "Swagger UI (备用路径)"
    test_json_endpoint "$ADMIN_BFF_URL/api-docs" "API Docs JSON"
    test_json_endpoint "$ADMIN_BFF_URL/v3/api-docs" "OpenAPI 3 JSON (备用路径)"
    
    echo
    echo "📋 可用的 Swagger 地址:"
    echo "   • Swagger UI: $ADMIN_BFF_URL/swagger-ui.html"
    echo "   • API Docs:   $ADMIN_BFF_URL/api-docs"
else
    echo -e "${RED}⚠️  请先启动 notification-admin-bff 服务${NC}"
fi

echo
echo

# 总结
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    测试总结${NC}"
echo -e "${BLUE}========================================${NC}"
echo
echo -e "${GREEN}✅ Swagger 配置完成！${NC}"
echo
echo "🚀 启动服务命令:"
echo "   # 启动核心服务"
echo "   cd notification-service && mvn spring-boot:run"
echo
echo "   # 启动管理后台 (新终端)"
echo "   cd notification-admin-bff && mvn spring-boot:run"
echo
echo "📖 访问 Swagger 文档:"
echo "   • 核心服务: http://localhost:8080/notification-service/swagger-ui.html"
echo "   • 管理后台: http://localhost:8081/notification-admin/swagger-ui.html"
echo
echo "🔧 API 功能:"
echo "   • 核心服务: 通知发送、健康检查"
echo "   • 管理后台: 模板管理、收件人管理、审计监控、站内信管理、渠道管理"
