# 国际化文本标识

## 📋 **通用文本**

### 基础操作
```typescript
// 操作按钮
'operation.search': '搜索' | 'Search'
'operation.reset': '重置' | 'Reset'
'operation.create': '新建' | 'Create'
'operation.edit': '编辑' | 'Edit'
'operation.delete': '删除' | 'Delete'
'operation.view': '查看' | 'View'
'operation.save': '保存' | 'Save'
'operation.cancel': '取消' | 'Cancel'
'operation.confirm': '确认' | 'Confirm'
'operation.export': '导出' | 'Export'
'operation.import': '导入' | 'Import'
'operation.refresh': '刷新' | 'Refresh'

// 状态
'status.enabled': '启用' | 'Enabled'
'status.disabled': '禁用' | 'Disabled'
'status.active': '活跃' | 'Active'
'status.inactive': '非活跃' | 'Inactive'
'status.success': '成功' | 'Success'
'status.failed': '失败' | 'Failed'
'status.pending': '处理中' | 'Pending'
'status.read': '已读' | 'Read'
'status.unread': '未读' | 'Unread'

// 时间
'time.created.at': '创建时间' | 'Created At'
'time.updated.at': '更新时间' | 'Updated At'
'time.read.at': '阅读时间' | 'Read At'
'time.sent.at': '发送时间' | 'Sent At'
'time.start.time': '开始时间' | 'Start Time'
'time.end.time': '结束时间' | 'End Time'
```

## 📝 **模板管理**

```typescript
// 页面标题
'template.management': '模板管理' | 'Template Management'
'template.create': '新建模板' | 'Create Template'
'template.edit': '编辑模板' | 'Edit Template'
'template.detail': '模板详情' | 'Template Detail'

// 字段标签
'template.code': '模板代码' | 'Template Code'
'template.name': '模板名称' | 'Template Name'
'template.subject': '主题/标题' | 'Subject/Title'
'template.content': '内容模板' | 'Content Template'
'template.third.party.code': '第三方模板代码' | 'Third Party Template Code'

// 操作
'template.test.send': '测试发送' | 'Test Send'
'template.preview': '预览' | 'Preview'
```

## 👥 **收件人组管理**

```typescript
// 页面标题
'group.management': '收件人组管理' | 'Recipient Group Management'
'group.create': '新建组' | 'Create Group'
'group.edit': '编辑组' | 'Edit Group'
'group.detail': '组详情' | 'Group Detail'

// 字段标签
'group.code': '组代码' | 'Group Code'
'group.name': '组名称' | 'Group Name'
'group.description': '组描述' | 'Group Description'
'group.member.count': '成员数量' | 'Member Count'

// 成员管理
'member.management': '成员管理' | 'Member Management'
'member.add': '添加成员' | 'Add Member'
'member.remove': '移除成员' | 'Remove Member'
'member.user.id': '用户ID' | 'User ID'
'member.user.name': '用户名称' | 'User Name'
'member.phone': '手机号' | 'Phone'
'member.email': '邮箱' | 'Email'
'member.im.account': 'IM账号' | 'IM Account'
'member.preferred.channels': '偏好渠道' | 'Preferred Channels'
```

## 👤 **收件人成员管理**

