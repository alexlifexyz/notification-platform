#!/bin/bash

# 启动服务并测试 Swagger 配置的脚本

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    启动服务并测试 Swagger${NC}"
echo -e "${BLUE}========================================${NC}"
echo

# 检查 Maven 是否可用
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven 未安装或不在 PATH 中${NC}"
    exit 1
fi

# 检查 Java 是否可用
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java 未安装或不在 PATH 中${NC}"
    exit 1
fi

echo -e "${GREEN}✅ 环境检查通过${NC}"
echo

# 编译项目
echo -e "${YELLOW}📦 编译项目...${NC}"
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ 项目编译失败${NC}"
    exit 1
fi
echo -e "${GREEN}✅ 项目编译成功${NC}"
echo

# 启动核心服务（后台运行）
echo -e "${YELLOW}🚀 启动核心通知服务...${NC}"
cd notification-service
nohup mvn spring-boot:run > ../notification-service.log 2>&1 &
NOTIFICATION_SERVICE_PID=$!
cd ..

# 等待服务启动
echo -n "等待核心服务启动"
for i in {1..30}; do
    if curl -s http://localhost:8080/notification-service/api/v1/notifications/health > /dev/null 2>&1; then
        echo -e " ${GREEN}✅ 核心服务启动成功${NC}"
        break
    fi
    echo -n "."
    sleep 2
done

if [ $i -eq 30 ]; then
    echo -e " ${RED}❌ 核心服务启动超时${NC}"
    kill $NOTIFICATION_SERVICE_PID 2>/dev/null
    exit 1
fi

# 启动管理后台服务（后台运行）
echo -e "${YELLOW}🛠️ 启动管理后台服务...${NC}"
cd notification-admin-bff
nohup mvn spring-boot:run > ../notification-admin-bff.log 2>&1 &
ADMIN_BFF_PID=$!
cd ..

# 等待服务启动
echo -n "等待管理后台服务启动"
for i in {1..30}; do
    if curl -s http://localhost:8081/notification-admin/api/admin/channels > /dev/null 2>&1; then
        echo -e " ${GREEN}✅ 管理后台服务启动成功${NC}"
        break
    fi
    echo -n "."
    sleep 2
done

if [ $i -eq 30 ]; then
    echo -e " ${RED}❌ 管理后台服务启动超时${NC}"
    kill $NOTIFICATION_SERVICE_PID 2>/dev/null
    kill $ADMIN_BFF_PID 2>/dev/null
    exit 1
fi

echo
echo -e "${GREEN}🎉 所有服务启动成功！${NC}"
echo

# 测试 Swagger 端点
echo -e "${YELLOW}🧪 测试 Swagger 端点...${NC}"
./test-swagger-endpoints.sh

echo
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}    服务信息${NC}"
echo -e "${BLUE}========================================${NC}"
echo
echo "📋 服务进程 ID:"
echo "   • 核心服务: $NOTIFICATION_SERVICE_PID"
echo "   • 管理后台: $ADMIN_BFF_PID"
echo
echo "📖 Swagger 文档地址:"
echo "   • 核心服务: http://localhost:8080/notification-service/swagger-ui.html"
echo "   • 管理后台: http://localhost:8081/notification-admin/swagger-ui.html"
echo
echo "📝 日志文件:"
echo "   • 核心服务: notification-service.log"
echo "   • 管理后台: notification-admin-bff.log"
echo
echo "🛑 停止服务:"
echo "   kill $NOTIFICATION_SERVICE_PID $ADMIN_BFF_PID"
echo
echo -e "${GREEN}✅ 测试完成！请在浏览器中访问 Swagger UI 查看 API 文档。${NC}"
