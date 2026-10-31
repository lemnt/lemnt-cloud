package com.lemnt.system.infrastructure.file.gateway;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.oss.configure.MinioConfig;
import com.lemnt.common.oss.service.OssFileService;
import com.lemnt.common.oss.service.OssFileValidator;
import com.lemnt.system.domain.file.model.UploadFile;

import com.sun.net.httpserver.HttpServer;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileGatewayImplTest {
    @Test
    void shouldUploadThroughRealMinioClientWithoutDependencyLinkageErrors() throws Exception {
        // 本地 S3 响应替身验证实际 HTTP 依赖链，不向真实 OSS 写入测试对象。
        var received = new AtomicReference<byte[]>();

        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

        server.createContext("/test-assets/", exchange -> {
                received.set(exchange.getRequestBody().readAllBytes());

                exchange.getResponseHeaders().set("ETag", "\"test-etag\"");

                exchange.sendResponseHeaders(200, -1);

                exchange.close();

                });

        server.start();

        try {
            var config = new MinioConfig();

            config.setBucketName("test-assets");

            config.setUrl("https://oss.example.test");

            var client = MinioClient.builder()
                    .endpoint("http://127.0.0.1:" + server.getAddress().getPort())
                    .credentials("test-access", "test-secret")
                    .region("us-east-1")
                    .build();

            var gateway = new FileGatewayImpl(new OssFileService(config, client, new OssFileValidator()));

            var bytes = new byte[]{
                1, 2
            };

            var url = gateway.upload(new UploadFile(new ByteArrayInputStream(bytes), "image.png", "image/png", 2));

            assertTrue(url.startsWith("https://oss.example.test/test-assets/"));

            assertTrue(url.endsWith(".png"));

            assertArrayEquals(bytes, received.get());

        } finally {
            server.stop(0);

        }
    }

    @Test
    void shouldDelegateStreamAndMetadataToOss() throws Exception {
        var oss = mock(OssFileService.class);

        var content = new ByteArrayInputStream(new byte[]{
                1
                });

        when(oss.uploadStream(content, "image.png", "image/png", 1))
                .thenReturn("https://oss.example.test/image.png");

        assertEquals("https://oss.example.test/image.png", new FileGatewayImpl(oss)
                .upload(new UploadFile(content, "image.png", "image/png", 1)));

        verify(oss).uploadStream(content, "image.png", "image/png", 1);

    }

    @Test
    void shouldNotReturnUrlWhenOssFails() throws Exception {
        var oss = mock(OssFileService.class);

        var content = new ByteArrayInputStream(new byte[]{
                1
                });

        when(oss.uploadStream(content, "image.png", "image/png", 1))
                .thenThrow(new IOException("存储服务不可用"));

        assertThrows(ServiceException.class, () -> new FileGatewayImpl(oss)
                .upload(new UploadFile(content, "image.png", "image/png", 1)));

    }
}
