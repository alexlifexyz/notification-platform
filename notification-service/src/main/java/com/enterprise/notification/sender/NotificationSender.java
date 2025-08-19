package com.enterprise.notification.sender;

import com.enterprise.notification.entity.NotificationTemplate;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 通知发送器接口
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
public interface NotificationSender {

    /**
     * 获取支持的渠道代码
     *
     * @return 渠道代码
     */
    String getSupportedChannel();

    /**
     * 发送通知
     *
     * @param template        通知模板
     * @param recipientInfo   接收者信息
     * @param renderedSubject 渲染后的主题
     * @param renderedContent 渲染后的内容
     * @param templateParams  模板参数
     * @return 发送结果
     */
    SendResult send(NotificationTemplate template, 
                   RecipientInfo recipientInfo,
                   String renderedSubject, 
                   String renderedContent,
                   Map<String, Object> templateParams);

    /**
     * 发送结果
     */
    @Getter
    @Setter
    class SendResult {
        // Getters and Setters
        private boolean success;
        private String providerCode;
        private String errorMessage;
        private Map<String, Object> metadata;

        public SendResult(boolean success, String providerCode) {
            this.success = success;
            this.providerCode = providerCode;
        }

        public SendResult(boolean success, String providerCode, String errorMessage) {
            this.success = success;
            this.providerCode = providerCode;
            this.errorMessage = errorMessage;
        }
    }

    /**
     * 接收者信息
     */
    @Setter
    @Getter
    class RecipientInfo {
        private String type;        // 接收者类型：individual 或 group
        private String userId;      // 用户ID（统一字段）
        private String userName;
        private String phone;
        private String email;
        private String imAccount;

        public RecipientInfo() {}

        public RecipientInfo(String userId, String userName, String phone, String email, String imAccount) {
            this.type = "individual";
            this.userId = userId;  // 直接使用userId字段
            this.userName = userName;
            this.phone = phone;
            this.email = email;
            this.imAccount = imAccount;
        }

    }
}
