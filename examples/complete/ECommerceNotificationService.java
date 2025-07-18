package com.enterprise.ecommerce.service;

import com.enterprise.notification.client.NotificationClient;
import com.enterprise.notification.common.dto.SendNotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 电商业务通知服务 - 完整的电商场景示例
 * 
 * 展示了电商平台中订单、支付、物流等各个环节的通知集成
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Service
public class ECommerceNotificationService {

    @Autowired
    private NotificationClient notificationClient;

    /**
     * 订单创建通知
     * 场景：用户下单成功后的确认通知
     */
    public void sendOrderCreatedNotification(OrderInfo order) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userName", order.getUserName());
            params.put("orderNo", order.getOrderNo());
            params.put("totalAmount", order.getTotalAmount().toString());
            params.put("itemCount", order.getItems().size());
            params.put("orderTime", order.getCreatedTime().toString());
            params.put("expectedDelivery", order.getExpectedDelivery().toString());
            params.put("orderDetailLink", "https://shop.company.com/order/" + order.getOrderNo());

            String requestId = "order_created_" + order.getOrderNo();

            // 发送订单确认通知
            SendNotificationResponse response = notificationClient.sendToUser(
                    requestId,
                    "ORDER_CREATED",
                    order.getUserId(),
                    order.getUserName(),
                    order.getPhone(),
                    order.getEmail(),
                    params
            );

            log.info("订单创建通知发送完成: orderNo={}, status={}", order.getOrderNo(), response.getStatus());

