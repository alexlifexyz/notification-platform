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

/**
 * 企业IM发送器
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Component
public class ImSender implements NotificationSender {

    @Autowired
    private NotificationProperties notificationProperties;

    @Autowired
    private ObjectMapper objectMapper;

    private final WebClient webClient;

    public ImSender() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String getSupportedChannel() {
        return ChannelCode.IM.getCode();
    }

    @Override
    public SendResult send(NotificationTemplate template,
                          RecipientInfo recipientInfo,
                          String renderedSubject,
                          String renderedContent,
                          Map<String, Object> templateParams) {

        try {
            log.info("开始发送IM消息: imAccount={}, content={}",
                    recipientInfo.getImAccount(), renderedContent);

            // 验证接收者信息
            if (!StringUtils.hasText(recipientInfo.getImAccount())) {
                return new SendResult(false, "IM", "IM账号不能为空");
            }

            if (!StringUtils.hasText(renderedContent)) {
                return new SendResult(false, "IM", "IM消息内容不能为空");
            }

            // 选择可用的IM服务商
            String providerCode = selectImProvider();
            if (providerCode == null) {
                return new SendResult(false, "IM", "没有可用的IM服务商");
            }

            // 发送IM消息
            boolean sendSuccess = sendImMessage(providerCode, recipientInfo.getImAccount(),
                                              renderedSubject, renderedContent, templateParams);

            if (sendSuccess) {
                log.info("IM消息发送成功: imAccount={}, provider={}",
                        recipientInfo.getImAccount(), providerCode);
                return new SendResult(true, providerCode);
            } else {
                log.error("IM消息发送失败: imAccount={}, provider={}",
                         recipientInfo.getImAccount(), providerCode);
                return new SendResult(false, providerCode, "IM消息发送失败");
            }

        } catch (Exception e) {
            log.error("IM消息发送异常: imAccount={}", recipientInfo.getImAccount(), e);
            return new SendResult(false, "IM", "发送异常: " + e.getMessage());
        }
    }

    /**
     * 选择可用的IM服务商
     */
    private String selectImProvider() {
        NotificationProperties.Im imConfig = notificationProperties.getProviders().getIm();
        
        if (imConfig.getWechatWork().isEnabled()) {
            return "WECHAT_WORK";
        }
        
        if (imConfig.getDingtalk().isEnabled()) {
            return "DINGTALK";
        }
        
        return null;
    }

    /**
     * 发送IM消息
     */
    private boolean sendImMessage(String providerCode, String imAccount, String subject, String content, Map<String, Object> templateParams) {
        try {
            switch (providerCode) {
                case "WECHAT_WORK":
                    return sendWechatWorkMessage(imAccount, subject, content, templateParams);
                case "DINGTALK":
                    return sendDingTalkMessage(imAccount, subject, content, templateParams);
                default:
                    log.warn("未知的IM服务商: {}, 使用Mock模式", providerCode);
                    return mockSendIm(providerCode, imAccount, subject, content);
            }
        } catch (Exception e) {
            log.error("IM消息发送失败: provider={}, imAccount={}", providerCode, imAccount, e);
            return false;
        }
    }

    /**
     * 企业微信消息发送
     */
    private boolean sendWechatWorkMessage(String imAccount, String subject, String content, Map<String, Object> templateParams) {
        try {
            NotificationProperties.WechatWork config = notificationProperties.getProviders().getIm().getWechatWork();

            // 如果配置不完整，使用Mock模式
            if (!StringUtils.hasText(config.getCorpId()) ||
                !StringUtils.hasText(config.getCorpSecret()) ||
                !StringUtils.hasText(config.getAgentId())) {
                log.info("企业微信配置不完整，使用Mock模式发送: imAccount={}, content={}", imAccount, content);
                return true;
            }

            // 第一步：获取access_token
            String accessToken = getWechatWorkAccessToken(config);
            if (accessToken == null) {
                log.error("获取企业微信access_token失败");
                return false;
            }

            // 第二步：发送消息
            return sendWechatWorkMessageWithToken(accessToken, config, imAccount, subject, content);

        } catch (Exception e) {
            log.error("企业微信消息发送异常: imAccount={}", imAccount, e);
            return false;
        }
    }

    /**
     * 获取企业微信access_token
     */
    private String getWechatWorkAccessToken(NotificationProperties.WechatWork config) {
        try {
            String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken";

            Map<String, String> params = new HashMap<>();
            params.put("corpid", config.getCorpId());
            params.put("corpsecret", config.getCorpSecret());

            String queryString = buildQueryString(params);
            String fullUrl = url + "?" + queryString;

            log.info("获取企业微信access_token: corpId={}", config.getCorpId());

            String response = webClient.get()
                    .uri(fullUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("企业微信token响应: {}", response);

            if (response != null && response.contains("\"access_token\"")) {
                // 简单解析JSON获取access_token（实际项目中应该使用JSON解析库）
                String token = extractJsonValue(response, "access_token");
                if (token != null) {
                    log.info("企业微信access_token获取成功");
                    return token;
                }
            }

            log.error("企业微信access_token获取失败: {}", response);
            return null;

        } catch (Exception e) {
            log.error("获取企业微信access_token异常", e);
            return null;
        }
    }

    /**
     * 使用access_token发送企业微信消息
     */
    private boolean sendWechatWorkMessageWithToken(String accessToken, NotificationProperties.WechatWork config,
                                                  String imAccount, String subject, String content) {
        try {
            String url = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=" + accessToken;

            // 构建消息内容
            String messageContent = StringUtils.hasText(subject) ?
                    subject + "\n\n" + content : content;

            // 构建请求参数
            Map<String, Object> message = new HashMap<>();
            message.put("touser", imAccount);
            message.put("msgtype", "text");
            message.put("agentid", Integer.parseInt(config.getAgentId()));

            Map<String, String> textContent = new HashMap<>();
            textContent.put("content", messageContent);
            message.put("text", textContent);

            log.info("发送企业微信消息: imAccount={}, agentId={}", imAccount, config.getAgentId());

            String response = webClient.post()
                    .uri(url)
                    .bodyValue(message)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("企业微信消息发送响应: {}", response);

            // 判断是否发送成功
            boolean success = response != null && response.contains("\"errcode\":0");

            if (success) {
                log.info("企业微信消息发送成功: imAccount={}", imAccount);
            } else {
                log.error("企业微信消息发送失败: imAccount={}, response={}", imAccount, response);
            }

            return success;

        } catch (Exception e) {
            log.error("企业微信消息发送异常: imAccount={}", imAccount, e);
            return false;
        }
    }

    /**
     * 钉钉消息发送
     */
    private boolean sendDingTalkMessage(String imAccount, String subject, String content, Map<String, Object> templateParams) {
        try {
            NotificationProperties.DingTalk config = notificationProperties.getProviders().getIm().getDingtalk();

            // 如果配置不完整，使用Mock模式
            if (!StringUtils.hasText(config.getAppKey()) ||
                !StringUtils.hasText(config.getAppSecret())) {
                log.info("钉钉配置不完整，使用Mock模式发送: imAccount={}, content={}", imAccount, content);
                return true;
            }

            // 第一步：获取access_token
            String accessToken = getDingTalkAccessToken(config);
            if (accessToken == null) {
                log.error("获取钉钉access_token失败");
                return false;
            }

            // 第二步：发送工作通知
            return sendDingTalkWorkNotification(accessToken, config, imAccount, subject, content);

        } catch (Exception e) {
            log.error("钉钉消息发送异常: imAccount={}", imAccount, e);
            return false;
        }
    }

    /**
     * 获取钉钉access_token
     */
    private String getDingTalkAccessToken(NotificationProperties.DingTalk config) {
        try {
            String url = "https://oapi.dingtalk.com/gettoken";

            Map<String, String> params = new HashMap<>();
            params.put("appkey", config.getAppKey());
            params.put("appsecret", config.getAppSecret());

            String queryString = buildQueryString(params);
            String fullUrl = url + "?" + queryString;

            log.info("获取钉钉access_token: appKey={}", config.getAppKey());

            String response = webClient.get()
                    .uri(fullUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("钉钉token响应: {}", response);

            if (response != null && response.contains("\"access_token\"")) {
                // 简单解析JSON获取access_token
                String token = extractJsonValue(response, "access_token");
                if (token != null) {
                    log.info("钉钉access_token获取成功");
                    return token;
                }
            }

            log.error("钉钉access_token获取失败: {}", response);
            return null;

        } catch (Exception e) {
            log.error("获取钉钉access_token异常", e);
            return null;
        }
    }

    /**
     * 发送钉钉工作通知
     */
    private boolean sendDingTalkWorkNotification(String accessToken, NotificationProperties.DingTalk config,
                                               String imAccount, String subject, String content) {
        try {
            String url = "https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2?access_token=" + accessToken;

            // 构建消息内容
            String messageContent = StringUtils.hasText(subject) ?
                    subject + "\n\n" + content : content;

            // 构建请求参数
            Map<String, Object> message = new HashMap<>();
            message.put("agent_id", config.getAppKey()); // 使用appKey作为agentId
            message.put("userid_list", imAccount);

            // 构建消息内容（Java 8兼容写法）
            Map<String, String> textContent = new HashMap<>();
            textContent.put("content", messageContent);

            Map<String, Object> msgContent = new HashMap<>();
            msgContent.put("msgtype", "text");
            msgContent.put("text", textContent);

            message.put("msg", msgContent);

            log.info("发送钉钉工作通知: imAccount={}, agentId={}", imAccount, config.getAppKey());

            String response = webClient.post()
                    .uri(url)
                    .bodyValue(message)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("钉钉工作通知发送响应: {}", response);

            // 判断是否发送成功
            boolean success = response != null && response.contains("\"errcode\":0");

            if (success) {
                log.info("钉钉消息发送成功: imAccount={}", imAccount);
            } else {
                log.error("钉钉消息发送失败: imAccount={}, response={}", imAccount, response);
            }

            return success;

        } catch (Exception e) {
            log.error("钉钉工作通知发送异常: imAccount={}", imAccount, e);
            return false;
        }
    }

    /**
     * Mock IM消息发送
     */
    private boolean mockSendIm(String providerCode, String imAccount, String subject, String content) {
        log.info("Mock IM消息发送: provider={}, imAccount={}, subject={}, content={}",
                providerCode, imAccount, subject, content);
        return true;
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

    /**
     * 从JSON字符串中提取指定字段的值（简单实现）
     */
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            log.warn("JSON解析失败: key={}, json={}", key, json, e);
        }
        return null;
    }
}
