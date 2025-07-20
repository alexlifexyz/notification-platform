# API接口规范文档

## 📋 **基础信息**

### 服务地址
- **开发环境**: http://localhost:8081/notification-admin
- **测试环境**: https://test-admin.company.com/notification-admin
- **生产环境**: https://admin.company.com/notification-admin

### 认证方式
- **类型**: Bearer Token
- **Header**: `Authorization: Bearer {token}`
- **获取方式**: 通过登录接口获取

### 通用响应格式
```json
{
  "success": true,
  "code": "1000",
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 错误响应格式
```json
{
  "success": false,
  "code": "2001",
  "message": "模板不存在",
  "data": null,
  "timestamp": "2024-01-15T10:30:00Z",
  "errors": [
    {
      "field": "templateCode",
      "message": "模板代码不能为空"
    }
  ]
}
```

## 🔧 **模板管理API**

### 1. 分页查询模板
```http
POST /api/admin/templates/query
Content-Type: application/json

{
  "current": 1,
  "size": 10,
  "templateCode": "",
  "templateName": "",
  "channelCode": "IN_APP",
  "isEnabled": true
}
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "records": [
      {
        "id": 1,
        "templateCode": "USER_REGISTER_WELCOME",
        "templateName": "用户注册欢迎",
        "channelCode": "IN_APP",
        "subject": "欢迎加入我们！",
        "content": "亲爱的 ${userName}，欢迎您注册成为我们的用户！",
        "isEnabled": true,
        "createdAt": "2024-01-15T10:30:00Z",
        "updatedAt": "2024-01-15T10:30:00Z"
      }
    ],
    "total": 25,
    "current": 1,
    "size": 10,
    "pages": 3
  }
}
```

### 2. 获取模板详情
```http
GET /api/admin/templates/{id}
```

### 3. 创建模板
```http
POST /api/admin/templates
Content-Type: application/json

{
  "templateCode": "NEW_TEMPLATE",
  "templateName": "新模板",
  "channelCode": "IN_APP",
  "subject": "主题",
  "content": "内容 ${variable}",
  "thirdPartyTemplateCode": "",
  "isEnabled": true
}
```

### 4. 更新模板
```http
PUT /api/admin/templates/{id}
Content-Type: application/json
```

### 5. 删除模板
```http
DELETE /api/admin/templates/{id}
```

### 6. 测试发送
```http
POST /api/admin/templates/test-send
Content-Type: application/json

{
  "templateCode": "USER_REGISTER_WELCOME",
  "recipient": {
    "type": "individual",
    "id": "test001"
  },
  "templateParams": {
    "userName": "测试用户"
  }
}
```

## 👥 **收件人组管理API**

### 1. 分页查询收件人组
```http
POST /api/admin/recipient-groups/query
Content-Type: application/json

{
  "current": 1,
  "size": 10,
  "groupCode": "",
  "groupName": "",
  "isEnabled": true
}
```

### 2. 获取组详情
```http
GET /api/admin/recipient-groups/{groupCode}
```

### 3. 创建收件人组
```http
POST /api/admin/recipient-groups
Content-Type: application/json

{
  "groupCode": "NEW_GROUP",
  "groupName": "新组",
  "description": "组描述",
  "isEnabled": true
}
```

### 4. 查询组成员
```http
POST /api/admin/recipient-groups/{groupCode}/members/query
Content-Type: application/json

{
  "current": 1,
  "size": 10,
  "userId": "",
  "userName": "",
  "isEnabled": true
}
```

### 5. 添加组成员
```http
POST /api/admin/recipient-groups/{groupCode}/members
Content-Type: application/json

{
  "userId": "user001",
  "userName": "用户名",
  "phone": "13800138000",
  "email": "user@company.com",
  "imAccount": "user001",
  "preferredChannels": ["IN_APP", "EMAIL"],
  "isEnabled": true
}
```

## 📊 **通知审计API**

### 1. 分页查询通知记录
```http
POST /api/admin/notifications/query
Content-Type: application/json

{
  "current": 1,
  "size": 10,
  "templateCode": "",
  "channelCode": "",
  "recipientId": "",
  "sendStatus": "SUCCESS",
  "startTime": "2024-01-15T00:00:00Z",
  "endTime": "2024-01-15T23:59:59Z"
}
```

### 2. 获取通知详情
```http
GET /api/admin/notifications/{id}
```

### 3. 重发通知
```http
POST /api/admin/notifications/resend
Content-Type: application/json

