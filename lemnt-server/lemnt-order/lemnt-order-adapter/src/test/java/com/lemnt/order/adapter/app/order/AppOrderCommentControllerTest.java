package com.lemnt.order.adapter.app.order;

import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.common.security.interceptor.HeaderInterceptor;
import com.lemnt.order.application.order.cmd.OrderCommentCreateCmd;
import com.lemnt.order.application.order.executor.OrderCommentCreateCmdExe;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 投稿身份与昵称快照只能来自登录上下文，请求体伪造字段必须被忽略。
 */
class AppOrderCommentControllerTest {
    @AfterEach
    void clear() {
        SecurityContextHolder.remove();
    }

    @Test
    void reviewerIdentityAndSnapshotComeFromContextNotBody() throws Exception {
        var create = mock(OrderCommentCreateCmdExe.class);
        when(create.execute(any())).thenReturn(1L);
        var mvc =
            MockMvcBuilders.standaloneSetup(new AppOrderCommentController(create))
                .addInterceptors(new HeaderInterceptor())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                    new JacksonJsonHttpMessageConverter(JsonMapper.builder().build()))
                .build();
        mvc.perform(
                post("/mall/app/comments")
                    .header(SecurityConstants.HEADER_USER_ID, "7")
                    .header(SecurityConstants.HEADER_USER_NICK_NAME, "真实昵称")
                    .header(SecurityConstants.HEADER_USER_AVATAR, "https://example.test/a.png")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"memberId\":999,\"userNickname\":\"伪造昵称\",\"orderNo\":\"TR1\",\"skuId\":9,\"score\":5,\"content\":\"真实评价\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value("1"));
        var captured = ArgumentCaptor.forClass(OrderCommentCreateCmd.class);
        verify(create).execute(captured.capture());
        var cmd = captured.getValue();
        assertEquals(7L, cmd.memberId());
        assertEquals("真实昵称", cmd.userNickname());
        assertEquals("https://example.test/a.png", cmd.userAvatar());
        mvc.perform(
                post("/mall/app/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"orderNo\":\"TR1\",\"skuId\":9,\"score\":5,\"content\":\" \"}"))
            .andExpect(status().is(422));
    }
}