```typescript
// 页面标题
'member.management.global': '收件人成员管理' | 'Recipient Member Management'
'member.create': '新增成员' | 'Create Member'
'member.edit': '编辑成员' | 'Edit Member'
'member.detail': '成员详情' | 'Member Detail'
'member.import': '导入成员' | 'Import Members'
'member.export': '导出成员' | 'Export Members'

// 字段标签
'member.id': '成员ID' | 'Member ID'
'member.info': '成员信息' | 'Member Info'
'member.contact': '联系方式' | 'Contact Info'
'member.groups': '所属组' | 'Groups'
'member.channels': '偏好渠道' | 'Preferred Channels'
'member.status': '成员状态' | 'Member Status'
'member.join.time': '加入时间' | 'Join Time'

// 统计指标
'member.total.count': '总成员数' | 'Total Members'
'member.active.count': '活跃成员' | 'Active Members'
'member.inactive.count': '禁用成员' | 'Inactive Members'
'member.group.count': '关联组数' | 'Related Groups'

// 批量操作
'member.batch.assign.groups': '批量分配组' | 'Batch Assign Groups'
'member.batch.set.channels': '批量设置偏好渠道' | 'Batch Set Preferred Channels'
'member.batch.enable': '批量启用' | 'Batch Enable'
'member.batch.disable': '批量禁用' | 'Batch Disable'
'member.batch.delete': '批量删除' | 'Batch Delete'

// 导入导出
'member.import.file': '导入文件' | 'Import File'
'member.import.result': '导入结果' | 'Import Result'
'member.import.success': '导入成功' | 'Import Success'
'member.import.failed': '导入失败' | 'Import Failed'
'member.export.format': '导出格式' | 'Export Format'
'member.export.excel': 'Excel格式' | 'Excel Format'
'member.export.csv': 'CSV格式' | 'CSV Format'

// 操作
'member.assign.groups': '分配组' | 'Assign Groups'
'member.set.channels': '设置偏好渠道' | 'Set Preferred Channels'
'member.check.user': '检查用户' | 'Check User'

// 视图
'member.card.view': '卡片视图' | 'Card View'
'member.table.view': '表格视图' | 'Table View'

// 组操作类型
'group.operation.add': '添加到组' | 'Add to Groups'
'group.operation.remove': '从组移除' | 'Remove from Groups'
'group.operation.replace': '替换所有组' | 'Replace All Groups'
```

## 📊 **通知审计监控**

```typescript
// 页面标题
'audit.management': '通知审计监控' | 'Notification Audit'
'audit.records': '通知记录' | 'Notification Records'
'audit.statistics': '统计分析' | 'Statistics'
'audit.monitoring': '实时监控' | 'Real-time Monitoring'

// 字段标签
'audit.request.id': '请求ID' | 'Request ID'
'audit.template.info': '模板信息' | 'Template Info'
'audit.recipient': '接收者' | 'Recipient'
'audit.send.status': '发送状态' | 'Send Status'
'audit.error.message': '错误信息' | 'Error Message'

// 操作
'audit.resend': '重发' | 'Resend'
'audit.view.detail': '查看详情' | 'View Detail'
```

## 📡 **渠道管理**

```typescript
// 页面标题
'channel.management': '渠道管理' | 'Channel Management'
'channel.create': '新增渠道' | 'Create Channel'
'channel.edit': '编辑渠道' | 'Edit Channel'
'channel.config': '渠道配置' | 'Channel Configuration'

// 渠道类型
'channel.inapp': '站内信' | 'In-App'
'channel.sms': '短信' | 'SMS'
'channel.email': '邮件' | 'Email'
'channel.im': '企业IM' | 'Enterprise IM'

// 字段标签
'channel.code': '渠道代码' | 'Channel Code'
'channel.name': '渠道名称' | 'Channel Name'
'channel.status': '渠道状态' | 'Channel Status'

// 统计指标
'channel.total.count': '总渠道数' | 'Total Channels'
'channel.enabled.count': '启用渠道' | 'Enabled Channels'
'channel.disabled.count': '禁用渠道' | 'Disabled Channels'
'channel.usage.today': '今日使用量' | 'Today Usage'
'channel.sent.today': '今日发送' | 'Sent Today'
'channel.success.rate': '成功率' | 'Success Rate'
'channel.avg.latency': '平均延迟' | 'Avg Latency'

// 操作
'channel.test': '测试' | 'Test'
'channel.statistics': '统计' | 'Statistics'
'channel.configure': '配置' | 'Configure'

// 视图
'channel.card.view': '卡片视图' | 'Card View'
'channel.table.view': '表格视图' | 'Table View'
```

## 💬 **站内信管理**

