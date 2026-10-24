package com.lemnt.system.infrastructure.notice.gateway;

import com.lemnt.system.domain.notice.model.Notice;
import com.lemnt.system.domain.notice.model.NoticeRead;
import com.lemnt.system.infrastructure.notice.dataobject.NoticeReadDO;
import com.lemnt.system.infrastructure.notice.mapper.NoticeReadMapper;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NoticeReadGatewayImplTest {

    private final NoticeReadMapper mapper = mock(NoticeReadMapper.class);
    private final NoticeReadGatewayImpl gateway = new NoticeReadGatewayImpl(mapper);

    @Test
    void shouldPreserveReadStatusFromTopListQuery() {
        when(mapper.selectNoticeListWithReadStatus(1L, 5)).thenReturn(List.of(
                new Notice().setNoticeId(10L).setIsRead(true),
                new Notice().setNoticeId(11L).setIsRead(false)));

        assertThat(gateway.selectNoticeListWithReadStatus(1L, 5))
                .extracting(Notice::getIsRead).containsExactly(true, false);
    }

    @Test
    void shouldReturnGlobalUnreadCountFromMapper() {
        when(mapper.selectUnreadCount(1L)).thenReturn(8);

        assertThat(gateway.selectUnreadCount(1L)).isEqualTo(8);
    }

    @Test
    void shouldLeaveBatchPrimaryKeysToDatabaseInsteadOfReusingUserId() {
        assertThat(gateway.markReadBatch(1L, List.of(10L, 11L))).isTrue();

        ArgumentCaptor<List<NoticeReadDO>> records = ArgumentCaptor.captor();
        verify(mapper).insertReadRecords(records.capture());
        assertThat(records.getValue()).extracting(NoticeReadDO::getNoticeId)
                .containsExactly(10L, 11L);
        assertThat(records.getValue()).allSatisfy(record -> {
            assertThat(record.getReadId()).isNull();
            assertThat(record.getUserId()).isEqualTo(1L);
            assertThat(record.getReadTime()).isNotNull();
        });
    }

    @Test
    void shouldAcceptAlreadyReadRecordsWithoutDependingOnAffectedRowCount() {
        when(mapper.insertReadRecords(anyList())).thenReturn(0);

        assertThat(gateway.markReadBatch(1L, List.of(10L))).isTrue();
        assertThat(gateway.insert(new NoticeRead()
                .setReadId(1L)
                .setNoticeId(10L)
                .setUserId(1L)
                .setReadTime(LocalDateTime.now()))).isTrue();
    }

    @Test
    void shouldSkipEmptyBatch() {
        assertThat(gateway.markReadBatch(1L, List.of())).isTrue();
        assertThat(gateway.markReadBatch(1L, null)).isTrue();
        verifyNoInteractions(mapper);
    }
}
