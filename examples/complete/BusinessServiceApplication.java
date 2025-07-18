package com.enterprise.business;

import com.enterprise.notification.client.NotificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 业务服务启动类 - 集成通知平台的完整示例
 * 
 * 展示了如何在Spring Boot应用中集成通知客户端SDK
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class BusinessServiceApplication implements CommandLineRunner {

    @Autowired
    private NotificationClient notificationClient;

    public static void main(String[] args) {
        SpringApplication.run(BusinessServiceApplication.class, args);
    }

    /**
     * 应用启动后执行
     * 验证通知服务连接状态
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("业务服务启动完成，开始验证通知服务连接...");
        
        try {
            boolean isHealthy = notificationClient.isHealthy();
            if (isHealthy) {
                log.info("✅ 通知服务连接正常");
            } else {
                log.warn("⚠️ 通知服务连接异常，请检查配置");
            }
        } catch (Exception e) {
            log.error("❌ 通知服务连接失败", e);
        }
        
        log.info("业务服务已就绪，可以开始处理业务请求");
    }

    /**
     * 配置异步执行器
     * 用于异步发送通知，避免阻塞主业务流程
     */
    @Bean(name = "notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("NotificationAsync-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        
        log.info("通知异步执行器配置完成: corePoolSize=5, maxPoolSize=20, queueCapacity=100");
        return executor;
    }

    /**
     * 配置通知客户端Bean（可选）
     * 如果需要自定义配置，可以覆盖自动配置
     */
    // @Bean
    // @Primary
    // public NotificationClient customNotificationClient() {
    //     NotificationClientProperties properties = new NotificationClientProperties();
    //     properties.setBaseUrl("http://custom-notification-service:8080/notification-service");
    //     properties.setConnectTimeout(5000);
    //     properties.setReadTimeout(30000);
    //     
    //     RestTemplate restTemplate = new RestTemplate();
    //     return new NotificationClientImpl(restTemplate, properties);
    // }
}
