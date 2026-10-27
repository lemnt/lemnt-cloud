package com.lemnt.member.application.growth.executor;

import com.lemnt.member.application.growth.cmd.AssetAdjustCmd;
import com.lemnt.member.application.growth.converter.MemberGrowthDTOConverter;
import com.lemnt.member.application.growth.dto.AssetRecordDTO;
import com.lemnt.member.application.growth.service.MemberGrowthService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 资产调整命令执行器。 */
@Component
@RequiredArgsConstructor
public class MemberAssetAdjustCmdExe {

    private final MemberGrowthService memberGrowthService;

    /**
     * 执行资产调整命令。
     */
    public AssetRecordDTO execute(AssetAdjustCmd cmd) {
        return MemberGrowthDTOConverter.toAssetRecordDTO(
                memberGrowthService.adjustAsset(
                        cmd.memberId(), cmd.assetType(), cmd.delta(), cmd.requestNo(), cmd.reason()));
    }
}