{
  "notificationId": 1,
  "reason": "重发原因"
}
```

### 4. 获取统计数据
```http
POST /api/admin/notifications/statistics
Content-Type: application/json

{
  "startTime": "2024-01-15T00:00:00Z",
  "endTime": "2024-01-15T23:59:59Z",
  "groupBy": "channel"
}
```

## 🌐 **国际化支持**

### 语言切换
- **参数**: `?lang=en` 或 `?lang=zh`
- **Header**: `Accept-Language: en-US` 或 `Accept-Language: zh-CN`

### 支持的语言
- `zh-CN`: 简体中文（默认）
- `en-US`: 英语

### 错误消息国际化
所有错误消息都支持国际化，前端根据当前语言环境显示对应的错误信息。

## 📱 **响应式设计要求**

### 断点设置
- **手机**: < 768px
- **平板**: 768px - 1024px  
- **桌面**: > 1024px

### 适配要求
1. **表格**: 小屏幕下转换为卡片布局
2. **表单**: 响应式栅格布局
3. **导航**: 移动端折叠菜单
4. **按钮**: 触摸友好的尺寸

## 🔒 **安全规范**

### 输入验证
- 所有用户输入必须进行前端验证
- 敏感操作需要二次确认
- XSS防护：对用户输入进行转义

### 权限控制
- 基于角色的访问控制
- 敏感操作需要权限验证
- 会话超时自动跳转登录

## 📈 **性能要求**

### 加载性能
- 首屏加载时间 < 2秒
- 接口响应时间 < 500ms
- 分页加载，避免一次性加载大量数据

### 用户体验
- 加载状态指示器
- 操作反馈提示
- 错误恢复机制

## 📊 **数据模型说明**

### TemplateDto - 模板数据模型
```typescript
interface TemplateDto {
  id: number;                    // 模板ID
  templateCode: string;          // 模板代码
  templateName: string;          // 模板名称
  channelCode: string;           // 渠道代码: IN_APP, SMS, EMAIL, IM
  subject?: string;              // 主题/标题（可选）
  content: string;               // 内容模板
  thirdPartyTemplateCode?: string; // 第三方模板代码（可选）
  isEnabled: boolean;            // 是否启用
  createdAt: string;             // 创建时间 (ISO 8601)
  updatedAt: string;             // 更新时间 (ISO 8601)
}
```

### RecipientGroupDto - 收件人组数据模型
```typescript
interface RecipientGroupDto {
  id: number;                    // 组ID
  groupCode: string;             // 组代码
  groupName: string;             // 组名称
  description?: string;          // 组描述（可选）
  memberCount: number;           // 成员数量
  isEnabled: boolean;            // 是否启用
  createdAt: string;             // 创建时间
  updatedAt: string;             // 更新时间
}
```

### RecipientGroupMemberDto - 组成员数据模型
```typescript
interface RecipientGroupMemberDto {
  id: number;                    // 成员ID
  groupCode: string;             // 所属组代码
  userId: string;                // 用户ID
  userName: string;              // 用户名称
  phone?: string;                // 手机号（可选）
  email?: string;                // 邮箱（可选）
  imAccount?: string;            // IM账号（可选）
  preferredChannels: string[];   // 偏好渠道列表
  isEnabled: boolean;            // 是否启用
  createdAt: string;             // 加入时间
  updatedAt: string;             // 更新时间
}
```

### NotificationRecordDto - 通知记录数据模型
```typescript
interface NotificationRecordDto {
  id: number;                    // 记录ID
  requestId: string;             // 请求ID
  templateCode: string;          // 模板代码
  channelCode: string;           // 发送渠道
  providerCode?: string;         // 服务商代码（可选）
  recipientType: 'INDIVIDUAL' | 'GROUP'; // 接收者类型
  recipientId: string;           // 接收者ID
  recipientInfo: any;            // 接收者信息（JSON）
  templateParams: any;           // 模板参数（JSON）
  renderedSubject?: string;      // 渲染后主题（可选）
  renderedContent: string;       // 渲染后内容
  sendStatus: 'PENDING' | 'SUCCESS' | 'FAILED'; // 发送状态
  errorMessage?: string;         // 错误信息（可选）
  sentAt?: string;               // 发送时间（可选）
  createdAt: string;             // 创建时间
  updatedAt: string;             // 更新时间
}
```

### PageResult - 分页结果数据模型
```typescript
interface PageResult<T> {
  records: T[];                  // 数据列表
  total: number;                 // 总记录数
  current: number;               // 当前页码
  size: number;                  // 每页大小
  pages: number;                 // 总页数
}
```

### ApiResponse - 通用响应数据模型
```typescript
interface ApiResponse<T = any> {
  success: boolean;              // 是否成功
  code: string;                  // 响应码
  message: string;               // 响应消息
  data: T;                       // 响应数据
  timestamp: string;             // 响应时间
  errors?: FieldError[];         // 字段错误列表（可选）
}

