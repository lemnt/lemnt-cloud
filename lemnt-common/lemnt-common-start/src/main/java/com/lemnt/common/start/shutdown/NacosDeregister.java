package com.lemnt.common.start.shutdown;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 应用实例从 Nacos 服务注册中心摘除
 *
 * @author wangxx
 */
@Component
public class NacosDeregister {

    private static final Logger logger = LoggerFactory.getLogger(NacosDeregister.class);

    private final String serverAddr;
    private final String namespace;
    private final String groupName;
    private final String serviceName;
    private final int port;
    private final long waitTime;

    public NacosDeregister(
            @Value("${spring.cloud.nacos.discovery.server-addr}") String serverAddr,
            @Value("${spring.cloud.nacos.discovery.namespace:}") String namespace,
            @Value("${spring.cloud.nacos.discovery.group:DEFAULT_GROUP}") String groupName,
            @Value("${spring.application.name}") String serviceName,
            @Value("${server.port}") int port,
            @Value("${lemnt.shutdown.wait-time:500}") long waitTime  // 等待 Gateway / Nacos 刷新感知
    ) {
        this.serverAddr = serverAddr;
        this.namespace = namespace;
        this.groupName = groupName;
        this.serviceName = serviceName;
        this.port = port;
        this.waitTime = waitTime;
    }

    /**
     * 摘除实例
     */
    @PreDestroy
    public void deregister() {
        try {
            logger.info(">>> 开始 Nacos 下线 [{}:{}]", serviceName, port);

            // 获取本机 IP（优先环境变量 HOST_IP，容器化安全）
            String ip = System.getenv("HOST_IP");
            if (ip == null || ip.isEmpty()) {
                ip = InetAddress.getLocalHost().getHostAddress();
            }

            // 等待 Gateway / Nacos 刷新感知
            TimeUnit.MILLISECONDS.sleep(waitTime);

            // 初始化 Nacos NamingService
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("namespace", namespace);
            NamingService namingService = NacosFactory.createNamingService(properties);
            namingService.deregisterInstance(serviceName, groupName, ip, port);
            logger.info(">>> Nacos 下线成功 [{}:{}]", ip, port);
            namingService.shutDown();
        } catch (NacosException e) {
            logger.error("Nacos 下线异常", e);
        } catch (Exception e) {
            logger.error("通用异常", e);
        }
    }
}
