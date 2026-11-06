package com.lemnt.system.application.notice.executor;

import com.lemnt.system.domain.notice.gateway.NoticeReadGateway;
import com.lemnt.system.domain.notice.model.NoticeRead;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标记公告已读命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class NoticeMarkReadCmdExe {
    private final NoticeReadGateway noticeReadGateway;

    /**
     * 执行标记单条公告已读
     *
     * @param noticeId 公告ID
     * @param userId   当前用户ID
     */
    public Boolean execute(Long noticeId, Long userId) {
        if (!noticeReadGateway.isRead(noticeId, userId)) {
            NoticeRead entity = new NoticeRead();
            entity.setNoticeId(noticeId);
            entity.setUserId(userId);
            entity.setReadTime(LocalDateTime.now());
            noticeReadGateway.insert(entity);
        }
        return true;
    }
}
