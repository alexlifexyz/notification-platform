package com.enterprise.notification.common.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 通知发送请求DTO - 共享模块
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class SendNotificationRequest {

    /**
     * 请求ID，用于幂等性控制
     */
    @NotBlank(message = "请求ID不能为空")
    private String requestId;

    /**
     * 模板代码
     */
    @NotBlank(message = "模板代码不能为空")
    private String templateCode;

    /**
     * 接收者信息
     */
    @NotNull(message = "接收者信息不能为空")
    private RecipientInfo recipient;

    /**
     * 模板参数
     */
    private Map<String, Object> templateParams;

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
