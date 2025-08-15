# 简化发送API文档

## 📋 概述

我们对发送接口进行了全面简化，去除了复杂的嵌套结构，提供了以下功能：

- **多用户发送支持** - 一次请求可发送给多个用户
- **扁平化参数结构** - 去除复杂的嵌套内部类
- **信息一一对应** - 用户信息封装在对象中，避免索引错乱
- **智能参数验证** - 根据渠道自动验证必要的联系方式
- **便捷构造方法** - 提供常用组合的快捷构造

## ✨ 核心优势

### 🔄 简化前后对比

| 特性 | 简化前 | 简化后 |
|------|--------|--------|
| 参数结构 | 三层嵌套 | 扁平化 |
| 多用户发送 | 需要多次调用或用户组 | 一次调用支持多用户 |
| 信息对应 | 单用户，无对应问题 | 多用户信息天然一一对应 |
| 使用复杂度 | 高 | 很低 |
| 学习成本 | 高 | 很低 |

### 📊 使用对比

**简化前（复杂嵌套）**：
```json
{
  "requestId": "old_001",
  "channelCodes": ["EMAIL"],
  "subject": "测试",
  "content": "内容",
  "recipient": {
    "type": "individual",
    "id": "user123",
    "contactInfo": {
      "userName": "张三",
      "email": "zhangsan@example.com"
    }
  }
}
```

**简化后（扁平直观）**：
```json
{
  "requestId": "new_001",
  "channelCodes": ["EMAIL"],
  "subject": "测试",
  "content": "内容",
  "users": [
    {
      "userId": "user1",
      "userName": "张三",
      "email": "zhang@example.com"
    },
    {
      "userId": "user2",
      "userName": "李四",
      "email": "li@example.com"
    }
  ]
}
```

## 🚀 API接口

### 1. 直接发送通知（简化版）

**接口**: `POST /api/v1/notifications/send-direct`

**功能**: 直接发送指定内容的通知，支持多用户发送

**请求参数**:
```json
{
  "requestId": "direct_001",
  "channelCodes": ["IN_APP", "EMAIL"],
  "subject": "重要通知",
  "content": "这是通知内容",
  "users": [
    {
      "userId": "user1",
      "userName": "张三",
      "email": "zhang@example.com"
    },
    {
      "userId": "user2",
      "userName": "李四",
      "email": "li@example.com"
    }
  ]
}
```

**组发送格式**:
```json
{
  "requestId": "group_001",
  "channelCodes": ["IN_APP"],
  "subject": "团队通知",
  "content": "团队会议通知",
  "groupCode": "DEV_TEAM"
}
```

### 2. 模板发送通知（简化版）

**接口**: `POST /api/v1/notifications/send`

**功能**: 使用预配置模板发送通知，支持多用户发送

**请求参数**:
```json
{
  "requestId": "template_001",
  "templateCode": "USER_WELCOME",
  "templateParams": {
    "welcomeMessage": "欢迎使用我们的服务！"
  },
  "users": [
    {
      "userId": "user1",
      "userName": "张三",
      "email": "zhang@example.com"
    }
  ]
}
```

### 3. 异步发送（简化版）

**接口**: 
- `POST /api/v1/notifications/send-direct-async`
- `POST /api/v1/notifications/send-async`

**功能**: 异步发送，支持多用户发送

## 📝 参数说明

### DirectSendNotificationRequest / SendNotificationRequest

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| requestId | String | 是 | 请求唯一标识，用于幂等性控制 |
| channelCodes | List<String> | 是 | 通知渠道列表：IN_APP, SMS, EMAIL, IM |
| subject | String | 否 | 消息主题/标题 |
| content | String | 是* | 消息内容（直接发送时必填） |
| templateCode | String | 是* | 模板代码（模板发送时必填） |
| templateParams | Map | 否 | 模板参数（模板发送时使用） |
| users | List<UserInfo> | 否** | 用户信息列表 |
| groupCode | String | 否** | 组代码 |

*注：content用于直接发送，templateCode用于模板发送
**注：users和groupCode必须指定其中一个

### UserInfo 对象

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | String | 是 | 用户ID |
| userName | String | 否 | 用户名称 |
| email | String | 否*** | 邮箱地址 |
| phone | String | 否*** | 手机号 |
| imAccount | String | 否*** | IM账号 |