interface FieldError {
  field: string;                 // 字段名
  message: string;               // 错误消息
}
```

### NotificationChannelDto - 通知渠道数据模型
```typescript
interface NotificationChannelDto {
  id: number;                    // 渠道ID
  channelCode: string;           // 渠道代码: IN_APP, SMS, EMAIL, IM
  channelName: string;           // 渠道名称
  isEnabled: boolean;            // 是否启用
  createdAt: string;             // 创建时间
  updatedAt: string;             // 更新时间
}
```

### UserInAppMessageDto - 用户站内信数据模型
```typescript
interface UserInAppMessageDto {
  id: number;                    // 消息ID
  notificationId?: number;       // 关联通知记录ID（可选）
  userId: string;                // 用户ID
  subject: string;               // 消息主题
  content: string;               // 消息内容
  isRead: boolean;               // 是否已读
  readAt?: string;               // 阅读时间（可选）
  createdAt: string;             // 创建时间
  updatedAt: string;             // 更新时间
}
```

### ChannelStatisticsDto - 渠道统计数据模型
```typescript
interface ChannelStatisticsDto {
  totalSent: number;             // 总发送量
  successCount: number;          // 成功数量
  failedCount: number;           // 失败数量
  successRate: number;           // 成功率（百分比）
  avgLatency: number;            // 平均延迟（毫秒）
}
```

### InAppMessageStatisticsDto - 站内信统计数据模型
```typescript
interface InAppMessageStatisticsDto {
  totalMessages: number;         // 总消息数
  unreadCount: number;           // 未读数量
  readCount: number;             // 已读数量
  readRate: number;              // 阅读率（百分比）
}
```

### RecipientGroupMemberDto - 收件人组成员数据模型
```typescript
interface RecipientGroupMemberDto {
  id: number;                    // 成员ID
  groupCode: string;             // 组代码
  userId: string;                // 用户ID
  userName: string;              // 用户名称
  phone?: string;                // 手机号（可选）
  email?: string;                // 邮箱（可选）
  imAccount?: string;            // IM账号（可选）
  preferredChannels: string[];   // 偏好渠道列表
  isEnabled: boolean;            // 是否启用
  createdAt: string;             // 创建时间
  updatedAt: string;             // 更新时间
  groups?: GroupInfo[];          // 所属组信息（可选，用于跨组查询）
}

interface GroupInfo {
  groupCode: string;             // 组代码
  groupName: string;             // 组名称
  joinedAt?: string;             // 加入时间（可选）
}
```

### MemberQueryRequest - 成员查询请求数据模型
```typescript
interface MemberQueryRequest {
  current: number;               // 当前页码
  size: number;                  // 每页大小
  userId?: string;               // 用户ID（可选）
  userName?: string;             // 用户名称（可选）
  groupCode?: string;            // 组代码（可选）
  phone?: string;                // 手机号（可选）
  email?: string;                // 邮箱（可选）
  isEnabled?: boolean;           // 是否启用（可选）
}
```

### MemberCreateRequest - 成员创建请求数据模型
```typescript
interface MemberCreateRequest {
  userId: string;                // 用户ID
  userName: string;              // 用户名称
  phone?: string;                // 手机号（可选）
  email?: string;                // 邮箱（可选）
  imAccount?: string;            // IM账号（可选）
  groupCodes: string[];          // 所属组代码列表
  preferredChannels: string[];   // 偏好渠道列表
  isEnabled: boolean;            // 是否启用
}
```

### MemberUpdateRequest - 成员更新请求数据模型
```typescript
interface MemberUpdateRequest {
  userName?: string;             // 用户名称（可选）
  phone?: string;                // 手机号（可选）
  email?: string;                // 邮箱（可选）
  imAccount?: string;            // IM账号（可选）
  preferredChannels?: string[];  // 偏好渠道列表（可选）
  isEnabled?: boolean;           // 是否启用（可选）
}
```

### MemberBatchRequest - 成员批量操作请求数据模型
```typescript
interface MemberBatchRequest {
  memberIds: number[];           // 成员ID列表
}

