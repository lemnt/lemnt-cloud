package com.lemnt.member.application.growth.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.AssetRecordDTO;
import com.lemnt.member.application.growth.qry.ExperienceRecordAllPageQry;
import com.lemnt.member.domain.growth.gateway.MemberExperienceRecordGateway;
import com.lemnt.member.domain.growth.model.AssetRecordPageFilter;
import com.lemnt.member.domain.growth.model.ExperienceRecordMetadata;
import com.lemnt.member.domain.shared.model.MemberRules;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 后台经验记录专用查询，列表和总数使用同一条件。 */
@Component
@RequiredArgsConstructor
public class MemberExperienceRecordAllPageQueryExe {

    private final MemberExperienceRecordGateway experienceRecords;

    /** 校验经验筛选并读取统一分页，不根据现有业务码推断历史类型。 */
    public PageResult<AssetRecordDTO> execute(ExperienceRecordAllPageQry qry) {
        MemberRules.page(qry.pageNum(), qry.pageSize());
        if (qry.memberId() != null) {
            MemberRules.id(qry.memberId());
        }
        if (qry.experienceBizType() != null && !ExperienceRecordMetadata.supported(qry.experienceBizType())) {
            throw new ServiceException("经验业务类型不合法", 400);
        }
        if ((qry.bizNo() != null && qry.bizNo().length() > 64)
                || (qry.title() != null && qry.title().length() > 255)) {
            throw new ServiceException("经验筛选条件超长", 400);
        }
        if (qry.startTime() != null && qry.endTime() != null && qry.startTime().isAfter(qry.endTime())) {
            throw new ServiceException("开始时间不能晚于结束时间", 400);
        }
        var filter = new AssetRecordPageFilter(qry.memberId(), "EXPERIENCE",
                (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize(), null, null, null,
                qry.bizNo() == null || qry.bizNo().isBlank() ? null : qry.bizNo().trim(),
                qry.experienceBizType(),
                qry.title() == null || qry.title().isBlank() ? null : qry.title().trim(),
                qry.startTime(), qry.endTime());
        return new PageResult<>(
                experienceRecords.page(filter).stream().map(MemberGrowthDTOConverter::toAssetRecordDTO).toList(),
                experienceRecords.count(filter));
    }
}
