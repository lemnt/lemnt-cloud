package com.lemnt.member.infrastructure.growth.converter;

import com.lemnt.member.domain.growth.model.AssetRecord;
import com.lemnt.member.infrastructure.growth.dataobject.AssetRecordDO;

/**
 * 资产记录转换器。
 */
public final class AssetRecordConverter {
    private AssetRecordConverter() {}

    /**
     * 转换为领域对象。
     */
    public static AssetRecord toEntity(AssetRecordDO row) {
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

    /**
     * 转换为持久化数据对象。
     */
    public static AssetRecordDO toDO(AssetRecord value) {
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