***注：根据channelCodes自动验证必要字段
- EMAIL渠道需要email字段
- SMS渠道需要phone字段
- IM渠道需要imAccount字段

## 💡 使用示例

### 单用户发送

```bash
curl -X POST http://localhost:8080/notification-service/api/v1/notifications/send-direct \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "single_001",
    "channelCodes": ["IN_APP", "EMAIL"],
    "subject": "账户通知",
    "content": "您的账户余额不足，请及时充值。",
    "users": [
      {
        "userId": "user123",
        "userName": "张三",
        "email": "zhangsan@example.com"
      }
    ]
  }'
```

### 多用户发送（核心功能）

```bash
curl -X POST http://localhost:8080/notification-service/api/v1/notifications/send-direct \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "multi_001",
    "channelCodes": ["IN_APP", "EMAIL"],
    "subject": "系统维护通知",
    "content": "系统将于今晚22:00-24:00进行维护，请提前保存工作。",
    "users": [
      {
        "userId": "user1",
        "userName": "张三",
        "email": "zhang@example.com"
      },
      {
        "userId": "user2",
        "userName": "李四",
        "email": "li@example.com"
      },
      {
        "userId": "user3",
        "userName": "王五",
        "email": "wang@example.com"
      }
    ]
  }'
```

### 组发送

```bash
curl -X POST http://localhost:8080/notification-service/api/v1/notifications/send-direct \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "group_001",
    "channelCodes": ["IN_APP"],
    "subject": "团队通知",
    "content": "明天上午10点开会，请准时参加。",
    "groupCode": "DEV_TEAM"
  }'
```

### 模板发送

```bash
curl -X POST http://localhost:8080/notification-service/api/v1/notifications/send \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "template_001",
    "templateCode": "USER_WELCOME",
    "templateParams": {
      "userName": "新用户",
      "welcomeMessage": "欢迎加入我们！"
    },
    "users": [
      {
        "userId": "newuser001",
        "userName": "新用户",
        "email": "newuser@example.com"
      }
    ]
  }'
```

## ⚡ 便捷构造方法

UserInfo类提供了便捷的构造方法：

```java
// 常用组合：用户ID + 姓名 + 邮箱
new BaseNotificationRequest.UserInfo("user1", "张三", "zhang@example.com")

// 完整组合：用户ID + 姓名 + 邮箱 + 手机号
new BaseNotificationRequest.UserInfo("user1", "张三", "zhang@example.com", "13800138001")
```

## 🔍 智能验证

系统会根据指定的渠道自动验证用户信息：

- **EMAIL渠道** - 验证所有用户都有email字段
- **SMS渠道** - 验证所有用户都有phone字段
- **IM渠道** - 验证所有用户都有imAccount字段

验证失败时会返回详细的错误信息。

## 📊 响应格式

响应格式保持不变，多用户发送时会返回每个用户每个渠道的发送结果：

```json
{
  "requestId": "multi_001",
  "status": "SUCCESS",
  "results": [
    {
      "notificationId": 12345,
      "channelCode": "EMAIL",
      "recipientId": "user1",
      "status": "SUCCESS",
      "sentAt": "2024-07-18T10:30:00"
    },
    {
      "notificationId": 12346,
      "channelCode": "IN_APP",
      "recipientId": "user1",
      "status": "SUCCESS",
      "sentAt": "2024-07-18T10:30:01"
    }
  ],
  "processedAt": "2024-07-18T10:30:02"
}
```

## ⚠️ 注意事项

1. **参数格式** - 只支持新的简化格式，不再支持旧的嵌套格式
2. **格式互斥** - users和groupCode只能指定一个
3. **性能考虑** - 大批量用户建议分批发送（如每批50-100用户）
4. **错误处理** - 多用户发送时，部分失败不影响其他用户

## 🎉 总结

简化后的发送接口提供了：

- **极简的参数结构** - 去除了复杂的嵌套层级
- **强大的多用户发送能力** - 一次请求支持任意数量用户
- **天然的信息对应保证** - 避免了索引错乱问题
- **智能的参数验证** - 提供了更好的错误提示
- **便捷的使用方式** - 大大降低了学习和使用成本

这是一个专为外部团队设计的**极简易用**的通知发送解决方案！
