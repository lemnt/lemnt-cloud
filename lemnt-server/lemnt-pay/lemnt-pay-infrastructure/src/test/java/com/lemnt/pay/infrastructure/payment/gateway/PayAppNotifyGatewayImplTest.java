package com.lemnt.pay.infrastructure.payment.gateway;

import com.lemnt.common.core.utils.JsonUtils;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayAppNotifyGatewayImplTest {
    @Test
    void postsJsonAndRequiresAnAcknowledgementFromAnAllowedOrigin() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var received = new AtomicReference<String>();
        Map<String, String> payload =
                Map.of("orderNo", "O1", "paymentNo", "P1", "status", "SUCCESS");
        String origin = "http://127.0.0.1:" + server.getAddress().getPort();
        server.createContext(
                "/success",
                exchange -> {
                    try (exchange) {
                        received.set(
                                exchange.getRequestMethod()
                                        + " "
                                        + exchange.getRequestHeaders().getFirst("Content-Type")
                                        + " "
                                        + new String(
                                                exchange.getRequestBody().readAllBytes(),
                                                StandardCharsets.UTF_8));
                        byte[] response = "{\"code\":200}".getBytes(StandardCharsets.UTF_8);
                        exchange.sendResponseHeaders(200, response.length);
                        exchange.getResponseBody().write(response);
                    }
                });
        server.createContext(
                "/rejected",
                exchange -> {
                    try (exchange) {
                        byte[] response = "{\"code\":500}".getBytes(StandardCharsets.UTF_8);
                        exchange.sendResponseHeaders(200, response.length);
                        exchange.getResponseBody().write(response);
                    }
                });
        server.createContext(
                "/redirect",
                exchange -> {
                    try (exchange) {
                        exchange.getResponseHeaders().set("Location", origin + "/success");
                        exchange.sendResponseHeaders(307, -1);
                    }
                });
        server.start();
        try {
            var sender = new PayAppNotifyGatewayImpl(origin);
            assertThrows(
                    IllegalStateException.class,
                    () -> new PayAppNotifyGatewayImpl("").post(origin + "/success", payload));
            assertThrows(
                    IllegalStateException.class, () -> sender.post(origin + "/redirect", payload));
            assertNull(received.get());
            assertThrows(
                    IllegalStateException.class, () -> sender.post(origin + "/rejected", payload));
            sender.post(origin + "/success", payload);
            String[] request = received.get().split(" ", 3);
            assertEquals("POST", request[0]);
            assertEquals("application/json", request[1]);
            assertEquals(payload, JsonUtils.toMap(request[2]));
        } finally {
            server.stop(0);
        }
    }
}
