package com.lemnt.mall.infrastructure.checkout.mapper;

import com.lemnt.mall.infrastructure.checkout.dataobject.SettlementAbandonDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** SettlementAbandon 表数据访问接口。 */
@Mapper
public interface SettlementAbandonMapper extends BaseMapper<SettlementAbandonDO> {
    /**
     * 已放弃。
     */
    default int abandoned(Long memberId, String id) {
        return this.selectCount(Wrappers.<SettlementAbandonDO>lambdaQuery()
                .eq(SettlementAbandonDO::getMemberId, memberId)
                .eq(SettlementAbandonDO::getSettlementId, id)).intValue();
    }

    /**
     * 放弃。
     */
    @Insert(
            "INSERT INTO mall_settlement_abandon(member_id,settlement_id)"
            + " VALUES(#{memberId},#{id}) ON DUPLICATE KEY UPDATE settlement_id=settlement_id")
    int abandon(@Param("memberId") Long memberId, @Param("id") String id);
}