```typescript
// 页面标题
'message.management': '站内信管理' | 'In-App Message Management'
'message.detail': '消息详情' | 'Message Detail'

// 字段标签
'message.id': '消息ID' | 'Message ID'
'message.user.id': '用户ID' | 'User ID'
'message.user.name': '用户名称' | 'User Name'
'message.subject': '消息主题' | 'Message Subject'
'message.content': '消息内容' | 'Message Content'
'message.read.status': '阅读状态' | 'Read Status'

// 统计指标
'message.total.count': '总消息数' | 'Total Messages'
'message.unread.count': '未读消息' | 'Unread Messages'
'message.read.count': '已读消息' | 'Read Messages'
'message.read.rate': '阅读率' | 'Read Rate'

// 操作
'message.mark.read': '标记已读' | 'Mark as Read'
'message.mark.unread': '标记未读' | 'Mark as Unread'
'message.batch.read': '批量标记已读' | 'Batch Mark as Read'
'message.batch.unread': '批量标记未读' | 'Batch Mark as Unread'
'message.batch.delete': '批量删除' | 'Batch Delete'

// 批量操作
'batch.selected': '已选择' | 'Selected'
'batch.items': '条消息' | 'messages'
```

## ⚙️ **系统设置**

```typescript
// 页面标题
'settings.management': '系统设置' | 'System Settings'
'settings.channel.config': '渠道配置' | 'Channel Configuration'
'settings.system.params': '系统参数' | 'System Parameters'
'settings.user.management': '用户管理' | 'User Management'

// 配置项
'settings.smtp.server': 'SMTP服务器' | 'SMTP Server'
'settings.sms.provider': '短信服务商' | 'SMS Provider'
'settings.im.webhook': 'IM Webhook' | 'IM Webhook'
```

## 🔔 **消息提示**

```typescript
// 成功消息
'message.success.create': '创建成功' | 'Created successfully'
'message.success.update': '更新成功' | 'Updated successfully'
'message.success.delete': '删除成功' | 'Deleted successfully'
'message.success.save': '保存成功' | 'Saved successfully'
'message.success.send': '发送成功' | 'Sent successfully'

// 确认消息
'confirm.delete': '确定要删除吗？' | 'Are you sure to delete?'
'confirm.delete.template': '确定要删除这个模板吗？' | 'Are you sure to delete this template?'
'confirm.delete.group': '确定要删除这个组吗？' | 'Are you sure to delete this group?'
'confirm.delete.message': '确定要删除这条消息吗？' | 'Are you sure to delete this message?'
'confirm.batch.delete': '确定要批量删除选中的项目吗？' | 'Are you sure to batch delete selected items?'

// 错误消息
'error.network': '网络连接失败' | 'Network connection failed'
'error.server': '服务器错误' | 'Server error'
'error.unauthorized': '未授权访问' | 'Unauthorized access'
'error.forbidden': '权限不足' | 'Insufficient permissions'
'error.not.found': '资源不存在' | 'Resource not found'
'error.template.not.found': '模板不存在' | 'Template not found'
'error.template.code.exists': '模板代码已存在' | 'Template code already exists'
'error.group.not.found': '收件人组不存在' | 'Recipient group not found'
'error.group.code.exists': '组代码已存在' | 'Group code already exists'
'error.channel.not.found': '渠道不存在' | 'Channel not found'
'error.channel.code.exists': '渠道代码已存在' | 'Channel code already exists'

// 验证消息
'validation.required': '此字段为必填项' | 'This field is required'
'validation.email.format': '请输入正确的邮箱格式' | 'Please enter a valid email format'
'validation.phone.format': '请输入正确的手机号格式' | 'Please enter a valid phone format'
'validation.code.format': '代码只能包含字母、数字和下划线' | 'Code can only contain letters, numbers and underscores'
```

## 📄 **分页和表格**

```typescript
// 分页
'pagination.total': '共 {total} 条记录' | 'Total {total} records'
'pagination.page': '第 {current}/{pages} 页' | 'Page {current} of {pages}'
'pagination.prev': '上一页' | 'Previous'
'pagination.next': '下一页' | 'Next'
'pagination.goto': '跳转到' | 'Go to'
'pagination.page.size': '每页显示' | 'Items per page'

// 表格
'table.no.data': '暂无数据' | 'No data'
'table.loading': '加载中...' | 'Loading...'
'table.select.all': '全选' | 'Select All'
'table.actions': '操作' | 'Actions'
```

## 🌐 **语言切换**

```typescript
// 语言选项
'lang.chinese': '中文' | 'Chinese'
'lang.english': 'English' | 'English'
'lang.switch': '切换语言' | 'Switch Language'
```

这份国际化文本标识文档为前端开发提供了完整的多语言支持方案，确保所有界面文本都能正确进行中英文切换。
