package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.AssetRecord;
import com.lemnt.member.infrastructure.account.dataobject.AssetRecordDO;

public final class AssetRecordConverter {
    private AssetRecordConverter() {}

    public static AssetRecord domain(AssetRecordDO row) {
        return row == null
                ? null
                : new AssetRecord(
                        row.getId(),
                        row.getMemberId(),
                        row.getAssetType(),
                        row.getDelta(),
                        row.getBalance(),
                        row.getBizType(),
                        row.getBizNo(),
                        row.getReason(),
                        row.getCreateTime());
    }

    public static AssetRecordDO row(AssetRecord value) {
        var row = new AssetRecordDO();
        row.setId(value.id());
        row.setMemberId(value.memberId());
        row.setAssetType(value.assetType());
        row.setDelta(value.delta());
        row.setBalance(value.balance());
        row.setBizType(value.bizType());
        row.setBizNo(value.bizNo());
        row.setReason(value.reason());
        row.setCreateTime(value.createTime());
        return row;
    }
}
