package com.lemnt.system.application.notice.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.notice.cmd.NoticeUpdateCmd;
import com.lemnt.system.domain.notice.gateway.NoticeGateway;
import com.lemnt.system.domain.notice.model.Notice;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 修改通知公告命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class NoticeUpdateCmdExe {
    private final NoticeGateway noticeGateway;

    /**
     * 执行修改通知公告
     *
     * @param cmd 命令对象
     * @return 影响行数
     */
    public Boolean execute(NoticeUpdateCmd cmd) {
        Notice entity = new Notice();
        entity.setNoticeId(cmd.noticeId());
        entity.setNoticeTitle(cmd.noticeTitle());
        entity.setNoticeType(cmd.noticeType());
        entity.setNoticeContent(cmd.noticeContent());
        entity.setStatus(cmd.status());
        entity.setUpdateBy(SecurityUtils.getUsername());

        // 完整编辑表单省略空备注时，仍需清除数据库旧值。
        entity.setRemark(cmd.remark() == null ? "" : cmd.remark());
        return noticeGateway.update(entity);
    }
}
