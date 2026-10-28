package com.lemnt.member.domain.growth.gateway;

import com.lemnt.member.domain.growth.model.AssetRecord;
import com.lemnt.member.domain.growth.model.AssetRecordPageFilter;

import java.util.List;

/** member_asset_record 表领域存储网关。 */
public interface MemberAssetRecordGateway {

    /**
     * 资产请求。
     */
    AssetRecord assetRequest(Long memberId, String assetType, String bizType, String bizNo);

    /**
     * 资产保存。
     */
    void assetSave(AssetRecord record);

    /**
     * 查询会员资产记录。
     */
    List<AssetRecord> assets(Long memberId, String type, int offset, int limit);

    /**
     * 资产数量。
     */
    long assetCount(Long memberId, String type);

    /**
     * 查询会员资产记录。
     */
    List<AssetRecord> assets(AssetRecordPageFilter filter);

    /**
     * 查询会员资产记录数量。
     */
    long assetsCount(AssetRecordPageFilter filter);

    /**
     * 查询会员资产记录详情。
     */
    AssetRecord assetsDetail(Long id);
    /** 同一订单已发生的售后资产回退量，调用方持有会员行锁。 */
    long tradeReturns(Long memberId, String assetType, String bizType, String orderNo);
}