            // 如果是VIP用户，发送特殊服务通知
            if (order.isVipUser()) {
                sendVipServiceNotification(order);
            }

        } catch (Exception e) {
            log.error("发送订单创建通知失败: orderNo={}", order.getOrderNo(), e);
        }
    }

    /**
     * VIP用户特殊服务通知
     */
    private void sendVipServiceNotification(OrderInfo order) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userName", order.getUserName());
            params.put("orderNo", order.getOrderNo());
            params.put("vipLevel", order.getVipLevel());
            params.put("specialServices", "专属客服、优先发货、免费包装");
            params.put("customerServicePhone", "400-888-8888");

            String requestId = "vip_service_" + order.getOrderNo();

            SendNotificationResponse response = notificationClient.sendToUser(
                    requestId,
                    "VIP_ORDER_SERVICE",
                    order.getUserId(),
                    params
            );

            log.info("VIP服务通知发送完成: orderNo={}, status={}", order.getOrderNo(), response.getStatus());

        } catch (Exception e) {
            log.error("发送VIP服务通知失败: orderNo={}", order.getOrderNo(), e);
        }
    }

    /**
     * 支付成功通知
     * 场景：订单支付完成后的通知
     */
    public void sendPaymentSuccessNotification(PaymentInfo payment) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userName", payment.getUserName());
            params.put("orderNo", payment.getOrderNo());
            params.put("paymentAmount", payment.getAmount().toString());
            params.put("paymentMethod", payment.getPaymentMethod());
            params.put("paymentTime", payment.getPaymentTime().toString());
            params.put("transactionId", payment.getTransactionId());
            params.put("receiptLink", "https://shop.company.com/receipt/" + payment.getTransactionId());

            String requestId = "payment_success_" + payment.getTransactionId();

            // 同时发送站内信和短信
            SendNotificationResponse inAppResponse = notificationClient.sendToUser(
                    requestId + "_inapp",
                    "PAYMENT_SUCCESS_INAPP",
                    payment.getUserId(),
                    params
            );

            SendNotificationResponse smsResponse = notificationClient.sendToUser(
                    requestId + "_sms",
                    "PAYMENT_SUCCESS_SMS",
                    payment.getUserId(),
                    payment.getUserName(),
                    payment.getPhone(),
                    null,
                    params
            );

            log.info("支付成功通知发送完成: transactionId={}, inAppStatus={}, smsStatus={}", 
                    payment.getTransactionId(), inAppResponse.getStatus(), smsResponse.getStatus());

            // 通知仓库准备发货
            notifyWarehouseForShipping(payment.getOrderNo());

        } catch (Exception e) {
            log.error("发送支付成功通知失败: transactionId={}", payment.getTransactionId(), e);
        }
    }

    /**
     * 通知仓库发货
     */
    private void notifyWarehouseForShipping(String orderNo) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("orderNo", orderNo);
            params.put("urgency", "NORMAL");
            params.put("paymentTime", LocalDateTime.now().toString());
            params.put("warehouseSystemLink", "https://warehouse.company.com/orders/" + orderNo);

            String requestId = "warehouse_shipping_" + orderNo;

            // 发送给仓库团队
            SendNotificationResponse response = notificationClient.sendToGroup(
                    requestId,
                    "WAREHOUSE_SHIPPING_NOTICE",
                    "WAREHOUSE_TEAM",
                    params
            );

            log.info("仓库发货通知发送完成: orderNo={}, status={}", orderNo, response.getStatus());

        } catch (Exception e) {
            log.error("发送仓库发货通知失败: orderNo={}", orderNo, e);
        }
    }

    /**
     * 物流状态更新通知
     * 场景：订单在物流过程中的状态变更
     */
    public void sendShippingStatusNotification(ShippingInfo shipping) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userName", shipping.getUserName());
            params.put("orderNo", shipping.getOrderNo());
            params.put("trackingNo", shipping.getTrackingNo());
            params.put("currentStatus", shipping.getStatus());
            params.put("statusDescription", getStatusDescription(shipping.getStatus()));
            params.put("updateTime", shipping.getUpdateTime().toString());
            params.put("trackingLink", "https://tracking.company.com/" + shipping.getTrackingNo());

            // 根据不同状态选择不同模板
            String templateCode = getShippingTemplateCode(shipping.getStatus());
            String requestId = "shipping_" + shipping.getStatus().toLowerCase() + "_" + shipping.getTrackingNo();

            SendNotificationResponse response = notificationClient.sendToUser(
                    requestId,
                    templateCode,
                    shipping.getUserId(),
                    shipping.getUserName(),
                    shipping.getPhone(),
                    shipping.getEmail(),
                    params
            );

            log.info("物流状态通知发送完成: trackingNo={}, status={}, notificationStatus={}", 
                    shipping.getTrackingNo(), shipping.getStatus(), response.getStatus());

            // 如果是已送达，发送评价邀请
            if ("DELIVERED".equals(shipping.getStatus())) {
                scheduleReviewInvitation(shipping);
            }

        } catch (Exception e) {
            log.error("发送物流状态通知失败: trackingNo={}", shipping.getTrackingNo(), e);
        }
    }

    /**
     * 异步发送评价邀请
     */
    @Async
    public void scheduleReviewInvitation(ShippingInfo shipping) {
        try {
            // 延迟24小时后发送评价邀请
            Thread.sleep(24 * 60 * 60 * 1000); // 实际项目中应使用定时任务

            Map<String, Object> params = new HashMap<>();
            params.put("userName", shipping.getUserName());
            params.put("orderNo", shipping.getOrderNo());
            params.put("deliveryTime", shipping.getUpdateTime().toString());
            params.put("reviewLink", "https://shop.company.com/review/" + shipping.getOrderNo());
            params.put("incentive", "完成评价可获得10积分");

            String requestId = "review_invitation_" + shipping.getOrderNo() + "_" + System.currentTimeMillis();

            SendNotificationResponse response = notificationClient.sendToUser(
                    requestId,
                    "REVIEW_INVITATION",
                    shipping.getUserId(),
                    params
            );

            log.info("评价邀请发送完成: orderNo={}, status={}", shipping.getOrderNo(), response.getStatus());

        } catch (Exception e) {
            log.error("发送评价邀请失败: orderNo={}", shipping.getOrderNo(), e);
        }
    }

    /**
     * 促销活动通知
     * 场景：向特定用户群体发送促销信息
     */
    public void sendPromotionNotification(PromotionInfo promotion, List<String> targetUserIds) {
        log.info("开始发送促销通知: promotionId={}, targetUsers={}", promotion.getPromotionId(), targetUserIds.size());

        // 使用并行流提高发送效率
        targetUserIds.parallelStream().forEach(userId -> {
            try {
                Map<String, Object> params = new HashMap<>();
                params.put("promotionTitle", promotion.getTitle());
                params.put("discount", promotion.getDiscount());
                params.put("validUntil", promotion.getValidUntil().toString());
                params.put("promotionCode", promotion.getPromotionCode());
                params.put("shopLink", "https://shop.company.com/promotion/" + promotion.getPromotionId());
                params.put("termsLink", "https://shop.company.com/terms/" + promotion.getPromotionId());

                String requestId = "promotion_" + promotion.getPromotionId() + "_" + userId + "_" + System.currentTimeMillis();

                SendNotificationResponse response = notificationClient.sendToUser(
                        requestId,
                        "PROMOTION_NOTIFICATION",
                        userId,
                        params
                );

                log.debug("促销通知发送: userId={}, status={}", userId, response.getStatus());

            } catch (Exception e) {
                log.error("发送促销通知失败: userId={}", userId, e);
            }
        });

        log.info("促销通知发送完成: promotionId={}", promotion.getPromotionId());
    }

    /**
     * 库存不足警告
     * 场景：商品库存低于阈值时通知采购团队
     */
    public void sendLowStockAlert(ProductInfo product) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("productName", product.getProductName());
            params.put("productSku", product.getSku());
            params.put("currentStock", product.getCurrentStock());
            params.put("threshold", product.getLowStockThreshold());
            params.put("category", product.getCategory());
            params.put("supplier", product.getSupplier());
            params.put("lastSaleDate", product.getLastSaleDate().toString());
            params.put("inventoryLink", "https://admin.company.com/inventory/" + product.getSku());

            String requestId = "low_stock_alert_" + product.getSku() + "_" + System.currentTimeMillis();

            // 发送给采购团队
            SendNotificationResponse response = notificationClient.sendToGroup(
                    requestId,
                    "LOW_STOCK_ALERT",
                    "PROCUREMENT_TEAM",
                    params
            );

            log.info("库存不足警告发送完成: sku={}, currentStock={}, status={}", 
                    product.getSku(), product.getCurrentStock(), response.getStatus());

            // 如果是热销商品，同时通知管理层
            if (product.isHotSelling()) {
                notifyManagementLowStock(product);
            }

        } catch (Exception e) {
            log.error("发送库存不足警告失败: sku={}", product.getSku(), e);
        }
    }

    /**
     * 通知管理层库存不足
     */
    private void notifyManagementLowStock(ProductInfo product) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("productName", product.getProductName());
            params.put("productSku", product.getSku());
            params.put("currentStock", product.getCurrentStock());
            params.put("dailySales", product.getDailySales());
            params.put("estimatedStockoutDays", calculateStockoutDays(product));
            params.put("revenueImpact", calculateRevenueImpact(product));

            String requestId = "management_stock_alert_" + product.getSku() + "_" + System.currentTimeMillis();

            SendNotificationResponse response = notificationClient.sendToGroup(
                    requestId,
                    "MANAGEMENT_STOCK_ALERT",
                    "MANAGEMENT_TEAM",
                    params
            );

            log.info("管理层库存警告发送完成: sku={}, status={}", product.getSku(), response.getStatus());

        } catch (Exception e) {
            log.error("发送管理层库存警告失败: sku={}", product.getSku(), e);
        }
    }

    /**
     * 批量发送订单提醒
     * 场景：定时任务提醒用户未完成的订单
     */
    public CompletableFuture<Void> sendPendingOrderReminders(List<OrderInfo> pendingOrders) {
        return CompletableFuture.runAsync(() -> {
            log.info("开始发送待付款订单提醒，订单数量: {}", pendingOrders.size());

            for (OrderInfo order : pendingOrders) {
                try {
                    Map<String, Object> params = new HashMap<>();
                    params.put("userName", order.getUserName());
                    params.put("orderNo", order.getOrderNo());
                    params.put("totalAmount", order.getTotalAmount().toString());
                    params.put("createdTime", order.getCreatedTime().toString());
                    params.put("expiryTime", order.getExpiryTime().toString());
                    params.put("paymentLink", "https://shop.company.com/pay/" + order.getOrderNo());
                    params.put("remainingHours", calculateRemainingHours(order.getExpiryTime()));

                    String requestId = "pending_order_reminder_" + order.getOrderNo() + "_" + System.currentTimeMillis();

                    SendNotificationResponse response = notificationClient.sendToUser(
                            requestId,
                            "PENDING_ORDER_REMINDER",
                            order.getUserId(),
                            params
                    );

                    log.info("待付款订单提醒发送完成: orderNo={}, status={}", order.getOrderNo(), response.getStatus());

                    // 控制发送频率
                    Thread.sleep(200);

                } catch (Exception e) {
                    log.error("发送待付款订单提醒失败: orderNo={}", order.getOrderNo(), e);
                }
            }

            log.info("待付款订单提醒发送完成");
        });
    }

    // 辅助方法
    private String getStatusDescription(String status) {
        switch (status) {
            case "SHIPPED": return "商品已发货";
            case "IN_TRANSIT": return "运输中";
            case "OUT_FOR_DELIVERY": return "派送中";
            case "DELIVERED": return "已送达";
            default: return "状态更新";
        }
    }

    private String getShippingTemplateCode(String status) {
        switch (status) {
            case "SHIPPED": return "ORDER_SHIPPED";
            case "IN_TRANSIT": return "ORDER_IN_TRANSIT";
            case "OUT_FOR_DELIVERY": return "ORDER_OUT_FOR_DELIVERY";
            case "DELIVERED": return "ORDER_DELIVERED";
            default: return "SHIPPING_STATUS_UPDATE";
        }
    }

    private int calculateStockoutDays(ProductInfo product) {
        if (product.getDailySales() <= 0) return 999;
        return product.getCurrentStock() / product.getDailySales();
    }

    private BigDecimal calculateRevenueImpact(ProductInfo product) {
        return product.getPrice().multiply(BigDecimal.valueOf(product.getDailySales() * 7));
    }

    private long calculateRemainingHours(LocalDateTime expiryTime) {
        return java.time.Duration.between(LocalDateTime.now(), expiryTime).toHours();
    }

    // 数据类定义
    public static class OrderInfo {
        private String orderNo;
        private String userId;
        private String userName;
        private String phone;
        private String email;
        private BigDecimal totalAmount;
        private List<String> items;
        private LocalDateTime createdTime;
        private LocalDateTime expectedDelivery;
        private LocalDateTime expiryTime;
        private boolean vipUser;
        private String vipLevel;

        // 构造函数和getter/setter省略
        public String getOrderNo() { return orderNo; }
        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public List<String> getItems() { return items; }
        public LocalDateTime getCreatedTime() { return createdTime; }
        public LocalDateTime getExpectedDelivery() { return expectedDelivery; }
        public LocalDateTime getExpiryTime() { return expiryTime; }
        public boolean isVipUser() { return vipUser; }
        public String getVipLevel() { return vipLevel; }
    }

    public static class PaymentInfo {
        private String transactionId;
        private String orderNo;
        private String userId;
        private String userName;
        private String phone;
        private BigDecimal amount;
        private String paymentMethod;
        private LocalDateTime paymentTime;

        // getter方法省略
        public String getTransactionId() { return transactionId; }
        public String getOrderNo() { return orderNo; }
        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getPhone() { return phone; }
        public BigDecimal getAmount() { return amount; }
        public String getPaymentMethod() { return paymentMethod; }
        public LocalDateTime getPaymentTime() { return paymentTime; }
    }

    public static class ShippingInfo {
        private String trackingNo;
        private String orderNo;
        private String userId;
        private String userName;
        private String phone;
        private String email;
        private String status;
        private LocalDateTime updateTime;

        // getter方法省略
        public String getTrackingNo() { return trackingNo; }
        public String getOrderNo() { return orderNo; }
        public String getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
        public LocalDateTime getUpdateTime() { return updateTime; }
    }

    public static class PromotionInfo {
        private String promotionId;
        private String title;
        private String discount;
        private String promotionCode;
        private LocalDateTime validUntil;

        // getter方法省略
        public String getPromotionId() { return promotionId; }
        public String getTitle() { return title; }
        public String getDiscount() { return discount; }
        public String getPromotionCode() { return promotionCode; }
        public LocalDateTime getValidUntil() { return validUntil; }
    }

    public static class ProductInfo {
        private String sku;
        private String productName;
        private int currentStock;
        private int lowStockThreshold;
        private String category;
        private String supplier;
        private LocalDateTime lastSaleDate;
        private boolean hotSelling;
        private int dailySales;
        private BigDecimal price;

        // getter方法省略
        public String getSku() { return sku; }
        public String getProductName() { return productName; }
        public int getCurrentStock() { return currentStock; }
        public int getLowStockThreshold() { return lowStockThreshold; }
        public String getCategory() { return category; }
        public String getSupplier() { return supplier; }
        public LocalDateTime getLastSaleDate() { return lastSaleDate; }
        public boolean isHotSelling() { return hotSelling; }
        public int getDailySales() { return dailySales; }
        public BigDecimal getPrice() { return price; }
    }
}
