package com.lemnt.trade.infrastructure.payment.mapper;

import com.lemnt.trade.infrastructure.payment.dataobject.PayNotifyLogDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** trade_pay_notify_log Mapper，追加与查询复用 MyBatis Plus。 */
@Mapper
public interface PayNotifyLogMapper extends BaseMapper<PayNotifyLogDO> {
}
