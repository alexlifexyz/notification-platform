package com.enterprise.notification.sender;

import com.enterprise.notification.entity.NotificationTemplate;

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
    class SendResult {
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

        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getProviderCode() {
            return providerCode;
        }

        public void setProviderCode(String providerCode) {
            this.providerCode = providerCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public Map<String, Object> getMetadata() {
            return metadata;
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }
    }

    /**
     * 接收者信息
     */
    class RecipientInfo {
        private String userId;
        private String userName;
        private String phone;
        private String email;
        private String imAccount;

        public RecipientInfo() {}

        public RecipientInfo(String userId, String userName, String phone, String email, String imAccount) {
            this.userId = userId;
            this.userName = userName;
            this.phone = phone;
            this.email = email;
            this.imAccount = imAccount;
        }

        // Getters and Setters
        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getImAccount() {
            return imAccount;
        }

        public void setImAccount(String imAccount) {
            this.imAccount = imAccount;
        }
    }
}
