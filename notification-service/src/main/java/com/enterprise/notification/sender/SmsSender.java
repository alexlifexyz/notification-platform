package com.enterprise.notification.sender;

import com.enterprise.notification.config.NotificationProperties;
import com.enterprise.notification.entity.NotificationTemplate;
import com.enterprise.notification.enums.ChannelCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 短信发送器
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class SmsSender implements NotificationSender {

    @Autowired
    private NotificationProperties notificationProperties;

    @Autowired
    private ObjectMapper objectMapper;

    private final WebClient webClient;
    private final Pattern phonePattern = Pattern.compile("^1[3-9]\\d{9}$");

    public SmsSender() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String getSupportedChannel() {
        return ChannelCode.SMS.getCode();
    }

    @Override
    public SendResult send(NotificationTemplate template,
                          RecipientInfo recipientInfo,
                          String renderedSubject,
                          String renderedContent,
                          Map<String, Object> templateParams) {

        try {
            log.info("开始发送短信: phone={}, content={}",
                    recipientInfo.getPhone(), renderedContent);

            // 验证接收者信息
            if (!StringUtils.hasText(recipientInfo.getPhone())) {
                return new SendResult(false, "SMS", "手机号不能为空");
            }

            if (!isValidPhoneNumber(recipientInfo.getPhone())) {
                return new SendResult(false, "SMS", "手机号格式不正确");
            }

            if (!StringUtils.hasText(renderedContent)) {
                return new SendResult(false, "SMS", "短信内容不能为空");
            }

            // 选择可用的短信服务商
            String providerCode = selectSmsProvider();
            if (providerCode == null) {
                return new SendResult(false, "SMS", "没有可用的短信服务商");
            }

            // 发送短信
            boolean sendSuccess = sendSms(providerCode, recipientInfo.getPhone(),
                                        renderedContent, template.getThirdPartyTemplateCode(), templateParams);

            if (sendSuccess) {
                log.info("短信发送成功: phone={}, provider={}",
                        recipientInfo.getPhone(), providerCode);
                return new SendResult(true, providerCode);
            } else {
                log.error("短信发送失败: phone={}, provider={}",
                         recipientInfo.getPhone(), providerCode);
                return new SendResult(false, providerCode, "短信发送失败");
            }

        } catch (Exception e) {
            log.error("短信发送异常: phone={}", recipientInfo.getPhone(), e);
            return new SendResult(false, "SMS", "发送异常: " + e.getMessage());
        }
    }

    /**
     * 选择可用的短信服务商
     */
    private String selectSmsProvider() {
        NotificationProperties.Sms smsConfig = notificationProperties.getProviders().getSms();
        
        if (smsConfig.getAliyun().isEnabled()) {
            return "ALIYUN_SMS";
        }
        
        if (smsConfig.getTencent().isEnabled()) {
            return "TENCENT_SMS";
        }
        
        return null;
    }

    /**
     * 发送短信
     */
    private boolean sendSms(String providerCode, String phone, String content, String templateCode, Map<String, Object> templateParams) {
        try {
            switch (providerCode) {
                case "ALIYUN_SMS":
                    return sendAliyunSms(phone, content, templateCode, templateParams);
                case "TENCENT_SMS":
                    return sendTencentSms(phone, content, templateCode, templateParams);
                default:
                    log.warn("未知的短信服务商: {}, 使用Mock模式", providerCode);
                    return mockSendSms(providerCode, phone, content, templateCode);
            }
        } catch (Exception e) {
            log.error("短信发送失败: provider={}, phone={}", providerCode, phone, e);
            return false;
        }
    }

    /**
     * 阿里云短信发送
     */
    private boolean sendAliyunSms(String phone, String content, String templateCode, Map<String, Object> templateParams) {
        try {
            NotificationProperties.AliyunSms config = notificationProperties.getProviders().getSms().getAliyun();

            // 如果配置不完整，使用Mock模式
            if (!StringUtils.hasText(config.getAccessKeyId()) ||
                !StringUtils.hasText(config.getAccessKeySecret()) ||
                !StringUtils.hasText(config.getSignName())) {
                log.info("阿里云短信配置不完整，使用Mock模式发送: phone={}, content={}", phone, content);
                return true;
            }

            // 构建阿里云短信API请求
            String url = "https://dysmsapi.aliyuncs.com/";

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("Action", "SendSms");
            params.put("Version", "2017-05-25");
            params.put("RegionId", "cn-hangzhou");
            params.put("PhoneNumbers", phone);
            params.put("SignName", config.getSignName());
            params.put("TemplateCode", templateCode != null ? templateCode : "SMS_DEFAULT");

            // 处理模板参数
            if (templateParams != null && !templateParams.isEmpty()) {
                try {
                    params.put("TemplateParam", objectMapper.writeValueAsString(templateParams));
                } catch (Exception e) {
                    log.warn("模板参数序列化失败，使用内容作为参数", e);
                    Map<String, String> fallbackParams = new HashMap<>();
                    fallbackParams.put("content", content);
                    params.put("TemplateParam", objectMapper.writeValueAsString(fallbackParams));
                }
            } else {
                // 如果没有模板参数，使用内容作为默认参数
                Map<String, String> defaultParams = new HashMap<>();
                defaultParams.put("content", content);
                params.put("TemplateParam", objectMapper.writeValueAsString(defaultParams));
            }

            // 添加公共参数
            params.put("AccessKeyId", config.getAccessKeyId());
            params.put("Format", "JSON");
            params.put("SignatureMethod", "HMAC-SHA1");
            params.put("SignatureVersion", "1.0");
            params.put("SignatureNonce", String.valueOf(System.currentTimeMillis()));
            params.put("Timestamp", java.time.Instant.now().toString());

            log.info("发送阿里云短信: phone={}, signName={}, templateCode={}",
                    phone, config.getSignName(), templateCode);

            // 使用WebClient发送HTTP请求
            String response = webClient.post()
                    .uri(url)
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(buildQueryString(params))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("阿里云短信API响应: {}", response);

            // 简单判断响应是否成功（实际项目中应该解析JSON响应）
            boolean success = response != null && (response.contains("\"Code\":\"OK\"") || response.contains("\"Message\":\"OK\""));

            if (success) {
                log.info("阿里云短信发送成功: phone={}", phone);
            } else {
                log.error("阿里云短信发送失败: phone={}, response={}", phone, response);
            }

            return success;

        } catch (Exception e) {
            log.error("阿里云短信发送异常: phone={}", phone, e);
            return false;
        }
    }

    /**
     * 腾讯云短信发送
     */
    private boolean sendTencentSms(String phone, String content, String templateCode, Map<String, Object> templateParams) {
        try {
            NotificationProperties.TencentSms config = notificationProperties.getProviders().getSms().getTencent();

            // 如果配置不完整，使用Mock模式
            if (!StringUtils.hasText(config.getSecretId()) ||
                !StringUtils.hasText(config.getSecretKey()) ||
                !StringUtils.hasText(config.getAppId())) {
                log.info("腾讯云短信配置不完整，使用Mock模式发送: phone={}, content={}", phone, content);
                return true;
            }

            // 腾讯云短信API地址
            String url = "https://sms.tencentcloudapi.com/";

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("SmsSdkAppId", config.getAppId());
            requestBody.put("TemplateId", templateCode != null ? templateCode : "DEFAULT");

            // 处理手机号（腾讯云需要+86前缀）
            String[] phoneNumbers = {phone.startsWith("+86") ? phone : "+86" + phone};
            requestBody.put("PhoneNumberSet", phoneNumbers);

            // 处理模板参数
            if (templateParams != null && !templateParams.isEmpty()) {
                String[] templateParamArray = templateParams.values().stream()
                        .map(Object::toString)
                        .toArray(String[]::new);
                requestBody.put("TemplateParamSet", templateParamArray);
            } else {
                // 如果没有模板参数，使用内容作为默认参数
                requestBody.put("TemplateParamSet", new String[]{content});
            }

            // 签名参数（这里简化处理，实际项目中需要完整的签名算法）
            requestBody.put("SignName", "腾讯云");

            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "TC3-HMAC-SHA256 Credential=" + config.getSecretId());
            headers.put("Content-Type", "application/json; charset=utf-8");
            headers.put("Host", "sms.tencentcloudapi.com");
            headers.put("X-TC-Action", "SendSms");
            headers.put("X-TC-Version", "2021-01-11");
            headers.put("X-TC-Region", "ap-guangzhou");

            log.info("发送腾讯云短信: phone={}, appId={}, templateCode={}",
                    phone, config.getAppId(), templateCode);

            // 使用WebClient发送HTTP请求
            String response = webClient.post()
                    .uri(url)
                    .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("腾讯云短信API响应: {}", response);

            // 简单判断响应是否成功（实际项目中应该解析JSON响应）
            boolean success = response != null && !response.contains("\"Error\":");

            if (success) {
                log.info("腾讯云短信发送成功: phone={}", phone);
            } else {
                log.error("腾讯云短信发送失败: phone={}, response={}", phone, response);
            }

            return success;

        } catch (Exception e) {
            log.error("腾讯云短信发送异常: phone={}", phone, e);
            return false;
        }
    }

    /**
     * Mock短信发送
     */
    private boolean mockSendSms(String providerCode, String phone, String content, String templateCode) {
        log.info("Mock短信发送: provider={}, phone={}, templateCode={}, content={}",
                providerCode, phone, templateCode, content);
        return true;
    }

    /**
     * 验证手机号格式
     */
    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phonePattern.matcher(phone).matches();
    }

    /**
     * 构建查询字符串
     */
    private String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            try {
                sb.append(java.net.URLEncoder.encode(entry.getKey(), "UTF-8"))
                  .append("=")
                  .append(java.net.URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (Exception e) {
                log.warn("URL编码失败: key={}, value={}", entry.getKey(), entry.getValue(), e);
            }
        }
        return sb.toString();
    }
}
