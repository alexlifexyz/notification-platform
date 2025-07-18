# 通知管理后台 BFF API 文档

## 概述

通知管理后台采用 BFF (Backend for Frontend) 架构，为前端UI提供专门的、聚合的API。BFF服务直接连接到与notification-service相同的数据库，并操作相关表。

## 基础信息

- **服务地址**: `http://localhost:8081/notification-admin`
- **API前缀**: `/api/admin`
- **认证方式**: 假设已登录（简化实现）

## API 端点列表

### 1. 模板管理模块 (已实现)

| HTTP方法 | URL | 功能描述 | 请求参数 | 响应格式 |
|---------|-----|---------|---------|---------|
| GET | `/api/admin/templates` | 分页查询模板列表 | current, size, templateCode, templateName, channelCode, isEnabled | PageResult<TemplateDto> |
| GET | `/api/admin/templates/{id}` | 获取单个模板详情 | id (路径参数) | TemplateDto |
| POST | `/api/admin/templates` | 创建新模板 | TemplateCreateRequest | TemplateDto |
| PUT | `/api/admin/templates/{id}` | 更新模板 | id (路径参数), TemplateUpdateRequest | TemplateDto |
| DELETE | `/api/admin/templates/{id}` | 删除模板 | id (路径参数) | 无内容 |
| POST | `/api/admin/templates/test-send` | 测试发送模板 | TemplateTestSendRequest | SendNotificationResponse |

#### 模板管理 API 详细说明

**1. 分页查询模板列表**
```
GET /api/admin/templates?current=1&size=10&templateCode=USER&isEnabled=true
```

**2. 创建模板**
```json
POST /api/admin/templates
{
  "templateCode": "ORDER_SHIPPED",
  "templateName": "订单发货通知",
  "channelCode": "SMS",
  "subject": null,
  "content": "您的订单${orderNo}已发货，预计${deliveryTime}送达。",
  "thirdPartyTemplateCode": "SMS_123456",
  "isEnabled": true
}
```

**3. 测试发送模板**
```json
POST /api/admin/templates/test-send
{
  "templateCode": "ORDER_SHIPPED",
  "testRecipient": {
    "userId": "test_user_001",
    "userName": "测试用户",
    "phone": "13800138000",
    "email": "test@example.com"
  },
  "templateParams": {
    "orderNo": "ORD20240718001",
    "deliveryTime": "明天下午"
  }
}
```

### 2. 接收组管理模块 (API设计)

| HTTP方法 | URL | 功能描述 | 请求参数 | 响应格式 |
|---------|-----|---------|---------|---------|
| GET | `/api/admin/recipient-groups` | 分页查询接收组 | current, size, groupCode, groupName, isEnabled | PageResult<RecipientGroupDto> |
| GET | `/api/admin/recipient-groups/{groupCode}` | 获取接收组详情 | groupCode (路径参数) | RecipientGroupDto |
| POST | `/api/admin/recipient-groups` | 创建接收组 | RecipientGroupCreateRequest | RecipientGroupDto |
| PUT | `/api/admin/recipient-groups/{groupCode}` | 更新接收组信息 | groupCode (路径参数), RecipientGroupUpdateRequest | RecipientGroupDto |
| DELETE | `/api/admin/recipient-groups/{groupCode}` | 删除接收组 | groupCode (路径参数) | 无内容 |
| GET | `/api/admin/recipient-groups/{groupCode}/members` | 查询组成员 | groupCode (路径参数), current, size | PageResult<RecipientGroupMemberDto> |
| POST | `/api/admin/recipient-groups/{groupCode}/members` | 添加组成员 | groupCode (路径参数), RecipientGroupMemberCreateRequest | RecipientGroupMemberDto |
| PUT | `/api/admin/recipient-groups/{groupCode}/members/{userId}` | 更新组成员 | groupCode, userId (路径参数), RecipientGroupMemberUpdateRequest | RecipientGroupMemberDto |
| DELETE | `/api/admin/recipient-groups/{groupCode}/members/{userId}` | 删除组成员 | groupCode, userId (路径参数) | 无内容 |

#### 接收组管理示例

