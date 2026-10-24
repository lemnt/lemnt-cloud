package com.lemnt.system.application.notice.executor;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.notice.converter.NoticeConverter;
import com.lemnt.system.application.notice.dto.NoticeDTO;
import com.lemnt.system.application.notice.qry.NoticePageQry;
import com.lemnt.system.domain.notice.model.Notice;
import com.lemnt.system.domain.notice.gateway.NoticeGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 通知公告分页查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class NoticePageQueryExe {
    private final NoticeGateway noticeGateway;

    /**
     * 执行通知公告分页查询
     *
     * @param qry 查询条件
     * @return 分页结果
     */
    public PageResult<NoticeDTO> execute(NoticePageQry qry) {
        Notice query = new Notice();
        query.setNoticeTitle(qry.noticeTitle());
        query.setNoticeType(qry.noticeType());
        query.setStatus(qry.status());
        query.setCreateBy(SecurityUtils.getUsername());
        PageResult<Notice> pageResult = noticeGateway.selectPage(query, new PageQry(qry.pageNum(), qry.pageSize()));
        List<NoticeDTO> dtoList = NoticeConverter.toDTOList(pageResult.getRecords());
        return PageResult.of(dtoList, pageResult.getTotal());
    }
}