interface MemberBatchAssignGroupsRequest extends MemberBatchRequest {
  groupCodes: string[];          // 组代码列表
  operation: 'add' | 'remove' | 'replace'; // 操作类型
}

interface MemberBatchChannelsRequest extends MemberBatchRequest {
  preferredChannels: string[];   // 偏好渠道列表
}

interface MemberBatchStatusRequest extends MemberBatchRequest {
  isEnabled: boolean;            // 是否启用
}
```

### MemberImportResult - 成员导入结果数据模型
```typescript
interface MemberImportResult {
  totalCount: number;            // 总记录数
  successCount: number;          // 成功数量
  failedCount: number;           // 失败数量
  failedRecords: ImportFailedRecord[]; // 失败记录列表
}

interface ImportFailedRecord {
  row: number;                   // 行号
  userId: string;                // 用户ID
  error: string;                 // 错误信息
}
```

### MemberStatisticsDto - 成员统计数据模型
```typescript
interface MemberStatisticsDto {
  totalMembers: number;          // 总成员数
  activeMembers: number;         // 活跃成员数
  inactiveMembers: number;       // 非活跃成员数
  groupCount: number;            // 关联组数
  channelDistribution: Record<string, number>; // 渠道分布
}
```

## 👤 **收件人成员管理API**

### 1. 分页查询成员
```http
POST /api/admin/members/query
Content-Type: application/json

{
  "current": 1,
  "size": 10,
  "userId": "",
  "userName": "",
  "groupCode": "",
  "phone": "",
  "email": "",
  "isEnabled": null
}
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "records": [
      {
        "id": 1,
        "groupCode": "ADMIN_GROUP",
        "userId": "admin001",
        "userName": "张三 - 系统管理员",
        "phone": "13800138000",
        "email": "zhangsan@company.com",
        "imAccount": "zhangsan",
        "preferredChannels": ["IN_APP", "EMAIL", "IM"],
        "isEnabled": true,
        "createdAt": "2024-01-15T10:30:00Z",
        "updatedAt": "2024-01-15T10:30:00Z",
        "groups": [
          {
            "groupCode": "ADMIN_GROUP",
            "groupName": "系统管理员组"
          },
          {
            "groupCode": "ALL_EMPLOYEES",
            "groupName": "全体员工"
          }
        ]
      }
    ],
    "total": 1256,
    "current": 1,
    "size": 10,
    "pages": 126
  }
}
```

### 2. 获取成员详情
```http
GET /api/admin/members/{id}
```

### 3. 创建成员
```http
POST /api/admin/members
Content-Type: application/json

{
  "userId": "new001",
  "userName": "新用户",
  "phone": "13800138999",
  "email": "newuser@company.com",
  "imAccount": "newuser",
  "groupCodes": ["DEV_TEAM", "ALL_EMPLOYEES"],
  "preferredChannels": ["IN_APP", "EMAIL"],
  "isEnabled": true
}
```

### 4. 更新成员
```http
PUT /api/admin/members/{id}
Content-Type: application/json

{
  "userName": "更新后的用户名",
  "phone": "13800138888",
  "email": "updated@company.com",
  "imAccount": "updated",
  "preferredChannels": ["IN_APP", "SMS", "EMAIL"],
  "isEnabled": true
}
```

### 5. 删除成员
```http
DELETE /api/admin/members/{id}
```

### 6. 批量删除成员
```http
DELETE /api/admin/members/batch
Content-Type: application/json

{
  "memberIds": [1, 2, 3]
}
```

### 7. 批量分配组
```http
POST /api/admin/members/batch/assign-groups
Content-Type: application/json

{
  "memberIds": [1, 2, 3],
  "groupCodes": ["DEV_TEAM", "ALL_EMPLOYEES"],
  "operation": "add"  // add: 添加到组, remove: 从组移除, replace: 替换所有组
}
```

### 8. 批量设置偏好渠道
```http
POST /api/admin/members/batch/preferred-channels
Content-Type: application/json

{
  "memberIds": [1, 2, 3],
  "preferredChannels": ["IN_APP", "EMAIL", "SMS"]
}
```

### 9. 批量更新状态
```http
POST /api/admin/members/batch/status
Content-Type: application/json

{
  "memberIds": [1, 2, 3],
  "isEnabled": true
}
```

### 10. 导入成员
```http
POST /api/admin/members/import
Content-Type: multipart/form-data

file: [Excel/CSV文件]
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "totalCount": 100,
    "successCount": 95,
    "failedCount": 5,
    "failedRecords": [
      {
        "row": 10,
        "userId": "invalid001",
        "error": "用户ID格式不正确"
      }
    ]
  }
}
```

### 11. 导出成员
```http
POST /api/admin/members/export
Content-Type: application/json

