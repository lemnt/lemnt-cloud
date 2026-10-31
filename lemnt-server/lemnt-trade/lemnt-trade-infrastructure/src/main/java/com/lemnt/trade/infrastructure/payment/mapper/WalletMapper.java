package com.lemnt.trade.infrastructure.payment.mapper;

import com.lemnt.trade.infrastructure.payment.dataobject.WalletDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** trade_member_wallet Mapper；资金变更使用行锁查询。 */
@Mapper
public interface WalletMapper extends BaseMapper<WalletDO> {
    /** 按会员锁定钱包，调用方必须持有事务。 */
    @Select("SELECT * FROM trade_member_wallet WHERE member_id=#{memberId} FOR UPDATE")
    WalletDO lockByMemberId(@Param("memberId") Long memberId);

    /** 按编号锁定钱包，供转账按编号升序锁定双方。 */
    @Select("SELECT * FROM trade_member_wallet WHERE id=#{id} FOR UPDATE")
    WalletDO lockById(@Param("id") Long id);
}
