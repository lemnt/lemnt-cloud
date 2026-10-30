package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.AssetRecordDTO;
import com.lemnt.member.application.growth.qry.AssetRecordDetailQry;
import com.lemnt.member.domain.growth.gateway.MemberPointRecordGateway;
import com.lemnt.member.domain.growth.gateway.MemberExperienceRecordGateway;
import com.lemnt.member.domain.shared.model.MemberRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员资产流水详情执行器。 */
@Component
@RequiredArgsConstructor
public class MemberAssetRecordDetailQueryExe {

    private final MemberPointRecordGateway pointRecords;
    private final MemberExperienceRecordGateway experienceRecords;

    /** 查询资产流水详情，沿用资源归属和分页约束。 */
    public AssetRecordDTO execute(AssetRecordDetailQry qry) {
        MemberRules.id(qry.id());
        var record = switch(qry.assetType()) {
            case "POINT" -> pointRecords.detail(qry.id());
            case "EXPERIENCE" -> experienceRecords.detail(qry.id());
            default -> throw new com.lemnt.common.core.exception.ServiceException("资产类型不合法",400);
        };
        if (record != null && !java.util.Objects.equals(qry.assetType(), record.assetType())) {
            return null;
        }
        return MemberGrowthDTOConverter.toAssetRecordDTO(record);
    }
}
