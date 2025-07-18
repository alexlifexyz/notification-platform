package com.enterprise.order.service;

import com.enterprise.notification.client.NotificationClient;
import com.enterprise.notification.common.dto.SendNotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 订单服务 - 使用通知客户端SDK示例
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private NotificationClient notificationClient;

    /**
     * 订单状态变更通知示例
     */
    public void notifyOrderStatusChange(String userId, String orderNo, String status) {
        try {
            // 构建模板参数
            Map<String, Object> templateParams = new HashMap<>();
            templateParams.put("orderNo", orderNo);
            templateParams.put("status", status);

            // 生成唯一请求ID
            String requestId = "order_status_" + orderNo + "_" + System.currentTimeMillis();

            // 发送通知 - 使用便捷方法
            SendNotificationResponse response = notificationClient.sendToUser(
                    requestId,
                    "ORDER_STATUS_CHANGE",
                    userId,
                    templateParams
            );

            log.info("订单状态变更通知发送完成: orderNo={}, userId={}, status={}, result={}", 
                    orderNo, userId, status, response.getStatus());

        } catch (Exception e) {
            log.error("订单状态变更通知发送失败: orderNo={}, userId={}", orderNo, userId, e);
        }
    }

    /**
     * 发送给运维团队的系统维护通知示例
     */
    public void notifySystemMaintenance(String maintenanceTime, String duration) {
        try {
            // 构建模板参数
            Map<String, Object> templateParams = new HashMap<>();
            templateParams.put("maintenanceTime", maintenanceTime);
            templateParams.put("duration", duration);

            // 生成唯一请求ID
            String requestId = "maintenance_" + UUID.randomUUID().toString().replace("-", "");

            // 发送组通知
            SendNotificationResponse response = notificationClient.sendToGroup(
                    requestId,
                    "SYSTEM_MAINTENANCE",
                    "OPS_TEAM",
                    templateParams
            );

            log.info("系统维护通知发送完成: maintenanceTime={}, result={}", 
                    maintenanceTime, response.getStatus());

        } catch (Exception e) {
            log.error("系统维护通知发送失败: maintenanceTime={}", maintenanceTime, e);
        }
    }

    /**
     * 带完整联系方式的用户注册欢迎通知示例
     */
    public void notifyUserRegistration(String userId, String userName, String phone, String email) {
        try {
            // 构建模板参数
            Map<String, Object> templateParams = new HashMap<>();
            templateParams.put("userName", userName);

            // 生成唯一请求ID
            String requestId = "user_register_" + userId + "_" + System.currentTimeMillis();

            // 发送通知 - 带联系方式
            SendNotificationResponse response = notificationClient.sendToUser(
                    requestId,
                    "USER_REGISTER_WELCOME",
                    userId,
                    userName,
                    phone,
                    email,
                    templateParams
            );

            log.info("用户注册欢迎通知发送完成: userId={}, userName={}, result={}", 
                    userId, userName, response.getStatus());

        } catch (Exception e) {
            log.error("用户注册欢迎通知发送失败: userId={}, userName={}", userId, userName, e);
        }
    }

    /**
     * 检查通知服务健康状态
     */
    public boolean checkNotificationServiceHealth() {
        try {
            boolean isHealthy = notificationClient.isHealthy();
            log.info("通知服务健康检查结果: {}", isHealthy);
            return isHealthy;
        } catch (Exception e) {
            log.error("通知服务健康检查失败", e);
            return false;
        }
    }
}
