package com.lemnt.pay.infrastructure.payment.mapper;

import com.lemnt.pay.infrastructure.payment.dataobject.WalletRechargeDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/** pay_wallet_recharge Mapper；到账/退款使用行锁查询。 */
@Mapper
public interface WalletRechargeMapper extends BaseMapper<WalletRechargeDO> {
    /** 按充值单号锁定，调用方必须持有事务。 */
    WalletRechargeDO lockByNo(@Param("rechargeNo") String rechargeNo);

    /** 数据库当前时间。 */
    LocalDateTime databaseNow();
}
