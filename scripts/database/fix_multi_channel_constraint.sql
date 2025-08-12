-- 修复多渠道发送时的唯一约束问题
-- 将 request_id 的唯一约束改为 (request_id, channel_code) 的组合唯一约束

USE notification_service;

-- 删除原有的 request_id 唯一约束
ALTER TABLE `notification` DROP INDEX `uk_request_id`;

-- 添加新的组合唯一约束：同一个 request_id 在不同渠道下可以有多条记录
ALTER TABLE `notification` ADD UNIQUE KEY `uk_request_channel` (`request_id`, `channel_code`);

-- 验证约束修改
SHOW INDEX FROM `notification` WHERE Key_name LIKE 'uk_%';

-- 说明：
-- 修改后，同一个 request_id 可以在不同的 channel_code 下创建多条记录
-- 这样支持多渠道发送，同时保持幂等性（同一 request_id + channel_code 组合不会重复）
