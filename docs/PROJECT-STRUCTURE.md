# 通知平台项目结构

## 多模块Maven项目结构

```
notification-platform/                          # 父项目
├── pom.xml                                     # 父项目POM，管理依赖版本和子模块
├── notification-common/                        # 共享模块
│   ├── pom.xml
│   └── src/main/java/com/enterprise/notification/common/
│       ├── dto/                                # 共享DTO
│       │   ├── SendNotificationRequest.java   # 发送请求DTO
│       │   └── SendNotificationResponse.java  # 发送响应DTO
│       └── exception/                          # 共享异常
│           └── NotificationClientException.java
├── notification-service/                       # 核心通知服务
│   ├── pom.xml                                # 依赖notification-common
│   └── src/main/java/com/enterprise/notification/
│       ├── NotificationServiceApplication.java # 启动类
│       ├── controller/                         # REST控制器
│       ├── service/                           # 业务服务
│       ├── entity/                            # 实体类
│       ├── mapper/                            # MyBatis Mapper
│       ├── sender/                            # 通知发送器
│       ├── dispatcher/                        # 通知分发器
│       └── config/                            # 配置类
├── notification-client-sdk/                   # 客户端SDK
│   ├── pom.xml                               # 依赖notification-common
│   └── src/main/java/com/enterprise/notification/client/
│       ├── NotificationClient.java           # 客户端接口
│       ├── impl/
│       │   └── NotificationClientImpl.java   # 默认实现
│       └── config/
│           ├── NotificationClientProperties.java      # 配置属性
│           └── NotificationClientAutoConfiguration.java # 自动配置
├── notification-admin-bff/                    # 管理后台BFF
│   ├── pom.xml                               # 依赖notification-common和client-sdk
│   └── src/main/java/com/enterprise/notification/admin/
│       ├── NotificationAdminApplication.java # 启动类
│       ├── controller/                       # BFF控制器
│       ├── service/                         # BFF服务
│       ├── dto/                             # BFF专用DTO
│       ├── entity/                          # 实体类（复用）
│       └── mapper/                          # 数据访问层
└── examples/                                # 使用示例
    ├── OrderService.java                    # 业务服务使用SDK示例
    └── application-order-service.properties # 配置示例
```

## 模块依赖关系

```
notification-platform (父项目)
├── notification-common (共享模块)
├── notification-service (核心服务)
│   └── depends on: notification-common
├── notification-client-sdk (客户端SDK)
│   └── depends on: notification-common
└── notification-admin-bff (管理后台)
    ├── depends on: notification-common
    └── depends on: notification-client-sdk
```

## 核心模块说明

### 1. notification-common (共享模块)
- **目的**: 避免代码重复，提供共享的DTO和异常类
- **内容**: 
  - `SendNotificationRequest/Response`: 核心API的请求响应DTO
  - `NotificationClientException`: 客户端异常类
- **被依赖**: notification-service, notification-client-sdk, notification-admin-bff

### 2. notification-service (核心通知服务)
- **目的**: 提供通知发送的核心功能
- **端口**: 8080
- **API**: `/api/v1/notifications/send`
- **特性**: 
  - 多渠道通知发送
  - 模板渲染
  - 幂等性支持
  - 完整审计日志

### 3. notification-client-sdk (客户端SDK)
- **目的**: 为业务服务提供类型安全的Java客户端
- **特性**:
  - RestTemplate封装HTTP调用
  - Spring Boot自动配置
  - 便捷方法支持
  - 异常处理和重试
- **使用**: 业务服务添加依赖即可自动注入`NotificationClient`

### 4. notification-admin-bff (管理后台BFF)
- **目的**: 为前端UI提供专门的管理API
- **端口**: 8081
- **架构**: BFF (Backend for Frontend)
- **功能**:
  - 模板管理 (已实现)
  - 接收组管理 (API设计)
  - 审计监控 (API设计)
  - 测试发送功能

## 数据库共享策略

```
MySQL Database: notification_service
├── notification-service (读写)
│   ├── 写入: notifications, user_in_app_messages
│   └── 读取: notification_templates, recipient_groups, etc.
└── notification-admin-bff (读写)
    ├── 写入: notification_templates, recipient_groups, etc.
    └── 读取: notifications (审计查询)
```

## 部署架构

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

## 开发和构建

### 构建整个项目
```bash
cd notification-platform
mvn clean install
```

### 单独构建模块
```bash
# 构建共享模块
cd notification-common && mvn clean install

# 构建SDK
cd notification-client-sdk && mvn clean install

# 构建服务
cd notification-service && mvn clean package

# 构建BFF
cd notification-admin-bff && mvn clean package
```

### 启动服务
```bash
# 启动核心服务
cd notification-service && mvn spring-boot:run

# 启动管理后台
cd notification-admin-bff && mvn spring-boot:run
```

## 版本管理

所有模块使用统一版本号，由父项目管理：
- 当前版本: 1.0.0
- 版本策略: 语义化版本控制 (Semantic Versioning)
- 发布策略: 所有模块同步发布
