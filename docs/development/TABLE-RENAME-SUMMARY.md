# 数据库表名重命名总结

## 📋 修改概述

为了统一数据库表命名规范，将所有表名从复数形式改为单数形式，去掉后缀`s`。

## 🔄 表名映射

| 原表名（复数） | 新表名（单数） |
|---------------|---------------|
| `notification_channels` | `notification_channel` |
| `notification_templates` | `notification_template` |
| `notifications` | `notification` |
| `recipient_groups` | `recipient_group` |
| `recipient_group_members` | `recipient_group_member` |
| `user_in_app_messages` | `user_in_app_message` |

## 📁 修改的文件清单

### 1. SQL脚本文件
- ✅ `scripts/database/notification_service.sql` - 主数据库脚本
- ✅ `scripts/database/notification_service_updated.sql` - 更新版数据库脚本
- ✅ `scripts/database/fix_multi_channel_constraint.sql` - 多渠道约束修复脚本
- ✅ `scripts/database/rename_tables_remove_s.sql` - 新增：表重命名脚本

### 2. Java实体类 (@TableName注解)
**notification-service模块：**
- ✅ `entity/Notification.java`
- ✅ `entity/NotificationChannel.java`
- ✅ `entity/NotificationTemplate.java`
- ✅ `entity/RecipientGroup.java`
- ✅ `entity/RecipientGroupMember.java`
- ✅ `entity/UserInAppMessage.java`

**notification-admin-bff模块：**
- ✅ `admin/entity/Notification.java`
- ✅ `admin/entity/NotificationChannel.java`
- ✅ `admin/entity/NotificationTemplate.java`
- ✅ `admin/entity/RecipientGroup.java`
- ✅ `admin/entity/RecipientGroupMember.java`
- ✅ `admin/entity/UserInAppMessage.java`

### 3. MyBatis XML文件
- ✅ `notification-admin-bff/src/main/resources/mapper/NotificationMapper.xml`

### 4. 脚本和配置文件
- ✅ `scripts/database-manager.sh` - 数据库管理脚本
- ✅ `scripts/fix-table-names.sh` - 新增：批量修改脚本

## 🚀 部署步骤

### 对于新部署
直接使用更新后的SQL脚本即可：
```bash
./scripts/database-manager.sh init
```

### 对于现有系统
1. **备份数据库**：
   ```bash
   ./scripts/database-manager.sh backup
   ```

2. **停止应用服务**：
   ```bash
   # 停止所有相关服务
   ```

3. **执行表重命名**：
   ```bash
   ./scripts/database-manager.sh rename-tables
   ```

4. **重新部署应用**：
   ```bash
   # 部署更新后的应用代码
   ```

5. **验证功能**：
   ```bash
   ./scripts/run-tests.sh all
   ```

## ✅ 验证检查清单

### 数据库层面
- [ ] 表名已正确重命名
- [ ] 表结构完整无损
- [ ] 数据记录数量一致
- [ ] 索引和约束正常

### 应用层面
- [ ] 实体类@TableName注解已更新
- [ ] MyBatis XML中的表名已更新
- [ ] 应用启动无错误
- [ ] 基本功能测试通过

### 测试验证
- [ ] 通知发送功能正常
- [ ] 模板管理功能正常
- [ ] 收件人组管理正常
- [ ] 站内信功能正常
- [ ] 多渠道发送正常
- [ ] 幂等性功能正常

## 🔧 工具脚本

### 数据库管理
```bash
# 检查数据库状态
./scripts/database-manager.sh status

# 重命名表（现有系统）
./scripts/database-manager.sh rename-tables

# 备份数据库
./scripts/database-manager.sh backup
```

### 功能测试
```bash
# 运行所有测试
./scripts/run-tests.sh all

# 运行特定测试
./scripts/run-tests.sh direct-send
./scripts/run-tests.sh multi-channel
```

## ⚠️ 注意事项

1. **备份重要性**：执行表重命名前务必备份数据库
2. **服务停止**：重命名期间必须停止应用服务
3. **原子操作**：表重命名脚本使用事务确保原子性
4. **回滚准备**：保留备份文件以便必要时回滚
5. **测试验证**：重命名后必须进行完整功能测试

## 🐛 故障排除

### 常见问题

1. **表名冲突**
   ```sql
   -- 检查是否存在同名表
   SHOW TABLES LIKE 'notification%';
   ```

2. **外键约束错误**
   ```sql
   -- 临时禁用外键检查
   SET FOREIGN_KEY_CHECKS = 0;
   -- 执行重命名
   -- 恢复外键检查
   SET FOREIGN_KEY_CHECKS = 1;
   ```

3. **应用启动失败**
   - 检查@TableName注解是否正确更新
   - 检查MyBatis XML中的表名引用
   - 查看应用日志确定具体错误

### 回滚方案
如需回滚到原表名：
```bash
# 使用备份恢复数据库
mysql -u root -p notification_service < backup/notification_service_backup.sql

# 或者手动重命名回原表名
RENAME TABLE notification TO notifications;
# ... 其他表类似操作
```

## 📊 影响评估

### 正面影响
- ✅ 统一了表命名规范
- ✅ 提高了代码可读性
- ✅ 符合单数命名约定
- ✅ 便于后续维护

### 风险控制
- ✅ 提供了完整的回滚方案
- ✅ 创建了自动化脚本
- ✅ 包含了详细的验证步骤
- ✅ 保持了数据完整性

## 📝 总结

表名重命名工作已完成，涉及6张表的重命名和相关代码的同步更新。通过提供的脚本和文档，可以安全地在现有系统中执行此变更，同时确保数据完整性和功能正常性。
