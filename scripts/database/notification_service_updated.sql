-- =====================================================
-- 企业级通知平台数据库初始化脚本 (更新版本)
-- Database: notification_service
-- Version: 2.0.0
-- Author: Enterprise Team
-- 更新内容: 字段名称标准化 (created_at->create_time, updated_at->modify_time)
--          新增用户追踪字段 (create_user_id, modify_user_id)
-- =====================================================

-- 设置字符集和时区
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET time_zone = '+08:00';

-- =====================================================
-- 1. 通知渠道表 (notification_channel)
-- =====================================================
DROP TABLE IF EXISTS `notification_channel`;
CREATE TABLE `notification_channel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `channel_code` varchar(50) NOT NULL COMMENT '渠道代码：IN_APP, SMS, EMAIL, IM',
  `channel_name` varchar(100) NOT NULL COMMENT '渠道名称',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` varchar(32) DEFAULT NULL COMMENT '创建用户ID',
  `modify_user_id` varchar(32) DEFAULT NULL COMMENT '修改用户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_code` (`channel_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知渠道表';

-- =====================================================
-- 2. 通知模板表 (notification_template)
-- =====================================================
DROP TABLE IF EXISTS `notification_template`;
CREATE TABLE `notification_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_code` varchar(100) NOT NULL COMMENT '模板代码',
  `template_name` varchar(200) NOT NULL COMMENT '模板名称',
  `channel_code` varchar(50) NOT NULL COMMENT '关联渠道代码',
  `subject` varchar(500) DEFAULT NULL COMMENT '主题/标题模板',
  `content` text NOT NULL COMMENT '内容模板，支持${variable}占位符',
  `third_party_template_code` varchar(200) DEFAULT NULL COMMENT '第三方服务商模板代码（如短信模板ID）',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` varchar(32) DEFAULT NULL COMMENT '创建用户ID',
  `modify_user_id` varchar(32) DEFAULT NULL COMMENT '修改用户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_code` (`template_code`),
  KEY `idx_channel_code` (`channel_code`),
  KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- =====================================================
