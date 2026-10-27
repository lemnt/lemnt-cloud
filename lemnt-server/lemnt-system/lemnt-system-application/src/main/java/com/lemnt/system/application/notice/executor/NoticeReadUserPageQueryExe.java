package com.lemnt.system.application.notice.executor;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.application.notice.converter.NoticeReadConverter;
import com.lemnt.system.application.notice.dto.NoticeReadUserDTO;
import com.lemnt.system.application.notice.qry.NoticeReadUserPageQry;
import com.lemnt.system.domain.notice.gateway.NoticeReadGateway;
import com.lemnt.system.domain.notice.model.NoticeReadUser;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 首页顶部公告列表查询执行器（携带已读状态）
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class NoticeReadUserPageQueryExe {
    private final NoticeReadGateway noticeReadGateway;
    public PageResult<NoticeReadUserDTO> execute(NoticeReadUserPageQry qry) {
        PageResult<NoticeReadUser> result = noticeReadGateway.selectPageReadUsers(
                qry.noticeId(), qry.searchValue(), new PageQry(qry.pageNum(), qry.pageSize()));
        return PageResult.of(NoticeReadConverter.toDTOList(result.getRecords()), result.getTotal());
    }
}
