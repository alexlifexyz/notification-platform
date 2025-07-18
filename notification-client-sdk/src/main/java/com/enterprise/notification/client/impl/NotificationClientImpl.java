package com.enterprise.notification.client.impl;

import com.enterprise.notification.client.NotificationClient;
import com.enterprise.notification.client.config.NotificationClientProperties;
import com.enterprise.notification.common.dto.SendNotificationRequest;
import com.enterprise.notification.common.dto.SendNotificationResponse;
import com.enterprise.notification.common.exception.NotificationClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * 通知客户端默认实现
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
public class NotificationClientImpl implements NotificationClient {

    private final RestTemplate restTemplate;
    private final NotificationClientProperties properties;

    public NotificationClientImpl(RestTemplate restTemplate, NotificationClientProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public SendNotificationResponse sendNotification(SendNotificationRequest request) {
        log.info("发送通知请求: requestId={}, templateCode={}, recipientType={}, recipientId={}", 
                request.getRequestId(), request.getTemplateCode(), 
                request.getRecipient().getType(), request.getRecipient().getId());

        try {
            String url = buildUrl("/api/v1/notifications/send");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<SendNotificationRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<SendNotificationResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, SendNotificationResponse.class);

            SendNotificationResponse result = response.getBody();
            
            log.info("通知发送完成: requestId={}, status={}", 
                    request.getRequestId(), result != null ? result.getStatus() : "null");
            
            return result;

        } catch (HttpClientErrorException e) {
            log.error("客户端错误: requestId={}, status={}, body={}", 
                     request.getRequestId(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new NotificationClientException("CLIENT_ERROR", 
                    "客户端请求错误: " + e.getResponseBodyAsString(), 
                    e.getRawStatusCode(), e);

        } catch (HttpServerErrorException e) {
            log.error("服务器错误: requestId={}, status={}, body={}", 
                     request.getRequestId(), e.getStatusCode(), e.getResponseBodyAsString());
            throw new NotificationClientException("SERVER_ERROR", 
                    "服务器内部错误: " + e.getResponseBodyAsString(), 
                    e.getRawStatusCode(), e);

        } catch (ResourceAccessException e) {
            log.error("网络连接错误: requestId={}", request.getRequestId(), e);
            throw new NotificationClientException("NETWORK_ERROR", 
                    "网络连接失败: " + e.getMessage(), 503, e);

        } catch (Exception e) {
            log.error("未知错误: requestId={}", request.getRequestId(), e);
            throw new NotificationClientException("UNKNOWN_ERROR", 
                    "未知错误: " + e.getMessage(), 500, e);
        }
    }

    @Override
    public SendNotificationResponse sendToUser(String requestId, String templateCode, String userId, 
                                              Map<String, Object> templateParams) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setRequestId(requestId);
        request.setTemplateCode(templateCode);
        request.setTemplateParams(templateParams);

        SendNotificationRequest.RecipientInfo recipient = new SendNotificationRequest.RecipientInfo();
        recipient.setType("individual");
        recipient.setId(userId);
        request.setRecipient(recipient);

        return sendNotification(request);
    }

    @Override
    public SendNotificationResponse sendToUser(String requestId, String templateCode, String userId, 
                                              String userName, String phone, String email, 
                                              Map<String, Object> templateParams) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setRequestId(requestId);
        request.setTemplateCode(templateCode);
        request.setTemplateParams(templateParams);

        SendNotificationRequest.RecipientInfo recipient = new SendNotificationRequest.RecipientInfo();
        recipient.setType("individual");
        recipient.setId(userId);

        SendNotificationRequest.ContactInfo contactInfo = new SendNotificationRequest.ContactInfo();
        contactInfo.setUserName(userName);
        contactInfo.setPhone(phone);
        contactInfo.setEmail(email);
        recipient.setContactInfo(contactInfo);

        request.setRecipient(recipient);

        return sendNotification(request);
    }

    @Override
    public SendNotificationResponse sendToGroup(String requestId, String templateCode, String groupCode, 
                                               Map<String, Object> templateParams) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setRequestId(requestId);
        request.setTemplateCode(templateCode);
        request.setTemplateParams(templateParams);

        SendNotificationRequest.RecipientInfo recipient = new SendNotificationRequest.RecipientInfo();
        recipient.setType("group");
        recipient.setId(groupCode);
        request.setRecipient(recipient);

        return sendNotification(request);
    }

    @Override
    public boolean isHealthy() {
        try {
            String url = buildUrl("/api/v1/notifications/health");
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("健康检查失败", e);
            return false;
        }
    }

    private String buildUrl(String path) {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + path;
    }
}
