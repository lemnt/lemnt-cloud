package com.lemnt.member.application.growth.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.AssetRecordDTO;
import com.lemnt.member.application.growth.qry.AssetRecordAllPageQry;
import com.lemnt.member.domain.growth.gateway.MemberPointRecordGateway;
import com.lemnt.member.domain.growth.gateway.MemberExperienceRecordGateway;
import com.lemnt.member.domain.growth.model.AssetRecordPageFilter;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/** 会员资产流水分页执行器。 */
@Component
@RequiredArgsConstructor
public class MemberAssetRecordAllPageQueryExe {

    private final MemberPointRecordGateway pointRecords;
    private final MemberExperienceRecordGateway experienceRecords;

    /** 查询资产流水分页，沿用资源归属和分页约束。 */
    public PageResult<AssetRecordDTO> execute(AssetRecordAllPageQry qry) {
        MemberRules.page(qry.pageNum(), qry.pageSize());
        if (qry.memberId() != null) {
            MemberRules.id(qry.memberId());
        }
        if (!Set.of("POINT", "EXPERIENCE").contains(qry.assetType())) {
            throw new ServiceException("资产类型不合法", 400);
        }
        if ((qry.nickname()!=null && qry.nickname().length()>80) || (qry.bizType()!=null && qry.bizType().length()>40))
            throw new ServiceException("积分筛选条件超长",400);
        var nickname=qry.nickname()==null || qry.nickname().isBlank()? null:qry.nickname().trim();
        var bizType=qry.bizType()==null || qry.bizType().isBlank()? null:qry.bizType().trim();
        if (qry.pointBizType()!=null && (!"POINT".equals(qry.assetType())
                || !com.lemnt.member.domain.growth.model.PointRecordMetadata.supported(qry.pointBizType())))
            throw new ServiceException("积分业务类型不合法",400);
        var filter = new AssetRecordPageFilter(qry.memberId(), qry.assetType(),
                (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize(),nickname,bizType,qry.pointBizType());
        return new PageResult<>(
                ("POINT".equals(qry.assetType()) ? pointRecords.page(filter) : experienceRecords.page(filter)).stream().map(MemberGrowthDTOConverter::toAssetRecordDTO).toList(),
                ("POINT".equals(qry.assetType()) ? pointRecords.count(filter) : experienceRecords.count(filter)));
    }
}
