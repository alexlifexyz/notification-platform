package com.enterprise.notification.enums;

/**
 * 通知渠道代码枚举
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
public enum ChannelCode {
    /**
     * 站内信
     */
    IN_APP("IN_APP", "站内信"),
    
    /**
     * 短信
     */
    SMS("SMS", "短信"),
    
    /**
     * 邮件
     */
    EMAIL("EMAIL", "邮件"),
    
    /**
     * 企业IM
     */
    IM("IM", "企业IM");

    private final String code;
    private final String name;

    ChannelCode(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static ChannelCode fromCode(String code) {
        for (ChannelCode channelCode : values()) {
            if (channelCode.code.equals(code)) {
                return channelCode;
            }
        }
        throw new IllegalArgumentException("Unknown channel code: " + code);
    }
}
