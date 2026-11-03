package com.lemnt.pay.infrastructure.payment.mapper;

import com.lemnt.pay.infrastructure.payment.dataobject.PayNotifyLogDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** pay_notify_log Mapper，追加与查询复用 MyBatis Plus。 */
@Mapper
public interface PayNotifyLogMapper extends BaseMapper<PayNotifyLogDO> {}
