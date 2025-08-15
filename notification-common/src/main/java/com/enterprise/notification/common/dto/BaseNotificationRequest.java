package com.enterprise.notification.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 通知请求基类
 * 包含公共的用户信息和验证逻辑
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public abstract class BaseNotificationRequest {

    /**
     * 请求ID，用于幂等性控制
     */
    @NotBlank(message = "请求ID不能为空")
    private String requestId;

    /**
     * 多用户发送
     * 用户信息列表，支持一次发送给多个用户
     */
    @Valid
    private List<UserInfo> users;

    /**
     * 组发送代码
     * 当需要发送给预定义的用户组时使用（与users互斥）
     */
    private String groupCode;

    /**
     * 简化的用户信息类
     * 支持多用户发送，信息一一对应
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        /**
         * 用户ID（必填）
         */
        @NotBlank(message = "用户ID不能为空")
        private String userId;

        /**
         * 用户名称（可选）
         */
        private String userName;

        /**
         * 邮箱地址（EMAIL渠道需要）
         */
        private String email;

        /**
         * 手机号（SMS渠道需要）
         */
        private String phone;

        /**
         * IM账号（IM渠道需要）
         */
        private String imAccount;

        /**
         * 便捷构造方法 - 用户ID+姓名+邮箱
         */
        public UserInfo(String userId, String userName, String email) {
            this.userId = userId;
            this.userName = userName;
            this.email = email;
        }

        /**
         * 便捷构造方法 - 用户ID+姓名+邮箱+手机号
         */
        public UserInfo(String userId, String userName, String email, String phone) {
            this.userId = userId;
            this.userName = userName;
            this.email = email;
            this.phone = phone;
        }
    }

    /**
     * 验证接收者信息
     * 确保users和groupCode不能同时为空
     */
    @AssertTrue(message = "必须指定用户列表或组代码")
    public boolean isValidRecipient() {
        return (users != null && !users.isEmpty()) ||
               (groupCode != null && !groupCode.trim().isEmpty());
    }

    /**
     * 获取用户数量
     */
    public int getUserCount() {
        return users != null ? users.size() : 0;
    }

    /**
     * 是否为多用户发送
     */
    public boolean isMultiUser() {
        return getUserCount() > 1;
    }

    /**
     * 是否为组发送
     */
    public boolean isGroupSend() {
        return groupCode != null && !groupCode.trim().isEmpty();
    }

    /**
     * 验证渠道和联系方式的匹配性
     * 子类需要提供渠道代码列表
     */
    protected boolean validateChannelContacts(List<String> channelCodes) {
        if (users == null || users.isEmpty()) {
            return true; // 组发送时不需要验证
        }

        for (UserInfo user : users) {
            // 验证EMAIL渠道
            if (channelCodes != null && channelCodes.contains("EMAIL")) {
                if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                    return false;
                }
            }

            // 验证SMS渠道
            if (channelCodes != null && channelCodes.contains("SMS")) {
                if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
                    return false;
                }
            }

            // 验证IM渠道
            if (channelCodes != null && channelCodes.contains("IM")) {
                if (user.getImAccount() == null || user.getImAccount().trim().isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }
}
