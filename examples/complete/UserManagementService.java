//package com.enterprise.user.service;

import com.enterprise.notification.client.NotificationClient;
import com.enterprise.notification.common.dto.SendNotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理服务 - 完整的通知集成示例
 * 
 * 展示了用户生命周期中的各种通知场景
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class UserManagementService {

    @Autowired
    private NotificationClient notificationClient;

    /**
     * 用户注册成功通知
     * 场景：新用户注册后发送欢迎消息
     */
    public void sendWelcomeNotification(String userId, String userName, String email, String phone) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userName", userName);
            params.put("registrationTime", java.time.LocalDateTime.now().toString());
            params.put("platformName", "企业通知平台");

            String requestId = "user_welcome_" + userId + "_" + System.currentTimeMillis();

            // 发送站内信欢迎消息
            SendNotificationResponse response = notificationClient.sendToUser(
                    requestId,
                    "USER_REGISTER_WELCOME",
                    userId,
                    userName,
                    phone,
                    email,
                    params
            );

            log.info("用户注册欢迎通知发送完成: userId={}, status={}", userId, response.getStatus());

            // 如果站内信发送成功，再发送邮件确认
            if ("SUCCESS".equals(response.getStatus())) {
                sendEmailConfirmation(userId, userName, email);
            }

        } catch (Exception e) {
            log.error("发送用户注册欢迎通知失败: userId={}", userId, e);
        }
    }

    /**
     * 发送邮件确认
     */
    private void sendEmailConfirmation(String userId, String userName, String email) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userName", userName);
            params.put("confirmationLink", "https://platform.company.com/confirm?token=" + generateToken(userId));
            params.put("expireHours", "24");

            String requestId = "email_confirm_" + userId + "_" + System.currentTimeMillis();

            SendNotificationResponse response = notificationClient.sendToUser(
                    requestId,
                    "EMAIL_CONFIRMATION",
                    userId,
                    userName,
                    null,
                    email,
                    params
            );

            log.info("邮件确认通知发送完成: userId={}, status={}", userId, response.getStatus());

        } catch (Exception e) {
            log.error("发送邮件确认通知失败: userId={}", userId, e);
        }
    }

    /**
     * 密码重置通知
     * 场景：用户申请密码重置
     */
    public void sendPasswordResetNotification(String userId, String userName, String email, String phone) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userName", userName);
            params.put("resetLink", "https://platform.company.com/reset?token=" + generateToken(userId));
            params.put("expireMinutes", "30");
            params.put("requestTime", java.time.LocalDateTime.now().toString());
            params.put("requestIp", getCurrentUserIp());

            String requestId = "password_reset_" + userId + "_" + System.currentTimeMillis();

            // 同时发送邮件和短信通知
            SendNotificationResponse emailResponse = notificationClient.sendToUser(
                    requestId + "_email",
                    "PASSWORD_RESET_EMAIL",
                    userId,
                    userName,
                    phone,
                    email,
                    params
            );

            SendNotificationResponse smsResponse = notificationClient.sendToUser(
                    requestId + "_sms",
                    "PASSWORD_RESET_SMS",
                    userId,
                    userName,
                    phone,
                    email,
                    params
            );

            log.info("密码重置通知发送完成: userId={}, emailStatus={}, smsStatus={}", 
                    userId, emailResponse.getStatus(), smsResponse.getStatus());

        } catch (Exception e) {
            log.error("发送密码重置通知失败: userId={}", userId, e);
        }
    }

    /**
     * 账户安全警告
     * 场景：检测到异常登录行为
     */
    public void sendSecurityAlert(String userId, String userName, String email, String phone, 
                                 String suspiciousActivity, String location) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userName", userName);
            params.put("activity", suspiciousActivity);
            params.put("location", location);
            params.put("time", java.time.LocalDateTime.now().toString());
            params.put("securityCenterLink", "https://platform.company.com/security");

            String requestId = "security_alert_" + userId + "_" + System.currentTimeMillis();

            // 发送紧急安全警告（多渠道）
            SendNotificationResponse response = notificationClient.sendToUser(
                    requestId,
                    "SECURITY_ALERT",
                    userId,
                    userName,
                    phone,
                    email,
                    params
            );

            log.info("安全警告通知发送完成: userId={}, status={}", userId, response.getStatus());

            // 如果是高风险活动，通知管理员
            if (isHighRiskActivity(suspiciousActivity)) {
                notifySecurityTeam(userId, userName, suspiciousActivity, location);
            }

        } catch (Exception e) {
            log.error("发送安全警告通知失败: userId={}", userId, e);
        }
    }

    /**
     * 通知安全团队
     */
    private void notifySecurityTeam(String userId, String userName, String activity, String location) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("userName", userName);
            params.put("activity", activity);
            params.put("location", location);
            params.put("time", java.time.LocalDateTime.now().toString());
            params.put("investigationLink", "https://admin.company.com/security/investigate?user=" + userId);

            String requestId = "security_team_alert_" + userId + "_" + System.currentTimeMillis();

            // 发送给安全团队组
            SendNotificationResponse response = notificationClient.sendToGroup(
                    requestId,
                    "SECURITY_TEAM_ALERT",
                    "SECURITY_TEAM",
                    params
            );

            log.info("安全团队警告发送完成: userId={}, status={}", userId, response.getStatus());

        } catch (Exception e) {
            log.error("发送安全团队警告失败: userId={}", userId, e);
        }
    }

    /**
     * 账户升级通知
     * 场景：用户账户等级提升
     */
    public void sendAccountUpgradeNotification(String userId, String userName, String email, 
                                             String oldLevel, String newLevel) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userName", userName);
            params.put("oldLevel", oldLevel);
            params.put("newLevel", newLevel);
            params.put("upgradeTime", java.time.LocalDateTime.now().toString());
            params.put("benefitsLink", "https://platform.company.com/benefits/" + newLevel.toLowerCase());
            params.put("newFeatures", getNewFeatures(newLevel));

            String requestId = "account_upgrade_" + userId + "_" + System.currentTimeMillis();

            SendNotificationResponse response = notificationClient.sendToUser(
                    requestId,
                    "ACCOUNT_UPGRADE",
                    userId,
                    userName,
                    null,
                    email,
                    params
            );

            log.info("账户升级通知发送完成: userId={}, oldLevel={}, newLevel={}, status={}", 
                    userId, oldLevel, newLevel, response.getStatus());

        } catch (Exception e) {
            log.error("发送账户升级通知失败: userId={}", userId, e);
        }
    }

    /**
     * 批量生日祝福
     * 场景：定时任务发送生日祝福
     */
    public void sendBirthdayWishes(java.util.List<UserInfo> birthdayUsers) {
        log.info("开始发送生日祝福，用户数量: {}", birthdayUsers.size());

        for (UserInfo user : birthdayUsers) {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("userName", user.getUserName());
                params.put("age", calculateAge(user.getBirthDate()));
                params.put("specialOffer", "生日专享8折优惠券");
                params.put("offerCode", generateOfferCode(user.getUserId()));
                params.put("validUntil", java.time.LocalDate.now().plusDays(7).toString());

                String requestId = "birthday_wish_" + user.getUserId() + "_" + 
                                 java.time.LocalDate.now().toString().replace("-", "");

                SendNotificationResponse response = notificationClient.sendToUser(
                        requestId,
                        "BIRTHDAY_WISHES",
                        user.getUserId(),
                        user.getUserName(),
                        user.getPhone(),
                        user.getEmail(),
                        params
                );

                log.info("生日祝福发送完成: userId={}, status={}", user.getUserId(), response.getStatus());

                // 避免发送过快
                Thread.sleep(100);

            } catch (Exception e) {
                log.error("发送生日祝福失败: userId={}", user.getUserId(), e);
            }
        }
    }

    /**
     * 健康检查和监控
     */
    public boolean checkNotificationServiceHealth() {
        try {
            boolean isHealthy = notificationClient.isHealthy();
            
            if (!isHealthy) {
                // 通知运维团队服务异常
                notifyOpsTeamServiceDown();
            }
            
            return isHealthy;
        } catch (Exception e) {
            log.error("通知服务健康检查失败", e);
            return false;
        }
    }

    /**
     * 通知运维团队服务异常
     */
    private void notifyOpsTeamServiceDown() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("serviceName", "通知服务");
            params.put("errorTime", java.time.LocalDateTime.now().toString());
            params.put("severity", "HIGH");
            params.put("monitoringLink", "https://monitor.company.com/notification-service");

            String requestId = "service_down_" + System.currentTimeMillis();

            // 这里可能需要使用备用通知方式，因为主服务可能不可用
            // 实际项目中可以集成其他告警系统
            log.error("通知服务异常，需要人工介入: requestId={}", requestId);

        } catch (Exception e) {
            log.error("通知运维团队失败", e);
        }
    }

    // 辅助方法
    private String generateToken(String userId) {
        return java.util.UUID.randomUUID().toString().replace("-", "") + userId.hashCode();
    }

    private String getCurrentUserIp() {
        // 实际实现中从请求上下文获取
        return "192.168.1.100";
    }

    private boolean isHighRiskActivity(String activity) {
        return activity.contains("异地登录") || activity.contains("多次失败") || activity.contains("权限提升");
    }

    private String getNewFeatures(String level) {
        switch (level.toUpperCase()) {
            case "VIP": return "专属客服、优先处理、专享活动";
            case "PREMIUM": return "高级功能、数据分析、API访问";
            default: return "基础功能升级";
        }
    }

    private int calculateAge(java.time.LocalDate birthDate) {
        return java.time.Period.between(birthDate, java.time.LocalDate.now()).getYears();
    }

    private String generateOfferCode(String userId) {
        return "BIRTHDAY" + userId.hashCode() % 10000;
    }

    // 用户信息类
    public static class UserInfo {
        private String userId;
        private String userName;
        private String email;
        private String phone;
        private java.time.LocalDate birthDate;

        // 构造函数和getter/setter省略
        public UserInfo(String userId, String userName, String email, String phone, java.time.LocalDate birthDate) {
            this.userId = userId;
            this.userName = userName;
            this.email = email;
            this.phone = phone;
            this.birthDate = birthDate;
        }

        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public java.time.LocalDate getBirthDate() { return birthDate; }
    }
}
