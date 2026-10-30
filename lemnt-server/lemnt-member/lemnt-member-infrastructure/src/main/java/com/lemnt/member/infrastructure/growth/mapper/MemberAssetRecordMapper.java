package com.lemnt.member.infrastructure.growth.mapper;

import com.lemnt.member.domain.growth.model.AssetRecordPageFilter;
import com.lemnt.member.infrastructure.growth.dataobject.AssetRecordDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** member_asset_record 表数据访问接口。 */
@Mapper
public interface MemberAssetRecordMapper extends BaseMapper<AssetRecordDO> {

    /**
     * 资产请求。
     */
    default AssetRecordDO assetRequest(
            Long memberId,
            String assetType,
            String bizType,
            String bizNo) {
        return this.selectOne(Wrappers.<AssetRecordDO>lambdaQuery()
                .eq(AssetRecordDO::getMemberId, memberId)
                .eq(AssetRecordDO::getAssetType, assetType)
                .eq(AssetRecordDO::getBizType, bizType)
                .eq(AssetRecordDO::getBizNo, bizNo));
    }

    /**
     * 资产保存。
     */
    default int assetSave(AssetRecordDO value) {
        return this.insert(value);
    }

    /**
     * 查询会员资产记录。
     */
    default List<AssetRecordDO> assets(
            Long memberId,
            String type,
            int offset,
            int limit) {
        return this.selectPage(new Page<>((long) offset / limit + 1, limit, false),
                Wrappers.<AssetRecordDO>lambdaQuery()
                .eq(AssetRecordDO::getMemberId, memberId)
                .eq(AssetRecordDO::getAssetType, type)
                .orderByDesc(AssetRecordDO::getId)).getRecords();
    }

    /**
     * 资产数量。
     */
    default long assetCount(Long memberId, String type) {
        return this.selectCount(Wrappers.<AssetRecordDO>lambdaQuery()
                .eq(AssetRecordDO::getMemberId, memberId)
                .eq(AssetRecordDO::getAssetType, type));
    }

    /**
     * 查询会员资产记录。
     */
    List<AssetRecordDO> assetsPage(@org.apache.ibatis.annotations.Param("f") AssetRecordPageFilter f);
    /** 使用相同关联条件统计。 */
    long assetsCount(@org.apache.ibatis.annotations.Param("f") AssetRecordPageFilter f);

    /**
     * 查询会员资产记录详情。
     */
    default AssetRecordDO assetsDetail(Long id) {
        return this.selectOne(Wrappers.<AssetRecordDO>lambdaQuery()
                .eq(AssetRecordDO::getId, id));
    }
}
