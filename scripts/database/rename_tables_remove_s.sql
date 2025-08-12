-- 数据库表名重命名脚本：去掉后缀s
-- 用于将现有数据库的表名从复数形式改为单数形式
-- 执行前请备份数据库！

USE notification_service;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 重命名表：去掉后缀s
-- =====================================================

-- 1. 重命名通知渠道表
RENAME TABLE `notification_channels` TO `notification_channel`;

-- 2. 重命名通知模板表  
RENAME TABLE `notification_templates` TO `notification_template`;

-- 3. 重命名通知记录表
RENAME TABLE `notifications` TO `notification`;

-- 4. 重命名收件人组表
RENAME TABLE `recipient_groups` TO `recipient_group`;

-- 5. 重命名收件人组成员表
RENAME TABLE `recipient_group_members` TO `recipient_group_member`;

-- 6. 重命名用户站内信表
RENAME TABLE `user_in_app_messages` TO `user_in_app_message`;

-- =====================================================
-- 验证重命名结果
-- =====================================================

-- 显示所有表名
SHOW TABLES;

-- 验证表结构
DESCRIBE notification_channel;
DESCRIBE notification_template;
DESCRIBE notification;
DESCRIBE recipient_group;
DESCRIBE recipient_group_member;
DESCRIBE user_in_app_message;

-- 验证数据完整性
SELECT 
    'notification_channel' AS table_name,
    COUNT(*) AS record_count
FROM notification_channel
UNION ALL
SELECT 
    'notification_template' AS table_name,
    COUNT(*) AS record_count
FROM notification_template
UNION ALL
SELECT 
    'notification' AS table_name,
    COUNT(*) AS record_count
FROM notification
UNION ALL
SELECT 
    'recipient_group' AS table_name,
    COUNT(*) AS record_count
FROM recipient_group
UNION ALL
SELECT 
    'recipient_group_member' AS table_name,
    COUNT(*) AS record_count
FROM recipient_group_member
UNION ALL
SELECT 
    'user_in_app_message' AS table_name,
    COUNT(*) AS record_count
FROM user_in_app_message;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 使用说明
-- =====================================================
/*
表名重命名完成！

重命名的表：
1. notification_channels -> notification_channel
2. notification_templates -> notification_template  
3. notifications -> notification
4. recipient_groups -> recipient_group
5. recipient_group_members -> recipient_group_member
6. user_in_app_messages -> user_in_app_message

注意事项：
1. 执行此脚本前请务必备份数据库
2. 确保应用程序已停止，避免数据不一致
3. 执行后需要重启应用程序
4. 如需回滚，请使用备份数据库

验证步骤：
1. 检查表名是否正确重命名
2. 验证表结构完整性
3. 确认数据记录数量一致
4. 测试应用程序功能正常
*/
