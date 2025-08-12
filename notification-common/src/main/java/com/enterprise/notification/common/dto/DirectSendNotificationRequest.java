package com.enterprise.notification.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 直接发送通知请求DTO - 不使用模板配置
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class DirectSendNotificationRequest {

    /**
     * 请求ID，用于幂等性控制
     */
    @NotBlank(message = "请求ID不能为空")
    private String requestId;

    /**
     * 通知渠道代码列表：IN_APP, SMS, EMAIL, IM
     * 支持多个渠道同时发送
     */
    @NotEmpty(message = "通知渠道不能为空")
    private List<String> channelCodes;

    /**
     * 消息主题/标题
     */
    private String subject;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 接收者信息
     */
    @NotNull(message = "接收者信息不能为空")
    private RecipientInfo recipient;

    /**
     * 接收者信息
     */
    @Data
    public static class RecipientInfo {
        /**
         * 接收者类型：individual（个人）或 group（组）
         */
        @NotBlank(message = "接收者类型不能为空")
        private String type;

        /**
         * 接收者ID（用户ID或组代码）
         */
        @NotBlank(message = "接收者ID不能为空")
        private String id;

        /**
         * 个人接收者的联系方式（当type为individual时使用）
         */
        private ContactInfo contactInfo;
    }

    /**
     * 联系方式信息
     */
    @Data
    public static class ContactInfo {
        /**
         * 用户名称
         */
        private String userName;

        /**
         * 手机号
         */
        private String phone;

        /**
         * 邮箱
         */
        private String email;

        /**
         * IM账号
         */
        private String imAccount;
    }
}
