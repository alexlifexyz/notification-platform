# 快速开始

本指南将帮助您在5分钟内快速部署通知平台并发送第一条通知。

## 🎯 目标

完成本指南后，您将：
- ✅ 成功部署通知平台的核心服务
- ✅ 配置基础的通知模板
- ✅ 发送第一条通知消息
- ✅ 了解管理后台的基本功能

## 📋 环境要求

- **JDK 1.8+**
- **MySQL 8.0+** 
- **Maven 3.6+**
- **Git**

## 🚀 第一步：获取代码

```bash
# 克隆项目（如果是从GitHub）
git clone https://github.com/company/notification-platform.git
cd notification-platform

# 或者直接使用已有的项目目录
cd notification-platform
```

## 🗄️ 第二步：初始化数据库

### 1. 创建数据库
```sql
CREATE DATABASE notification_service 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

### 2. 执行初始化脚本
```bash
mysql -u root -p notification_service < database/notification_service.sql
```

### 3. 验证表结构
```sql
USE notification_service;
SHOW TABLES;
-- 应该看到6个表：
-- notification_channels, notification_templates, notifications, 
-- user_in_app_messages, recipient_groups, recipient_group_members
```

## ⚙️ 第三步：配置服务

### 1. 配置数据库连接

编辑 `notification-service/src/main/resources/application.properties`：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/notification_service?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=your_password

# 其他配置保持默认
```

编辑 `notification-admin-bff/src/main/resources/application.properties`：

```properties
# 数据库配置（与notification-service相同）
spring.datasource.url=jdbc:mysql://localhost:3306/notification_service?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=your_password

# 通知客户端配置
notification.client.base-url=http://localhost:8080/notification-service
```


## 🏗️ 第四步：构建和启动服务

### 1. 构建整个项目
```bash
mvn clean install
```

### 2. 启动核心通知服务
```bash
cd notification-service
mvn spring-boot:run
```

等待启动完成，看到类似输出：
```
Started NotificationServiceApplication in 15.234 seconds
```

### 3. 启动管理后台（新终端）
```bash
cd notification-admin-bff
mvn spring-boot:run
```

等待启动完成，看到类似输出：
```
Started NotificationAdminApplication in 12.456 seconds
```

## ✅ 第五步：验证服务状态

### 1. 检查核心服务
```bash
curl http://localhost:8080/notification-service/api/v1/notifications/health
# 应该返回: Notification Service is running
```

### 2. 检查管理后台
```bash
curl http://localhost:8081/notification-admin/api/admin/templates/query \
  -X POST \
  -H "Content-Type: application/json" \
  -d '{"current":1,"size":10}'
# 应该返回模板列表的JSON响应
```

## 📝 第六步：创建第一个通知模板

使用管理后台API创建一个简单的欢迎模板：

```bash
curl -X POST http://localhost:8081/notification-admin/api/admin/templates \
  -H "Content-Type: application/json" \
  -d '{
    "templateCode": "WELCOME_MESSAGE",
    "templateName": "欢迎消息",
    "channelCode": "IN_APP",
    "content": "欢迎${userName}使用我们的平台！",
    "isEnabled": true
  }'
```

成功后会返回创建的模板信息。

## 📨 第七步：发送第一条通知

使用核心服务API发送通知：

```bash
curl -X POST http://localhost:8080/notification-service/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "quick_start_test_001",
    "templateCode": "WELCOME_MESSAGE",
    "recipient": {
      "type": "individual",
      "id": "user_001",
      "contactInfo": {
        "userName": "张三"
      }
    },
    "templateParams": {
      "userName": "张三"
    }
  }'
```

成功响应示例：
```json
{
  "requestId": "quick_start_test_001",
  "status": "SUCCESS",
  "results": [
    {
      "notificationId": 1,
      "channelCode": "IN_APP",
      "recipientId": "user_001",
      "status": "SUCCESS",
      "sentAt": "2024-07-18T10:30:00"
    }
  ],
  "processedAt": "2024-07-18T10:30:00"
}
```

## 🎉 第八步：验证通知发送

### 1. 查看通知记录
```bash
curl -X POST http://localhost:8081/notification-admin/api/admin/notifications/query \
  -H "Content-Type: application/json" \
  -d '{
    "current": 1,
    "size": 10,
    "requestId": "quick_start_test_001"
  }'
```

### 2. 检查数据库记录
```sql
-- 查看通知记录
SELECT * FROM notifications WHERE request_id = 'quick_start_test_001';

-- 查看用户站内信
SELECT * FROM user_in_app_messages WHERE recipient_id = 'user_001';
```

## 🔧 第九步：测试管理功能

### 1. 测试模板发送功能
```bash
curl -X POST http://localhost:8081/notification-admin/api/admin/templates/test-send \
  -H "Content-Type: application/json" \
  -d '{
    "templateCode": "WELCOME_MESSAGE",
    "testRecipient": {
      "userId": "test_user",
      "userName": "测试用户"
    },
    "templateParams": {
      "userName": "测试用户"
    }
  }'
```

### 2. 查看统计信息
```bash
curl -X POST http://localhost:8081/notification-admin/api/admin/notifications/statistics \
  -H "Content-Type: application/json" \
  -d '{
    "groupBy": "day"
  }'
```

## 🎯 下一步：业务集成

现在您已经成功部署了通知平台！接下来可以：

### 1. 集成到业务系统

在您的业务项目中添加依赖：
```xml
<dependency>
    <groupId>com.enterprise</groupId>
    <artifactId>notification-client-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

配置文件：
```properties
notification.client.base-url=http://localhost:8080/notification-service
```

Java代码：
```java
@Autowired
private NotificationClient notificationClient;

// 发送通知
notificationClient.sendToUser(
    "business_req_001",
    "WELCOME_MESSAGE", 
    "user123",
    Map.of("userName", "张三")
);
```

### 2. 创建更多模板

根据业务需要创建不同类型的通知模板：
- 用户注册欢迎
- 订单状态更新  
- 密码重置通知
- 系统维护公告

### 3. 配置收件人组

为不同的团队创建收件人组：
- 开发团队
- 运维团队
- 客服团队

## 🔍 故障排查

### 常见问题

**1. 数据库连接失败**
- 检查MySQL服务是否启动
- 验证数据库用户名密码
- 确认数据库名称正确

**2. 服务启动失败**
- 检查端口是否被占用（8080, 8081）
- 查看启动日志中的错误信息
- 确认JDK版本是否正确

**3. 通知发送失败**
- 检查模板是否存在且启用
- 验证请求参数格式
- 查看服务日志中的错误信息

### 查看日志
```bash
# 核心服务日志
tail -f notification-service/logs/notification-service.log

# 管理后台日志  
tail -f notification-admin-bff/logs/notification-admin.log
```

## 📚 更多资源

- **[API文档](./03-api-reference.md)** - 完整的API参考
- **[开发指南](./04-development-guide.md)** - 详细的开发指南
- **[部署运维](./05-deployment-guide.md)** - 生产环境部署

---

**恭喜！** 您已经成功完成了通知平台的快速开始。现在可以开始探索更多高级功能了！
