package com.enterprise.notification.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 模板测试发送请求DTO
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Data
public class TemplateTestSendRequest {

    /**
     * 模板代码
     */
    @NotBlank(message = "模板代码不能为空")
    private String templateCode;

    /**
     * 测试接收者信息
     */
    @NotNull(message = "测试接收者信息不能为空")
    private TestRecipient testRecipient;

    /**
     * 模板参数
     */
    private Map<String, Object> templateParams;

    /**
     * 测试接收者信息
     */
    @Data
    public static class TestRecipient {
        /**
         * 用户ID
         */
        @NotBlank(message = "用户ID不能为空")
        private String userId;

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
