package com.enterprise.notification.controller;

import com.enterprise.notification.common.dto.DirectSendNotificationRequest;
import com.enterprise.notification.common.dto.SendNotificationRequest;
import com.enterprise.notification.common.dto.SendNotificationResponse;
import com.enterprise.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 通知服务REST控制器
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@Validated
@Tag(name = "通知服务", description = "提供统一的消息通知发送服务")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 发送通知
     *
     * @param request 发送请求
     * @return 发送响应
     */
    @Operation(summary = "发送通知", description = "根据模板代码和接收者信息发送通知，支持个人通知和组通知")
    @PostMapping("/send")
    public ResponseEntity<SendNotificationResponse> sendNotification(
            @Parameter(description = "通知发送请求") @Valid @RequestBody SendNotificationRequest request) {
        
        log.info("接收到通知发送请求: requestId={}, templateCode={}, recipientType={}, recipientId={}", 
                request.getRequestId(), 
                request.getTemplateCode(),
                request.getRecipient().getType(),
                request.getRecipient().getId());

        try {
            SendNotificationResponse response = notificationService.sendNotification(request);

            log.info("通知发送请求处理完成: requestId={}, status={}",
                    request.getRequestId(), response.getStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("通知发送请求处理异常: requestId={}", request.getRequestId(), e);

            SendNotificationResponse errorResponse = new SendNotificationResponse();
            errorResponse.setRequestId(request.getRequestId());
            errorResponse.setStatus("FAILED");
            errorResponse.setErrorMessage("服务器内部错误: " + e.getMessage());
            errorResponse.setProcessedAt(java.time.LocalDateTime.now());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 直接发送通知（不使用模板）
     *
     * @param request 直接发送请求
     * @return 发送响应
     */
    @Operation(summary = "直接发送通知", description = "不使用模板配置，直接发送指定内容的通知，支持个人通知和组通知")
    @PostMapping("/send-direct")
    public ResponseEntity<SendNotificationResponse> sendDirectNotification(
            @Parameter(description = "直接发送通知请求") @Valid @RequestBody DirectSendNotificationRequest request) {

        log.info("接收到直接发送通知请求: requestId={}, channelCodes={}, recipientType={}, recipientId={}",
                request.getRequestId(),
                request.getChannelCodes(),
                request.getRecipient().getType(),
                request.getRecipient().getId());

        try {
            SendNotificationResponse response = notificationService.sendDirectNotification(request);

            log.info("直接发送通知请求处理完成: requestId={}, status={}",
                    request.getRequestId(), response.getStatus());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("直接发送通知请求处理异常: requestId={}", request.getRequestId(), e);

            SendNotificationResponse errorResponse = new SendNotificationResponse();
            errorResponse.setRequestId(request.getRequestId());
            errorResponse.setStatus("FAILED");
            errorResponse.setErrorMessage("服务器内部错误: " + e.getMessage());
            errorResponse.setProcessedAt(java.time.LocalDateTime.now());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 健康检查
     */
    @Operation(summary = "健康检查", description = "检查通知服务是否正常运行")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}
