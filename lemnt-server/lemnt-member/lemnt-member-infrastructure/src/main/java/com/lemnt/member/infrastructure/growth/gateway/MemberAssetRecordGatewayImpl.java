package com.lemnt.member.infrastructure.growth.gateway;

import com.lemnt.member.domain.growth.gateway.MemberAssetRecordGateway;
import com.lemnt.member.domain.growth.model.AssetRecord;
import com.lemnt.member.domain.growth.model.AssetRecordPageFilter;
import com.lemnt.member.infrastructure.growth.converter.AssetRecordConverter;
import com.lemnt.member.infrastructure.growth.mapper.MemberAssetRecordMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** member_asset_record 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class MemberAssetRecordGatewayImpl implements MemberAssetRecordGateway {

    private final MemberAssetRecordMapper memberAssetRecordMapper;

    /**
     * 资产请求。
     */
    @Override
    public AssetRecord assetRequest(Long memberId, String type, String bizType, String bizNo) {
        return AssetRecordConverter.toEntity(memberAssetRecordMapper.assetRequest(memberId, type, bizType, bizNo));
    }

    /**
     * 资产保存。
     */
    @Override
    public void assetSave(AssetRecord value) {
        memberAssetRecordMapper.assetSave(AssetRecordConverter.toDO(value));
    }

    /**
     * 查询会员资产记录。
     */
    @Override
    public List<AssetRecord> assets(Long memberId, String type, int offset, int limit) {
        return memberAssetRecordMapper.assets(memberId, type, offset, limit).stream()
                .map(AssetRecordConverter::toEntity)
                .toList();
    }

    /**
     * 资产数量。
     */
    @Override
    public long assetCount(Long memberId, String type) {
        return memberAssetRecordMapper.assetCount(memberId, type);
    }

    /**
     * 查询会员资产记录。
     */
    @Override
    public List<AssetRecord> assets(AssetRecordPageFilter f) {
        return memberAssetRecordMapper.assetsPage(f).stream().map(AssetRecordConverter::toEntity).toList();
    }

    /**
     * 查询会员资产记录数量。
     */
    @Override
    public long assetsCount(AssetRecordPageFilter f) {
        return memberAssetRecordMapper.assetsCount(f);
    }

    /**
     * 查询会员资产记录详情。
     */
    @Override
    public AssetRecord assetsDetail(Long id) {
        return AssetRecordConverter.toEntity(memberAssetRecordMapper.assetsDetail(id));
    }
    @Override
    public long tradeReturns(Long memberId, String assetType, String bizType, String orderNo) {
        return memberAssetRecordMapper.selectList(com.baomidou.mybatisplus.core.toolkit.Wrappers
                .<com.lemnt.member.infrastructure.growth.dataobject.AssetRecordDO>lambdaQuery()
                .eq(com.lemnt.member.infrastructure.growth.dataobject.AssetRecordDO::getMemberId, memberId)
                .eq(com.lemnt.member.infrastructure.growth.dataobject.AssetRecordDO::getAssetType, assetType)
                .eq(com.lemnt.member.infrastructure.growth.dataobject.AssetRecordDO::getBizType, bizType)
                .likeRight(com.lemnt.member.infrastructure.growth.dataobject.AssetRecordDO::getBizNo, orderNo + ":"))
                .stream().mapToLong(row -> row.getDelta()).sum();
    }
}
