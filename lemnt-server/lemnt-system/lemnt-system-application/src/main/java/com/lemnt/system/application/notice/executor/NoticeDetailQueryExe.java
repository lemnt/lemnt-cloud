package com.lemnt.system.application.notice.executor;

import com.lemnt.system.application.notice.converter.NoticeConverter;
import com.lemnt.system.application.notice.dto.NoticeDTO;
import com.lemnt.system.domain.notice.gateway.NoticeGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 通知公告详情查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class NoticeDetailQueryExe {
    private final NoticeGateway noticeGateway;

    /**
     * 执行通知公告详情查询
     *
     * @param noticeId 公告ID
     * @return 公告详情
     */
    public NoticeDTO execute(Long noticeId) {
        return NoticeConverter.toDTO(noticeGateway.selectById(noticeId));
    }
}
