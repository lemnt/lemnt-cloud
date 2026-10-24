package com.lemnt.gateway.utils;

import com.lemnt.common.core.utils.JsonUtils;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ServletUtilsTest {

    @Test
    void shouldReturnUnauthorizedBusinessCodeInsteadOfEnumData() {
        MockServerHttpResponse response = new MockServerHttpResponse();

        ServletUtils.unauthorized(response, "登录状态已过期").block();

        var body = JsonUtils.toJsonObj(response.getBodyAsString().block());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(body.getIntValue("code")).isEqualTo(401);
        assertThat(body.getString("msg")).isEqualTo("登录状态已过期");
        assertThat(body.get("data")).isNull();
    }
}
