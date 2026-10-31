package com.lemnt.trade.infrastructure.payment.mapper;

import com.lemnt.trade.infrastructure.payment.dataobject.WalletRechargeDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/** trade_pay_wallet_recharge Mapper；到账/退款使用行锁查询。 */
@Mapper
public interface WalletRechargeMapper extends BaseMapper<WalletRechargeDO> {
    /** 按充值单号锁定，调用方必须持有事务。 */
    @Select("SELECT * FROM trade_pay_wallet_recharge WHERE recharge_no=#{rechargeNo} FOR UPDATE")
    WalletRechargeDO lockByNo(@Param("rechargeNo") String rechargeNo);

    /** 数据库当前时间。 */
    @Select("SELECT CURRENT_TIMESTAMP")
    LocalDateTime databaseNow();
}