**创建接收组**
```json
POST /api/admin/recipient-groups
{
  "groupCode": "SALES_TEAM",
  "groupName": "销售团队",
  "description": "负责销售业务的团队成员",
  "isEnabled": true
}
```

**添加组成员**
```json
POST /api/admin/recipient-groups/SALES_TEAM/members
{
  "userId": "sales001",
  "userName": "张销售",
  "phone": "13800138001",
  "email": "sales001@company.com",
  "imAccount": "zhangxiaoshou",
  "preferredChannels": ["SMS", "EMAIL", "IM"],
  "isEnabled": true
}
```

### 3. 审计与监控模块 (API设计)

| HTTP方法 | URL | 功能描述 | 请求参数 | 响应格式 |
|---------|-----|---------|---------|---------|
| GET | `/api/admin/notifications` | 分页查询发送记录 | current, size, requestId, templateCode, recipientId, status, startTime, endTime | PageResult<NotificationRecordDto> |
| GET | `/api/admin/notifications/{id}` | 获取发送记录详情 | id (路径参数) | NotificationRecordDto |
| POST | `/api/admin/notifications/{id}/resend` | 重发失败通知 | id (路径参数) | SendNotificationResponse |
| GET | `/api/admin/notifications/statistics` | 获取发送统计 | startTime, endTime, groupBy | NotificationStatisticsDto |

#### 审计监控示例

**查询发送记录**
```
GET /api/admin/notifications?current=1&size=20&templateCode=ORDER_STATUS&status=FAILED&startTime=2024-07-18T00:00:00&endTime=2024-07-18T23:59:59
```

**重发失败通知**
```json
POST /api/admin/notifications/12345/resend
{
  "reason": "网络异常导致发送失败，现在重新发送"
}
```

## 数据传输对象 (DTOs)

### TemplateDto
```json
{
  "id": 1,
  "templateCode": "ORDER_STATUS_CHANGE",
  "templateName": "订单状态变更",
  "channelCode": "SMS",
  "channelName": "短信",
  "subject": null,
  "content": "您的订单${orderNo}状态已变更为${status}",
  "thirdPartyTemplateCode": "SMS_123456",
  "isEnabled": true,
  "createdAt": "2024-07-18T10:00:00",
  "updatedAt": "2024-07-18T10:00:00"
}
```

### PageResult<T>
```json
{
  "current": 1,
  "size": 10,
  "total": 100,
  "pages": 10,
  "records": [...]
}
```

### NotificationRecordDto
```json
{
  "id": 12345,
  "requestId": "req_20240718_001",
  "templateCode": "ORDER_STATUS_CHANGE",
  "channelCode": "SMS",
  "providerCode": "ALIYUN_SMS",
  "recipientType": "INDIVIDUAL",
  "recipientId": "user123",
  "recipientInfo": {...},
  "templateParams": {...},
  "renderedContent": "您的订单ORD001状态已变更为已发货",
  "sendStatus": "SUCCESS",
  "errorMessage": null,
  "sentAt": "2024-07-18T10:30:00",
  "createdAt": "2024-07-18T10:30:00"
}
```

## 错误处理

所有API遵循统一的错误响应格式：

```json
{
  "timestamp": "2024-07-18T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "模板代码已存在: ORDER_STATUS_CHANGE",
  "path": "/api/admin/templates"
}
```

## 认证与授权

当前版本简化了认证实现，假设用户已登录。生产环境中应该集成Spring Security或其他认证框架。

## 使用示例

### cURL 示例

```bash
# 查询模板列表
curl -X GET "http://localhost:8081/notification-admin/api/admin/templates?current=1&size=10"

# 创建模板
curl -X POST http://localhost:8081/notification-admin/api/admin/templates \
  -H "Content-Type: application/json" \
  -d '{
    "templateCode": "NEW_TEMPLATE",
    "templateName": "新模板",
    "channelCode": "IN_APP",
    "content": "这是一个新的${type}模板",
    "isEnabled": true
  }'

# 测试发送
curl -X POST http://localhost:8081/notification-admin/api/admin/templates/test-send \
  -H "Content-Type: application/json" \
  -d '{
    "templateCode": "NEW_TEMPLATE",
    "testRecipient": {
      "userId": "test001",
      "userName": "测试用户"
    },
    "templateParams": {
      "type": "测试"
    }
  }'
```
