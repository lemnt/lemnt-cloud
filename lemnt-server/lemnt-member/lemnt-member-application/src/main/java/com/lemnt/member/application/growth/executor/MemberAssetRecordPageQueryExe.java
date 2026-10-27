package com.lemnt.member.application.growth.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.AssetRecordDTO;
import com.lemnt.member.application.growth.qry.AssetRecordPageQry;
import com.lemnt.member.domain.growth.gateway.MemberAssetRecordGateway;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/** 查询会员资产记录分页查询执行器。 */
@Component
@RequiredArgsConstructor
public class MemberAssetRecordPageQueryExe {

    private final MemberAssetRecordGateway memberAssetRecordGateway;

    /**
     * 执行查询会员资产记录分页查询。
     */
    public PageResult<AssetRecordDTO> execute(AssetRecordPageQry qry) {
        MemberRules.id(qry.memberId());
        MemberRules.page(qry.pageNum(), qry.pageSize());
        if (!Set.of("POINT", "EXPERIENCE").contains(qry.assetType())) {
            throw new ServiceException("资产类型不合法", 400);
        }
        return new PageResult<>(
                memberAssetRecordGateway.assets(
                        qry.memberId(),
                        qry.assetType(),
                        (qry.pageNum() - 1) * qry.pageSize(),
                        qry.pageSize())
                .stream()
                .map(MemberGrowthDTOConverter::toAssetRecordDTO)
                .toList(),
                memberAssetRecordGateway.assetCount(qry.memberId(), qry.assetType()));
    }
}
