package com.lemnt.gateway.utils;

import com.lemnt.common.core.constant.HttpStatus;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.core.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ServletUtils {

    /**
     * 返回未授权响应
     */
    public static Mono<Void> unauthorized(ServerHttpResponse response, String msg) {
        return error(response, HttpStatus.UNAUTHORIZED, msg);
    }

    /**
     * 保持业务状态码响应约定；只有认证失败使用 401，避免客户端误清除会话。
     */
    public static Mono<Void> error(ServerHttpResponse response, int code, String msg) {
        response.setStatusCode(org.springframework.http.HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        R<?> result = R.fail(code, msg);
        DataBuffer dataBuffer = response.bufferFactory()
                .wrap(JsonUtils.toJsonStr(result).getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(dataBuffer));
    }

    /**
     * 内容编码
     *
     * @param str 内容
     * @return 编码后的内容
     */
    public static String urlEncode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    /**
     * 内容解码
     *
     * @param str 内容
     * @return 解码后的内容
     */
    public static String urlDecode(String str) {
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }
}
