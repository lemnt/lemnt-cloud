package com.lemnt.trade.infrastructure.payment.mapper;

import com.lemnt.trade.infrastructure.payment.dataobject.WalletTransferDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** trade_member_wallet_transfer Mapper，CRUD 复用 MyBatis Plus。 */
@Mapper
public interface WalletTransferMapper extends BaseMapper<WalletTransferDO> {
}
