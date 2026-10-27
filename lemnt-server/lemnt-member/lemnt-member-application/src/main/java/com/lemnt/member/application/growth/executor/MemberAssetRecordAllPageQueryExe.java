package com.lemnt.member.application.growth.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.AssetRecordDTO;
import com.lemnt.member.application.growth.qry.AssetRecordAllPageQry;
import com.lemnt.member.domain.growth.gateway.MemberAssetRecordGateway;
import com.lemnt.member.domain.growth.model.AssetRecordPageFilter;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/** 会员资产流水分页执行器。 */
@Component
@RequiredArgsConstructor
public class MemberAssetRecordAllPageQueryExe {

    private final MemberAssetRecordGateway memberAssetRecordGateway;

    /** 查询资产流水分页，沿用资源归属和分页约束。 */
    public PageResult<AssetRecordDTO> execute(AssetRecordAllPageQry qry) {
        MemberRules.page(qry.pageNum(), qry.pageSize());
        if (qry.memberId() != null) {
            MemberRules.id(qry.memberId());
        }
        if (!Set.of("POINT", "EXPERIENCE").contains(qry.assetType())) {
            throw new ServiceException("资产类型不合法", 400);
        }
        var filter = new AssetRecordPageFilter(qry.memberId(), qry.assetType(),
                (qry.pageNum() - 1) * qry.pageSize(), qry.pageSize());
        return new PageResult<>(
                memberAssetRecordGateway.assets(filter).stream().map(MemberGrowthDTOConverter::toAssetRecordDTO).toList(),
                memberAssetRecordGateway.assetsCount(filter));
    }
}
