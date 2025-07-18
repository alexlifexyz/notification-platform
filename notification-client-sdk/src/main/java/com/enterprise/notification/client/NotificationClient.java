package com.enterprise.notification.client;

import com.enterprise.notification.common.dto.SendNotificationRequest;
import com.enterprise.notification.common.dto.SendNotificationResponse;

import java.util.Map;

/**
 * 通知客户端接口
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
public interface NotificationClient {

    /**
     * 发送通知
     *
     * @param request 发送请求
     * @return 发送响应
     */
    SendNotificationResponse sendNotification(SendNotificationRequest request);

    /**
     * 发送个人通知 - 便捷方法
     *
     * @param requestId      请求ID
     * @param templateCode   模板代码
     * @param userId         用户ID
     * @param templateParams 模板参数
     * @return 发送响应
     */
    SendNotificationResponse sendToUser(String requestId, String templateCode, String userId, Map<String, Object> templateParams);

    /**
     * 发送个人通知（带联系方式） - 便捷方法
     *
     * @param requestId      请求ID
     * @param templateCode   模板代码
     * @param userId         用户ID
     * @param userName       用户名称
     * @param phone          手机号
     * @param email          邮箱
     * @param templateParams 模板参数
     * @return 发送响应
     */
    SendNotificationResponse sendToUser(String requestId, String templateCode, String userId, 
                                       String userName, String phone, String email, 
                                       Map<String, Object> templateParams);

    /**
     * 发送组通知 - 便捷方法
     *
     * @param requestId      请求ID
     * @param templateCode   模板代码
     * @param groupCode      组代码
     * @param templateParams 模板参数
     * @return 发送响应
     */
    SendNotificationResponse sendToGroup(String requestId, String templateCode, String groupCode, Map<String, Object> templateParams);

    /**
     * 检查服务健康状态
     *
     * @return 是否健康
     */
    boolean isHealthy();
}
