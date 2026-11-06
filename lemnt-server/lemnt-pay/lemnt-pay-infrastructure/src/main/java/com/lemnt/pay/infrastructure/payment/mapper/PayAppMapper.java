package com.lemnt.pay.infrastructure.payment.mapper;

import com.lemnt.pay.infrastructure.payment.dataobject.PayAppDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * pay_app Mapper，CRUD 复用 MyBatis Plus。
 */
@Mapper
public interface PayAppMapper extends BaseMapper<PayAppDO> {
}
