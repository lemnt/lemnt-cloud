package com.lemnt.system.application.notice.executor;

import com.lemnt.system.domain.notice.gateway.NoticeReadGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 批量标记公告已读命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class NoticeMarkReadAllCmdExe {
    private final NoticeReadGateway noticeReadGateway;

    /**
     * 执行批量标记已读
     *
     * @param userId    当前用户ID
     * @param noticeIds 公告ID数组
     */
    public Boolean execute(Long userId, List<Long> noticeIds) {
        return noticeReadGateway.markReadBatch(userId, noticeIds);
    }
}
