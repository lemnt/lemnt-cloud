package com.lemnt.member.application.user.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.member.application.user.converter.MemberUserDTOConverter;
import com.lemnt.member.application.user.dto.TagDTO;
import com.lemnt.member.application.user.qry.TagPageQry;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberTagGateway;
import com.lemnt.member.domain.user.model.TagPageFilter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员标签分页执行器。 */
@Component
@RequiredArgsConstructor
public class MemberTagPageQueryExe {

    private final MemberTagGateway memberTagGateway;

    /** 查询标签分页，沿用资源归属和分页约束。 */
    public PageResult<TagDTO> execute(TagPageQry qry) {
        MemberRules.page(qry.pageNum(), qry.pageSize());
        if (qry.startTime() != null && qry.endTime() != null && qry.startTime().isAfter(qry.endTime())) {
            throw new com.lemnt.common.core.exception.ServiceException("开始时间不能晚于结束时间", 400);
        }
        var filter = new TagPageFilter(MemberRules.optional(qry.name(), 100),
                (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize(), qry.startTime(), qry.endTime());
        return new PageResult<>(
                memberTagGateway.tags(filter).stream().map(MemberUserDTOConverter::toTagDTO).toList(),
                memberTagGateway.tagsCount(filter));
    }
}
