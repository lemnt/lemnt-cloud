package com.lemnt.system.application.notice.executor;

import com.lemnt.system.application.notice.dto.NoticeDTO;
import com.lemnt.system.domain.notice.gateway.NoticeReadGateway;
import com.lemnt.system.domain.notice.model.Notice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NoticeTopListQueryExeTest {

    private final NoticeReadGateway gateway = mock(NoticeReadGateway.class);
    private final NoticeTopListQueryExe executor = new NoticeTopListQueryExe(gateway);

    @Test
    void shouldReturnZeroUnreadEvenWhenReadNoticesRemainInList() {
        when(gateway.selectNoticeListWithReadStatus(1L, 5)).thenReturn(List.of(
                new Notice().setNoticeId(10L).setIsRead(true),
                new Notice().setNoticeId(11L).setIsRead(true)));
        when(gateway.selectUnreadCount(1L)).thenReturn(0);

        Map<String, Object> result = executor.execute(1L, 5);

        assertThat(result.get("unreadCount")).isEqualTo(0);
        assertThat(notices(result)).extracting(NoticeDTO::getIsRead).containsExactly(true, true);
    }

    @Test
    void shouldPreserveMixedReadStatusesAndCountUnreadBeyondVisibleList() {
        when(gateway.selectNoticeListWithReadStatus(1L, 5)).thenReturn(List.of(
                new Notice().setNoticeId(10L).setIsRead(true),
                new Notice().setNoticeId(11L).setIsRead(false)));
        when(gateway.selectUnreadCount(1L)).thenReturn(8);

        Map<String, Object> result = executor.execute(1L, 5);

        assertThat(result.get("unreadCount")).isEqualTo(8);
        assertThat(notices(result)).extracting(NoticeDTO::getNoticeId).containsExactly(10L, 11L);
        assertThat(notices(result)).extracting(NoticeDTO::getIsRead).containsExactly(true, false);
    }

    @Test
    void shouldReturnEmptyListAndZeroUnreadWhenThereAreNoNotices() {
        when(gateway.selectNoticeListWithReadStatus(1L, 5)).thenReturn(List.of());
        when(gateway.selectUnreadCount(1L)).thenReturn(0);

        Map<String, Object> result = executor.execute(1L, 5);

        assertThat(result.get("unreadCount")).isEqualTo(0);
        assertThat(notices(result)).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private List<NoticeDTO> notices(Map<String, Object> result) {
        return (List<NoticeDTO>) result.get("list");
    }
}
