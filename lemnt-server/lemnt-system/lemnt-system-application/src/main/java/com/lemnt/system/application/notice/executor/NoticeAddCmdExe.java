package com.lemnt.system.application.notice.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.notice.cmd.NoticeAddCmd;
import com.lemnt.system.domain.notice.gateway.NoticeGateway;
import com.lemnt.system.domain.notice.model.Notice;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 新增通知公告命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class NoticeAddCmdExe {
    private final NoticeGateway noticeGateway;

    /**
     * 执行新增通知公告
     *
     * @param cmd 命令对象
     * @return 影响行数
     */
    public Boolean execute(NoticeAddCmd cmd) {
        Notice entity = new Notice();
        entity.setNoticeTitle(cmd.noticeTitle());
        entity.setNoticeType(cmd.noticeType());
        entity.setNoticeContent(cmd.noticeContent());
        entity.setStatus(cmd.status());
        entity.setCreateBy(SecurityUtils.getUsername());
        entity.setRemark(cmd.remark());
        entity.validateForPublish();
        return noticeGateway.insert(entity);
    }
}
