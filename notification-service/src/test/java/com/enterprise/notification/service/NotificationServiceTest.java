package com.enterprise.notification.service;

import com.enterprise.notification.common.dto.SendNotificationRequest;
import com.enterprise.notification.common.dto.SendNotificationResponse;
import com.enterprise.notification.mapper.NotificationTemplateMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 通知服务测试类
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationTemplateMapper templateMapper;

//    @Test
    public void testSendIndividualNotification() {
        // 准备测试数据
        SendNotificationRequest request = new SendNotificationRequest();
        request.setRequestId("test_req_001");
        request.setTemplateCode("USER_REGISTER_WELCOME");

        SendNotificationRequest.RecipientInfo recipient = new SendNotificationRequest.RecipientInfo();
        recipient.setType("individual");
        recipient.setId("test_user_001");

        SendNotificationRequest.ContactInfo contactInfo = new SendNotificationRequest.ContactInfo();
        contactInfo.setUserName("测试用户");
        contactInfo.setPhone("13800138000");
        contactInfo.setEmail("test@example.com");
        recipient.setContactInfo(contactInfo);

        request.setRecipient(recipient);

        Map<String, Object> params = new HashMap<>();
        params.put("userName", "测试用户");
        request.setTemplateParams(params);

        // 执行测试
        SendNotificationResponse response = notificationService.sendNotification(request);

        // 验证结果
        assertNotNull(response);
        assertEquals("test_req_001", response.getRequestId());
        assertNotNull(response.getResults());
        assertTrue(!response.getResults().isEmpty());
    }

    @Test
    public void testIdempotency() {
        // 准备测试数据
        SendNotificationRequest request = new SendNotificationRequest();
        request.setRequestId("test_req_002");
        request.setTemplateCode("USER_REGISTER_WELCOME");

        SendNotificationRequest.RecipientInfo recipient = new SendNotificationRequest.RecipientInfo();
        recipient.setType("individual");
        recipient.setId("test_user_002");

        SendNotificationRequest.ContactInfo contactInfo = new SendNotificationRequest.ContactInfo();
        contactInfo.setUserName("测试用户2");
        recipient.setContactInfo(contactInfo);

        request.setRecipient(recipient);

        Map<String, Object> params = new HashMap<>();
        params.put("userName", "测试用户2");
        request.setTemplateParams(params);

        // 第一次发送
        SendNotificationResponse response1 = notificationService.sendNotification(request);

        // 第二次发送（幂等性测试）
        SendNotificationResponse response2 = notificationService.sendNotification(request);

        // 验证幂等性
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(response1.getRequestId(), response2.getRequestId());
        assertEquals(response1.getResults().size(), response2.getResults().size());
    }
}
