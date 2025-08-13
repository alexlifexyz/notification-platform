package com.enterprise.notification.controller;

import com.enterprise.notification.async.AsyncTaskManager;
import com.enterprise.notification.common.dto.AsyncSendResponse;
import com.enterprise.notification.common.dto.AsyncTaskStatusResponse;
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
import java.util.concurrent.CompletableFuture;

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

    @Autowired
    private AsyncTaskManager asyncTaskManager;

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
     * 异步发送通知（模板方式）
     *
     * @param request 发送请求
     * @return 异步响应
     */
    @Operation(summary = "异步发送通知", description = "异步发送通知，立即返回任务状态，通知在后台处理")
    @PostMapping("/send-async")
    public ResponseEntity<AsyncSendResponse> sendNotificationAsync(
            @Parameter(description = "通知发送请求") @Valid @RequestBody SendNotificationRequest request) {

        log.info("接收到异步发送通知请求: requestId={}, templateCode={}",
                request.getRequestId(), request.getTemplateCode());

        try {
            // 提交异步任务
            CompletableFuture<SendNotificationResponse> future =
                    notificationService.sendNotificationAsync(request);

            // 注册任务到管理器
            CompletableFuture<Void> taskFuture = future.thenAccept(response -> {
                log.info("异步通知任务完成: requestId={}, status={}",
                        request.getRequestId(), response.getStatus());
            });

            asyncTaskManager.registerTask(request.getRequestId(), taskFuture);

            // 立即返回异步响应
            AsyncSendResponse asyncResponse = AsyncSendResponse.success(request.getRequestId());

            log.info("异步通知任务已提交: requestId={}", request.getRequestId());

            return ResponseEntity.accepted().body(asyncResponse);

        } catch (Exception e) {
            log.error("异步通知任务提交失败: requestId={}", request.getRequestId(), e);

            AsyncSendResponse errorResponse = AsyncSendResponse.failure(
                    request.getRequestId(), e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 异步直接发送通知
     *
     * @param request 直接发送请求
     * @return 异步响应
     */
    @Operation(summary = "异步直接发送通知", description = "异步直接发送通知，不使用模板，立即返回任务状态")
    @PostMapping("/send-direct-async")
    public ResponseEntity<AsyncSendResponse> sendDirectNotificationAsync(
            @Parameter(description = "直接发送通知请求") @Valid @RequestBody DirectSendNotificationRequest request) {

        log.info("接收到异步直接发送通知请求: requestId={}, channelCodes={}",
                request.getRequestId(), request.getChannelCodes());

        try {
            // 提交异步任务
            CompletableFuture<SendNotificationResponse> future =
                    notificationService.sendDirectNotificationAsync(request);

            // 注册任务到管理器
            CompletableFuture<Void> taskFuture = future.thenAccept(response -> {
                log.info("异步直接发送任务完成: requestId={}, status={}",
                        request.getRequestId(), response.getStatus());
            });

            asyncTaskManager.registerTask(request.getRequestId(), taskFuture);

            // 立即返回异步响应
            AsyncSendResponse asyncResponse = AsyncSendResponse.success(request.getRequestId());

            log.info("异步直接发送任务已提交: requestId={}", request.getRequestId());

            return ResponseEntity.accepted().body(asyncResponse);

        } catch (Exception e) {
            log.error("异步直接发送任务提交失败: requestId={}", request.getRequestId(), e);

            AsyncSendResponse errorResponse = AsyncSendResponse.failure(
                    request.getRequestId(), e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 查询异步任务状态
     *
     * @param requestId 请求ID
     * @return 任务状态
     */
    @Operation(summary = "查询异步任务状态", description = "根据请求ID查询异步通知任务的执行状态和结果")
    @GetMapping("/async/status/{requestId}")
    public ResponseEntity<AsyncTaskStatusResponse> getAsyncTaskStatus(
            @Parameter(description = "请求ID") @PathVariable String requestId) {

        log.info("查询异步任务状态: requestId={}", requestId);

        AsyncTaskManager.AsyncTaskInfo taskInfo = asyncTaskManager.getTaskInfo(requestId);

        if (taskInfo == null) {
            log.warn("异步任务不存在: requestId={}", requestId);
            return ResponseEntity.notFound().build();
        }

        AsyncTaskStatusResponse response = new AsyncTaskStatusResponse();
        response.setRequestId(requestId);
        response.setTaskStatus(taskInfo.getStatus().name());
        response.setStartTime(taskInfo.getStartTime());
        response.setEndTime(taskInfo.getEndTime());
        response.setErrorMessage(taskInfo.getErrorMessage());

        // 如果任务已完成，尝试获取结果
        if (taskInfo.getFuture() != null && taskInfo.getFuture().isDone()) {
            try {
                // 注意：这里需要根据实际情况获取结果
                // 由于CompletableFuture<Void>，我们无法直接获取SendNotificationResponse
                // 在实际实现中，可能需要调整异步任务的返回类型或存储结果
                response.setProgressInfo("任务已完成，详细结果请查看日志");
            } catch (Exception e) {
                log.warn("获取异步任务结果失败: requestId={}", requestId, e);
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 取消异步任务
     *
     * @param requestId 请求ID
     * @return 取消结果
     */
    @Operation(summary = "取消异步任务", description = "取消正在执行的异步通知任务")
    @DeleteMapping("/async/cancel/{requestId}")
    public ResponseEntity<String> cancelAsyncTask(
            @Parameter(description = "请求ID") @PathVariable String requestId) {

        log.info("取消异步任务: requestId={}", requestId);

        boolean cancelled = asyncTaskManager.cancelTask(requestId);

        if (cancelled) {
            log.info("异步任务已取消: requestId={}", requestId);
            return ResponseEntity.ok("任务已成功取消");
        } else {
            log.warn("异步任务取消失败: requestId={}", requestId);
            return ResponseEntity.badRequest().body("任务取消失败，可能任务不存在或已完成");
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
