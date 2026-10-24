package com.lemnt.third.sdk.demo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 第三方 HTTP 调用示例，不绑定具体业务平台。
 */
public class DemoClient {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * 发起 GET 请求，保留响应状态码与正文供调用方处理。
     *
     * @param endpoint 完整的目标接口地址
     * @return HTTP 响应
     */
    public HttpResponse<String> get(URI endpoint) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(endpoint)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
