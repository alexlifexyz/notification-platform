# 企业级通知平台

一个功能完整、架构优雅的企业级通知解决方案，提供统一的消息通知服务，支持多渠道发送、模板管理、收件人组管理和完整的监控审计功能。

## 📚 完整文档

**[📖 查看完整文档](./docs/README.md)** - 包含详细的使用指南、API文档、部署说明等

## 🎯 项目亮点

### ✨ 功能完整
- **4个通知渠道**: 站内信、短信、邮件、企业IM
- **3大管理模块**: 模板管理、收件人组管理、审计监控
- **17个API端点**: 覆盖所有管理功能的完整API

### 🏗️ 架构优雅
- **多模块设计**: 4个独立模块，职责清晰
- **SDK集成**: Java客户端SDK，零配置集成
- **BFF架构**: 管理后台采用BFF模式，API优化

### 🚀 开发友好
- **类型安全**: 强类型Java接口，编译时错误检查
- **自动配置**: Spring Boot Starter，开箱即用
- **丰富示例**: 完整的业务场景示例

## 🏗️ 系统架构

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Business      │    │   Admin UI      │    │   Mobile App    │
│   Services      │    │   (Vue/React)   │    │   (Optional)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │ SDK                   │ HTTP                  │ HTTP
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ notification-   │    │ notification-   │    │ notification-   │
│ client-sdk      │    │ admin-bff       │    │ service         │
│ (Embedded)      │    │ :8081           │    │ :8080           │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                                 ▼
                    ┌─────────────────┐
                    │     MySQL       │
                    │ notification_   │
                    │ service         │
                    └─────────────────┘
```

## 🚀 快速开始

### 1. 环境要求
- JDK 1.8+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库初始化
```bash
mysql -u root -p < database/notification_service.sql
```

### 3. 启动服务
```bash
# 构建整个项目
mvn clean install

# 启动核心服务
cd notification-service && mvn spring-boot:run

# 启动管理后台
cd notification-admin-bff && mvn spring-boot:run
```

### 4. 业务系统集成

**添加依赖**:
```xml
<dependency>
    <groupId>com.enterprise</groupId>
    <artifactId>notification-client-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

**配置**:
```properties
notification.client.base-url=http://localhost:8080/notification-service
```

**使用**:
```java
@Autowired
private NotificationClient notificationClient;

// 发送个人通知
notificationClient.sendToUser(requestId, templateCode, userId, params);

// 发送组通知
notificationClient.sendToGroup(requestId, templateCode, groupCode, params);
```

### 5. API文档访问

**Swagger UI 地址**:
- 核心服务: http://localhost:8080/notification-service/swagger-ui.html
- 管理后台: http://localhost:8081/notification-admin/swagger-ui.html

**功能特性**:
- 📖 完整的API文档
- 🧪 在线测试功能
- 🔍 搜索和过滤
- 📋 请求/响应示例

## 📊 功能模块

### 🔧 核心服务 (notification-service)
- **端口**: 8080
- **功能**: 通知发送、模板渲染、幂等性控制
- **API**: `POST /api/v1/notifications/send`

### 📦 客户端SDK (notification-client-sdk)
- **类型**: Java库
- **功能**: 类型安全的客户端、自动配置、便捷方法
- **集成**: Spring Boot Starter

### 🎛️ 管理后台 (notification-admin-bff)
- **端口**: 8081
- **功能**: 模板管理、组管理、审计监控
- **API**: 17个管理端点

### 📚 共享模块 (notification-common)
- **功能**: 共享DTO、异常类
- **作用**: 避免代码重复，保证类型一致

## 📈 核心特性

### 🎯 统一发送接口
```bash
curl -X POST http://localhost:8080/notification-service/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "req_001",
    "templateCode": "USER_WELCOME",
    "recipient": {
      "type": "individual",
      "id": "user123"
    },
    "templateParams": {
      "userName": "张三"
    }
  }'
```

### 🔧 管理后台API
```bash
# 查询模板（使用POST+RequestBody）
curl -X POST http://localhost:8081/notification-admin/api/admin/templates/query \
  -H "Content-Type: application/json" \
  -d '{
    "current": 1,
    "size": 10,
    "isEnabled": true
  }'
```

### 📊 完整监控
- 实时发送状态监控
- 多维度数据统计分析
- 失败通知重发功能
- 完整的审计日志

## 🎨 使用示例

### 用户注册场景
```java
// 发送欢迎消息
notificationClient.sendToUser(
    "user_welcome_" + userId,
    "USER_REGISTER_WELCOME", 
    userId,
    Map.of("userName", userName)
);
```

### 订单状态更新
```java
// 订单发货通知
notificationClient.sendToUser(
    "order_shipped_" + orderNo,
    "ORDER_SHIPPED",
    userId,
    Map.of("orderNo", orderNo, "trackingNo", trackingNo)
);
```

### 系统维护通知
```java
// 通知运维团队
notificationClient.sendToGroup(
    "maintenance_" + System.currentTimeMillis(),
    "SYSTEM_MAINTENANCE",
    "OPS_TEAM",
    Map.of("startTime", startTime, "duration", duration)
);
```

## 📋 API端点总览

### 核心服务API
- `POST /api/v1/notifications/send` - 发送通知
- `GET /api/v1/notifications/health` - 健康检查

### 管理后台API
- **模板管理**: 6个端点（查询、创建、更新、删除、测试）
- **组管理**: 8个端点（组CRUD + 成员CRUD）
- **审计监控**: 4个端点（记录查询、重发、统计）

## 🛠️ 技术栈

- **Java 8** + **Spring Boot 2.6.15**
- **MyBatis Plus 3.5.6** + **MySQL 8.0**
- **Maven** 多模块项目
- **RestTemplate** HTTP客户端
- **Jackson** JSON处理

## 📖 文档导航

- **[项目概述](./docs/01-overview.md)** - 了解项目背景和核心特性
- **[快速开始](./docs/02-quick-start.md)** - 5分钟快速部署指南
- **[API参考](./docs/03-api-reference.md)** - 完整的API文档
- **[Swagger文档](./docs/06-swagger-api.md)** - 在线API文档和测试 🆕
- **[使用示例](./examples/)** - 丰富的业务场景示例
- **[架构设计](./PROJECT-STRUCTURE.md)** - 详细的架构说明
- **[升级总结](./UPGRADE-SUMMARY.md)** - 升级过程和成果

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 技术支持

- **文档**: [完整文档](./docs/README.md)
- **Issues**: [GitHub Issues](https://github.com/company/notification-platform/issues)
- **邮箱**: support@company.com

---

**开始使用**: [查看完整文档](./docs/README.md) | [快速开始指南](./docs/02-quick-start.md)
