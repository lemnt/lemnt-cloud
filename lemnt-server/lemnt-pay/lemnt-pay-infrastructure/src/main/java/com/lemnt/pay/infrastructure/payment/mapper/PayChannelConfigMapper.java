package com.lemnt.pay.infrastructure.payment.mapper;

import com.lemnt.pay.infrastructure.payment.dataobject.PayChannelConfigDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** pay_channel Mapper，CRUD 复用 MyBatis Plus。 */
@Mapper
public interface PayChannelConfigMapper extends BaseMapper<PayChannelConfigDO> {}
