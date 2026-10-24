package com.lemnt.system.application.notice.executor;

import com.lemnt.system.domain.notice.gateway.NoticeGateway;
import com.lemnt.system.domain.notice.gateway.NoticeReadGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 删除通知公告命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class NoticeDeleteCmdExe {
    private final NoticeGateway noticeGateway;

    private final NoticeReadGateway noticeReadGateway;

    /**
     * 执行删除通知公告（同时清理已读记录）
     *
     * @param noticeIds 公告ID数组
     * @return 影响行数
     */
    public Boolean execute(List<Long> noticeIds) {
        noticeReadGateway.deleteByNoticeIds(noticeIds);
        return noticeGateway.deleteByIds(noticeIds);
    }
}
