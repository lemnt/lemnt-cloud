package com.lemnt.common.security.interceptor;

import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.security.utils.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证通用用户上下文的传递和请求结束清理。 */
class HeaderInterceptorTest {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.remove();
    }

    @Test
    void shouldCarryUserIdentityAndIgnoreLegacyBusinessHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SecurityConstants.HEADER_USER_ID, "101");
        request.addHeader(SecurityConstants.HEADER_USER_NAME, "tester");
        request.addHeader("X-Agent-Id", "37");
        request.addHeader("X-Advertiser-Id", "91");

        new HeaderInterceptor().preHandle(request, new MockHttpServletResponse(),
                new HandlerMethod(new ProbeController(), ProbeController.class.getMethod("detail")));

        assertThat(SecurityUtils.getUserId()).isEqualTo(101L);
        assertThat(SecurityUtils.getUsername()).isEqualTo("tester");
        assertThat(SecurityContextHolder.getLocalMap()).doesNotContainKeys("agent_id", "advertiser_id");
    }

    @Test
    void shouldClearUserContextAfterCompletion() {
        SecurityContextHolder.setUserId("101");

        new HeaderInterceptor().afterCompletion(new MockHttpServletRequest(),
                new MockHttpServletResponse(), new ProbeController(), null);

        assertThat(SecurityUtils.getUserId()).isZero();
        assertThat(SecurityUtils.getLoginUser()).isNull();
    }

    @Test
    void shouldClearUserContextWhenAsyncHandlingStarts() {
        SecurityContextHolder.setUserId("101");

        new HeaderInterceptor().afterConcurrentHandlingStarted(new MockHttpServletRequest(),
                new MockHttpServletResponse(), new ProbeController());

        assertThat(SecurityUtils.getUserId()).isZero();
        assertThat(SecurityUtils.getLoginUser()).isNull();
    }

    public static class ProbeController {
        public void detail() {
        }
    }
}
