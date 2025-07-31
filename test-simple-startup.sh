#!/bin/bash

# 简单测试启动信息显示

echo "🚀 测试启动信息显示..."
echo

echo "启动核心服务 (5秒后自动停止):"
cd notification-service
timeout 5s mvn spring-boot:run 2>&1 | grep -A5 -B5 "Swagger UI" || echo "等待启动..."
cd ..

echo
echo "启动管理后台 (5秒后自动停止):"
cd notification-admin-bff  
timeout 5s mvn spring-boot:run 2>&1 | grep -A5 -B5 "Swagger UI" || echo "等待启动..."
cd ..

echo
echo "✅ 测试完成！"
echo "启动后将显示 Swagger UI 地址："
echo "• 核心服务: http://localhost:8080/notification-service/swagger-ui.html"
echo "• 管理后台: http://localhost:8081/notification-admin/swagger-ui.html"