{
  "userId": "",
  "userName": "",
  "groupCode": "",
  "isEnabled": null,
  "format": "excel"  // excel 或 csv
}
```

### 12. 获取成员统计
```http
GET /api/admin/members/statistics
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "totalMembers": 1256,
    "activeMembers": 1198,
    "inactiveMembers": 58,
    "groupCount": 8,
    "channelDistribution": {
      "IN_APP": 1256,
      "EMAIL": 1100,
      "SMS": 800,
      "IM": 600
    }
  }
}
```

### 13. 获取用户的所属组
```http
GET /api/admin/members/user/{userId}/groups
```

**响应示例**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "groupCode": "ADMIN_GROUP",
      "groupName": "系统管理员组",
      "joinedAt": "2024-01-15T10:30:00Z"
    },
    {
      "id": 2,
      "groupCode": "ALL_EMPLOYEES",
      "groupName": "全体员工",
      "joinedAt": "2024-01-15T10:30:00Z"
    }
  ]
}
```

### 14. 检查用户ID是否存在
```http
GET /api/admin/members/check-user/{userId}
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "exists": true,
    "memberCount": 2,  // 该用户在多少个组中
    "groups": ["ADMIN_GROUP", "ALL_EMPLOYEES"]
  }
}
```
```

## 📡 **渠道管理API**

### 1. 查询渠道列表
```http
GET /api/admin/channels
```

**响应示例**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "channelCode": "IN_APP",
      "channelName": "站内信",
      "isEnabled": true,
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-15T10:30:00Z"
    }
  ]
}
```

### 2. 获取渠道详情
```http
GET /api/admin/channels/{id}
```

### 3. 创建渠道
```http
POST /api/admin/channels
Content-Type: application/json

{
  "channelCode": "WECHAT",
  "channelName": "微信",
  "isEnabled": true
}
```

### 4. 更新渠道
```http
PUT /api/admin/channels/{id}
Content-Type: application/json

{
  "channelName": "微信通知",
  "isEnabled": false
}
```

### 5. 删除渠道
```http
DELETE /api/admin/channels/{id}
```

### 6. 获取渠道统计
```http
GET /api/admin/channels/{id}/statistics?startTime=2024-01-15T00:00:00Z&endTime=2024-01-15T23:59:59Z
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "totalSent": 856,
    "successCount": 849,
    "failedCount": 7,
    "successRate": 99.2,
    "avgLatency": 2
  }
}
```

## 💬 **站内信管理API**

### 1. 分页查询站内信
```http
POST /api/admin/in-app-messages/query
Content-Type: application/json

{
  "current": 1,
  "size": 10,
  "userId": "",
  "subject": "",
  "isRead": null,
  "startTime": "2024-01-15T00:00:00Z",
  "endTime": "2024-01-15T23:59:59Z"
}
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "records": [
      {
        "id": 1,
        "notificationId": 2001,
        "userId": "user123",
        "subject": "欢迎加入我们！",
        "content": "亲爱的 张三，欢迎您注册成为我们的用户！",
        "isRead": false,
        "readAt": null,
        "createdAt": "2024-01-15T10:30:15Z",
        "updatedAt": "2024-01-15T10:30:15Z"
      }
    ],
    "total": 2456,
    "current": 1,
    "size": 10,
    "pages": 246
  }
}
```

### 2. 获取站内信详情
```http
GET /api/admin/in-app-messages/{id}
```

### 3. 标记已读/未读
```http
PUT /api/admin/in-app-messages/{id}/read-status
Content-Type: application/json

{
  "isRead": true
}
```

### 4. 批量标记已读/未读
```http
PUT /api/admin/in-app-messages/batch/read-status
Content-Type: application/json

{
  "messageIds": [1, 2, 3],
  "isRead": true
}
```

### 5. 删除站内信
```http
DELETE /api/admin/in-app-messages/{id}
```

### 6. 批量删除站内信
```http
DELETE /api/admin/in-app-messages/batch
Content-Type: application/json

{
  "messageIds": [1, 2, 3]
}
```

### 7. 获取站内信统计
```http
GET /api/admin/in-app-messages/statistics?startTime=2024-01-15T00:00:00Z&endTime=2024-01-15T23:59:59Z
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "totalMessages": 2456,
    "unreadCount": 234,
    "readCount": 2222,
    "readRate": 90.5
  }
}
```
