#!/bin/bash

# Swagger API 测试脚本
# 用于验证 Swagger 配置是否正确

echo "🔍 测试 Swagger API 文档配置..."
echo "=================================="

# 服务地址配置
NOTIFICATION_SERVICE_URL="http://localhost:8080/notification-service"
ADMIN_BFF_URL="http://localhost:8081/notification-admin"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试函数
test_endpoint() {
    local url=$1
    local description=$2
    
    echo -n "测试 $description: "
    
    if curl -s --connect-timeout 5 "$url" > /dev/null; then
        echo -e "${GREEN}✓ 成功${NC}"
        return 0
    else
        echo -e "${RED}✗ 失败${NC}"
        return 1
    fi
}

# 测试核心通知服务
echo -e "${YELLOW}📡 核心通知服务 (notification-service)${NC}"
echo "-----------------------------------"

test_endpoint "$NOTIFICATION_SERVICE_URL/swagger-ui.html" "Swagger UI"
test_endpoint "$NOTIFICATION_SERVICE_URL/api-docs" "API Docs"
test_endpoint "$NOTIFICATION_SERVICE_URL/api-docs.json" "API Docs JSON"
test_endpoint "$NOTIFICATION_SERVICE_URL/api/v1/notifications/health" "健康检查"

echo ""

# 测试管理后台服务
echo -e "${YELLOW}🛠️ 管理后台服务 (notification-admin-bff)${NC}"
echo "----------------------------------------"

test_endpoint "$ADMIN_BFF_URL/swagger-ui.html" "Swagger UI"
test_endpoint "$ADMIN_BFF_URL/api-docs" "API Docs"
test_endpoint "$ADMIN_BFF_URL/api-docs.json" "API Docs JSON"

echo ""

# 显示访问地址
echo -e "${YELLOW}🌐 Swagger UI 访问地址${NC}"
echo "========================"
echo "核心服务: $NOTIFICATION_SERVICE_URL/swagger-ui.html"
echo "管理后台: $ADMIN_BFF_URL/swagger-ui.html"

echo ""
echo -e "${GREEN}✅ 测试完成！${NC}"
echo ""
echo "💡 使用提示:"
echo "1. 确保两个服务都已启动"
echo "2. 在浏览器中访问上述 Swagger UI 地址"
echo "3. 可以直接在 Swagger UI 中测试 API"
echo "4. 查看 API 文档和示例"