-- 3. 通知记录表 (notification)
-- =====================================================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `request_id` varchar(100) NOT NULL COMMENT '请求ID，用于幂等性',
  `template_code` varchar(100) NOT NULL COMMENT '使用的模板代码',
  `channel_code` varchar(50) NOT NULL COMMENT '发送渠道',
  `provider_code` varchar(50) DEFAULT NULL COMMENT '具体服务商代码',
  `recipient_type` varchar(20) NOT NULL COMMENT '接收者类型：INDIVIDUAL, GROUP',
  `recipient_id` varchar(100) NOT NULL COMMENT '接收者ID（用户ID或组代码）',
  `recipient_info` text COMMENT '接收者详细信息（JSON格式）',
  `template_params` text COMMENT '模板参数（JSON格式）',
  `rendered_subject` varchar(500) DEFAULT NULL COMMENT '渲染后的主题',
  `rendered_content` text COMMENT '渲染后的内容',
  `send_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '发送状态：PENDING, SUCCESS, FAILED',
  `error_message` text COMMENT '错误信息',
  `sent_at` datetime DEFAULT NULL COMMENT '发送时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` varchar(32) DEFAULT NULL COMMENT '创建用户ID',
  `modify_user_id` varchar(32) DEFAULT NULL COMMENT '修改用户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_request_id` (`request_id`),
  KEY `idx_template_code` (`template_code`),
  KEY `idx_channel_code` (`channel_code`),
  KEY `idx_recipient_id` (`recipient_id`),
  KEY `idx_send_status` (`send_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知记录表（发件箱/审计日志）';

-- =====================================================
-- 4. 用户站内信表 (user_in_app_message)
-- =====================================================
DROP TABLE IF EXISTS `user_in_app_message`;
CREATE TABLE `user_in_app_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `notification_id` bigint(20) DEFAULT NULL COMMENT '关联通知记录ID（逻辑外键）',
  `user_id` varchar(100) NOT NULL COMMENT '用户ID',
  `subject` varchar(500) NOT NULL COMMENT '消息主题',
  `content` text NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读：1-已读，0-未读',
  `read_at` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` varchar(32) DEFAULT NULL COMMENT '创建用户ID',
  `modify_user_id` varchar(32) DEFAULT NULL COMMENT '修改用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_notification_id` (`notification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户站内信表（收件箱）';

-- =====================================================
-- 5. 收件人组表 (recipient_group)
-- =====================================================
DROP TABLE IF EXISTS `recipient_group`;
CREATE TABLE `recipient_group` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_code` varchar(100) NOT NULL COMMENT '组代码',
  `group_name` varchar(200) NOT NULL COMMENT '组名称',
  `description` varchar(500) DEFAULT NULL COMMENT '组描述',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` varchar(32) DEFAULT NULL COMMENT '创建用户ID',
  `modify_user_id` varchar(32) DEFAULT NULL COMMENT '修改用户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_code` (`group_code`),
  KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收件人组表';

-- =====================================================
-- 6. 收件人组成员表 (recipient_group_member)
-- =====================================================
DROP TABLE IF EXISTS `recipient_group_member`;
CREATE TABLE `recipient_group_member` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `group_code` varchar(100) NOT NULL COMMENT '组代码',
  `user_id` varchar(100) NOT NULL COMMENT '用户ID',
  `user_name` varchar(200) DEFAULT NULL COMMENT '用户名称',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(200) DEFAULT NULL COMMENT '邮箱',
  `im_account` varchar(200) DEFAULT NULL COMMENT 'IM账号',
  `preferred_channels` text COMMENT '偏好渠道列表（JSON格式）',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_user_id` varchar(32) DEFAULT NULL COMMENT '创建用户ID',
  `modify_user_id` varchar(32) DEFAULT NULL COMMENT '修改用户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_code`, `user_id`),
  KEY `idx_group_code` (`group_code`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_enabled` (`is_enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收件人组成员表';

-- =====================================================
-- 初始化基础数据
-- =====================================================

-- 插入通知渠道基础数据
INSERT INTO `notification_channel` (`channel_code`, `channel_name`, `is_enabled`) VALUES
('IN_APP', '站内信', 1),
('SMS', '短信', 1),
('EMAIL', '邮件', 1),
('IM', '企业IM', 1);

-- 插入示例通知模板
INSERT INTO `notification_template` (`template_code`, `template_name`, `channel_code`, `subject`, `content`, `is_enabled`) VALUES
-- 站内信模板
('USER_REGISTER_WELCOME', '用户注册欢迎', 'IN_APP', '欢迎加入我们！', '亲爱的 ${userName}，欢迎您注册成为我们的用户！您的账号已激活，可以开始使用我们的服务了。', 1),
('ORDER_SHIPPED', '订单发货通知', 'IN_APP', '您的订单已发货', '您的订单 ${orderNo} 已发货，快递单号：${trackingNo}，预计 ${estimatedDays} 天内送达。', 1),
('ORDER_DELIVERED', '订单送达通知', 'IN_APP', '您的订单已送达', '您的订单 ${orderNo} 已成功送达，感谢您的购买！如有问题请联系客服。', 1),
('SYSTEM_MAINTENANCE', '系统维护通知', 'IN_APP', '系统维护通知', '系统将于 ${startTime} 开始维护，预计持续 ${duration}，维护期间服务可能受到影响，敬请谅解。', 1),
('ACCOUNT_SECURITY_ALERT', '账户安全提醒', 'IN_APP', '账户安全提醒', '检测到您的账户在 ${loginTime} 从 ${loginLocation} 登录，如非本人操作请及时修改密码。', 1),

-- 短信模板
('SMS_VERIFY_CODE', '短信验证码', 'SMS', NULL, '您的验证码是：${code}，5分钟内有效，请勿泄露给他人。【企业通知平台】', 1),
('SMS_ORDER_SHIPPED', '订单发货短信', 'SMS', NULL, '您的订单${orderNo}已发货，快递单号${trackingNo}，请注意查收。【企业通知平台】', 1),
('SMS_PAYMENT_SUCCESS', '支付成功短信', 'SMS', NULL, '您已成功支付${amount}元，订单号${orderNo}，感谢您的购买。【企业通知平台】', 1),

-- 邮件模板
('EMAIL_WELCOME', '欢迎邮件', 'EMAIL', '欢迎加入${companyName}', '亲爱的${userName}，\n\n欢迎您加入${companyName}！\n\n我们很高兴您选择了我们的服务。您的账户已经创建成功，现在可以开始使用我们的各项功能了。\n\n如果您有任何问题，请随时联系我们的客服团队。\n\n祝您使用愉快！\n\n${companyName}团队', 1),
('EMAIL_PASSWORD_RESET', '密码重置邮件', 'EMAIL', '密码重置通知', '您好，\n\n我们收到了您的密码重置请求。请点击以下链接重置您的密码：\n\n${resetLink}\n\n此链接将在30分钟内失效。如果您没有请求重置密码，请忽略此邮件。\n\n安全提醒：请勿将此链接分享给他人。', 1),
('EMAIL_ORDER_CONFIRMATION', '订单确认邮件', 'EMAIL', '订单确认 - ${orderNo}', '您好${userName}，\n\n感谢您的订单！\n\n订单详情：\n订单号：${orderNo}\n订单金额：${amount}元\n下单时间：${orderTime}\n\n我们将尽快为您处理订单，请耐心等待。\n\n谢谢！', 1),

-- IM模板
('IM_SYSTEM_ALERT', 'IM系统告警', 'IM', '【系统告警】${alertLevel}', '告警内容：${alertMessage}\n告警时间：${alertTime}\n影响范围：${affectedServices}\n请及时处理！', 1),
('IM_DEPLOYMENT_NOTICE', 'IM部署通知', 'IM', '【部署通知】${projectName}', '项目：${projectName}\n版本：${version}\n部署时间：${deployTime}\n部署人员：${deployer}\n状态：${status}', 1);

-- 插入示例收件人组
INSERT INTO `recipient_group` (`group_code`, `group_name`, `description`, `is_enabled`) VALUES
('ADMIN_GROUP', '系统管理员组', '系统管理员组，接收系统重要通知和安全告警', 1),
('OPS_TEAM', '运维团队', '运维团队，接收系统运维、部署、监控相关通知', 1),
('DEV_TEAM', '开发团队', '开发团队，接收开发、测试、发布相关通知', 1),
('PRODUCT_TEAM', '产品团队', '产品团队，接收产品相关通知和用户反馈', 1),
('CUSTOMER_SERVICE', '客服团队', '客服团队，接收用户投诉和服务相关通知', 1),
('ALL_EMPLOYEES', '全体员工', '全体员工组，用于公司级别的广播通知', 1),
('VIP_USERS', 'VIP用户组', 'VIP用户组，接收专属服务和优惠通知', 1),
('TEST_GROUP', '测试组', '测试组，用于测试通知功能', 1);

-- 插入示例组成员
INSERT INTO `recipient_group_member` (`group_code`, `user_id`, `user_name`, `phone`, `email`, `im_account`, `preferred_channels`, `is_enabled`) VALUES
-- 系统管理员组
('ADMIN_GROUP', 'admin001', '张三 - 系统管理员', '13800138000', 'zhangsan@company.com', 'zhangsan', '["IN_APP","EMAIL","IM"]', 1),
('ADMIN_GROUP', 'admin002', '李四 - 安全管理员', '13800138001', 'lisi@company.com', 'lisi', '["IN_APP","EMAIL","SMS","IM"]', 1),

-- 运维团队
('OPS_TEAM', 'ops001', '王五 - 运维工程师', '13800138002', 'wangwu@company.com', 'wangwu', '["IN_APP","SMS","IM"]', 1),
('OPS_TEAM', 'ops002', '赵六 - 运维工程师', '13800138003', 'zhaoliu@company.com', 'zhaoliu', '["IN_APP","SMS","IM"]', 1),
('OPS_TEAM', 'ops003', '孙七 - 运维主管', '13800138004', 'sunqi@company.com', 'sunqi', '["IN_APP","EMAIL","SMS","IM"]', 1),

-- 开发团队
('DEV_TEAM', 'dev001', '周八 - 前端工程师', '13800138005', 'zhouba@company.com', 'zhouba', '["IN_APP","EMAIL"]', 1),
('DEV_TEAM', 'dev002', '吴九 - 后端工程师', '13800138006', 'wujiu@company.com', 'wujiu', '["IN_APP","EMAIL"]', 1),
('DEV_TEAM', 'dev003', '郑十 - 架构师', '13800138007', 'zhengshi@company.com', 'zhengshi', '["IN_APP","EMAIL","IM"]', 1),
('DEV_TEAM', 'dev004', '钱一 - 测试工程师', '13800138008', 'qianyi@company.com', 'qianyi', '["IN_APP","EMAIL"]', 1),

-- 产品团队
('PRODUCT_TEAM', 'pm001', '刘二 - 产品经理', '13800138009', 'liuer@company.com', 'liuer', '["IN_APP","EMAIL","IM"]', 1),
('PRODUCT_TEAM', 'pm002', '陈三 - 产品助理', '13800138010', 'chensan@company.com', 'chensan', '["IN_APP","EMAIL"]', 1),

-- 客服团队
('CUSTOMER_SERVICE', 'cs001', '杨四 - 客服主管', '13800138011', 'yangsi@company.com', 'yangsi', '["IN_APP","EMAIL","SMS"]', 1),
('CUSTOMER_SERVICE', 'cs002', '黄五 - 客服专员', '13800138012', 'huangwu@company.com', 'huangwu', '["IN_APP","SMS"]', 1),
('CUSTOMER_SERVICE', 'cs003', '林六 - 客服专员', '13800138013', 'linliu@company.com', 'linliu', '["IN_APP","SMS"]', 1),

-- 测试组
('TEST_GROUP', 'test001', '测试用户1', '13900139001', 'test1@test.com', 'test001', '["IN_APP"]', 1),
('TEST_GROUP', 'test002', '测试用户2', '13900139002', 'test2@test.com', 'test002', '["IN_APP","EMAIL"]', 1);

-- 插入示例通知记录（用于演示和测试）
INSERT INTO `notification` (`request_id`, `template_code`, `channel_code`, `provider_code`, `recipient_type`, `recipient_id`, `recipient_info`, `template_params`, `rendered_subject`, `rendered_content`, `send_status`, `sent_at`, `create_time`) VALUES
('demo_001', 'USER_REGISTER_WELCOME', 'IN_APP', 'IN_APP_PROVIDER', 'INDIVIDUAL', 'test001', '{"userId":"test001","userName":"测试用户1"}', '{"userName":"测试用户1"}', '欢迎加入我们！', '亲爱的 测试用户1，欢迎您注册成为我们的用户！您的账号已激活，可以开始使用我们的服务了。', 'SUCCESS', NOW(), NOW()),
('demo_002', 'SYSTEM_MAINTENANCE', 'IN_APP', 'IN_APP_PROVIDER', 'GROUP', 'ALL_EMPLOYEES', '{"groupCode":"ALL_EMPLOYEES","groupName":"全体员工"}', '{"startTime":"2024-01-15 02:0user_in_app_message:00","duration":"2小时"}', '系统维护通知', '系统将于 2024-01-15 02:0user_in_app_message:00 开始维护，预计持续 2小时，维护期间服务可能受到影响，敬请谅解。', 'SUCCESS', NOW(), NOW()),
('demo_003', 'ORDER_SHIPPED', 'IN_APP', 'IN_APP_PROVIDER', 'INDIVIDUAL', 'test002', '{"userId":"test002","userName":"测试用户2"}', '{"orderNo":"ORD20240115001","trackingNo":"SF1234567890","estimatedDays":"3"}', '您的订单已发货', '您的订单 ORD20240115001 已发货，快递单号：SF1234567890，预计 3 天内送达。', 'SUCCESS', NOW(), NOW());

-- 插入示例站内信（对应上面的通知记录）
INSERT INTO `user_in_app_message` (`notification_id`, `user_id`, `subject`, `content`, `is_read`, `create_time`) VALUES
(1, 'test001', '欢迎加入我们！', '亲爱的 测试用户1，欢迎您注册成为我们的用户！您的账号已激活，可以开始使用我们的服务了。', 0, NOW()),
(3, 'test002', '您的订单已发货', '您的订单 ORD20240115001 已发货，快递单号：SF1234567890，预计 3 天内送达。', 1, NOW());

-- 为全体员工组的每个成员创建系统维护通知的站内信
INSERT INTO `user_in_app_message` (`notification_id`, `user_id`, `subject`, `content`, `is_read`, `create_time`)
SELECT 2, rgm.user_id, '系统维护通知', '系统将于 2024-01-15 02:0user_in_app_message:00 开始维护，预计持续 2小时，维护期间服务可能受到影响，敬请谅解。', 0, NOW()
FROM `recipient_group_member` rgm
WHERE rgm.group_code = 'ALL_EMPLOYEES' AND rgm.is_enabled = 1;

-- =====================================================
-- 创建性能优化索引
-- =====================================================

-- 为notification表创建复合索引，优化查询性能
CREATE INDEX `idx_notification_status_time` ON `notification` (`send_status`, `create_time`);
CREATE INDEX `idx_notification_template_time` ON `notification` (`template_code`, `create_time`);
CREATE INDEX `idx_notification_channel_time` ON `notification` (`channel_code`, `create_time`);
CREATE INDEX `idx_notification_recipient_type` ON `notification` (`recipient_type`, `recipient_id`);

-- 为user_in_app_message表创建复合索引
CREATE INDEX `idx_user_messages_user_read` ON `user_in_app_message` (`user_id`, `is_read`, `create_time`);
CREATE INDEX `idx_user_messages_user_time` ON `user_in_app_message` (`user_id`, `create_time` DESC);

-- 为recipient_group_member表创建复合索引
CREATE INDEX `idx_group_members_enabled` ON `recipient_group_member` (`group_code`, `is_enabled`);

-- =====================================================
-- 设置自增起始值
-- =====================================================
ALTER TABLE `notification_channel` AUTO_INCREMENT = 10;
ALTER TABLE `notification_template` AUTO_INCREMENT = 100;
ALTER TABLE `notification` AUTO_INCREMENT = 1000;
ALTER TABLE `user_in_app_message` AUTO_INCREMENT = 1000;
ALTER TABLE `recipient_group` AUTO_INCREMENT = 100;
ALTER TABLE `recipient_group_member` AUTO_INCREMENT = 1000;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 初始化完成统计信息
-- =====================================================

-- 显示初始化结果
SELECT '数据库初始化完成！' AS status;

SELECT
    '通知渠道' AS table_name,
    COUNT(*) AS record_count
FROM notification_channel
UNION ALL
SELECT
    '通知模板' AS table_name,
    COUNT(*) AS record_count
FROM notification_template
UNION ALL
SELECT
    '收件人组' AS table_name,
    COUNT(*) AS record_count
FROM recipient_group
UNION ALL
SELECT
    '组成员' AS table_name,
    COUNT(*) AS record_count
FROM recipient_group_member
UNION ALL
SELECT
    '示例通知记录' AS table_name,
    COUNT(*) AS record_count
FROM notification
UNION ALL
SELECT
    '示例站内信' AS table_name,
    COUNT(*) AS record_count
FROM user_in_app_message;

-- =====================================================
-- 使用说明
-- =====================================================
/*
数据库初始化完成！(更新版本 v2.0.0)

主要更新内容：
1. 字段名称标准化：
   - created_at -> create_time
   - updated_at -> modify_time
2. 新增用户追踪字段：
   - create_user_id varchar(32) - 创建用户ID
   - modify_user_id varchar(32) - 修改用户ID
3. 更新相关索引引用

包含的数据：
1. notification_channel: 4个通知渠道（站内信、短信、邮件、IM）
2. notification_template: 12个示例模板（覆盖各种业务场景）
3. recipient_group: 8个示例收件人组
4. recipient_group_member: 15个示例组成员
5. notification: 3条示例通知记录
6. user_in_app_message: 示例站内信数据

使用方法：
1. 创建数据库：
   CREATE DATABASE notification_service CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

2. 执行初始化脚本：
   mysql -u root -p notification_service < database/notification_service_updated.sql

3. 验证初始化结果：
   USE notification_service;
   SHOW TABLES;
   SELECT COUNT(*) FROM notification_channel;

字段变更说明：
- 所有表的 created_at 字段已更名为 create_time
- 所有表的 updated_at 字段已更名为 modify_time
- 所有表新增 create_user_id 字段，用于记录创建用户
- 所有表新增 modify_user_id 字段，用于记录修改用户
- 相关索引已同步更新字段引用

注意事项：
- 示例数据仅用于测试和演示
- 生产环境请根据实际需求修改组成员信息
- 建议定期备份数据库
- 新增的用户ID字段可根据实际业务需求设置值
*/
