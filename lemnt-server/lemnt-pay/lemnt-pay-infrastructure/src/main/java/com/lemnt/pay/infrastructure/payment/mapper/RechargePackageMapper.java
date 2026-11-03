package com.lemnt.pay.infrastructure.payment.mapper;

import com.lemnt.pay.infrastructure.payment.dataobject.RechargePackageDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** pay_recharge_package Mapper，CRUD 复用 MyBatis Plus。 */
@Mapper
public interface RechargePackageMapper extends BaseMapper<RechargePackageDO> {}
