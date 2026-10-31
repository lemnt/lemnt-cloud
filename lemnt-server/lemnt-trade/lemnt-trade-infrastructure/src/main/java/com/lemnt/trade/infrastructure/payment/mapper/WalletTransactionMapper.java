package com.lemnt.trade.infrastructure.payment.mapper;

import com.lemnt.trade.infrastructure.payment.dataobject.WalletTransactionDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** trade_member_wallet_transaction Mapper，追加与查询复用 MyBatis Plus。 */
@Mapper
public interface WalletTransactionMapper extends BaseMapper<WalletTransactionDO> {
}
