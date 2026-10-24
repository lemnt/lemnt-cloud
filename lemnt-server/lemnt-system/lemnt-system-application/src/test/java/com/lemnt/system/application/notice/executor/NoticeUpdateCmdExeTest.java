package com.lemnt.system.application.notice.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.notice.cmd.NoticeUpdateCmd;
import com.lemnt.system.domain.notice.gateway.NoticeGateway;
import com.lemnt.system.domain.notice.model.Notice;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 完整编辑提交空备注时应清除旧值，非空备注应保持原文。 */
class NoticeUpdateCmdExeTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "修改公告备注")
    void shouldWriteClearedOrPopulatedRemark(String remark) {
        NoticeGateway gateway = mock(NoticeGateway.class);
        when(gateway.update(any())).thenReturn(true);
        try (var security = mockStatic(SecurityUtils.class)) {
            assertThat(new NoticeUpdateCmdExe(gateway).execute(new NoticeUpdateCmd(
                    11L, "测试公告", "2", "<p>正文</p>", "2", remark))).isTrue();
        }
        ArgumentCaptor<Notice> entity = ArgumentCaptor.forClass(Notice.class);
        verify(gateway).update(entity.capture());
        assertThat(entity.getValue().getRemark()).isEqualTo(remark == null ? "" : remark);
        assertThat(entity.getValue().getNoticeContent()).isEqualTo("<p>正文</p>");
        assertThat(entity.getValue().getStatus()).isEqualTo("2");
    }
}
