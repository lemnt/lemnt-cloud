package com.lemnt.trade.domain.payment.gateway;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.trade.domain.payment.model.RechargePackage;
import com.lemnt.trade.domain.payment.model.RechargePackageFilter;

import java.util.List;

/** 充值套餐存储契约。 */
public interface RechargePackageGateway {
    PageResult<RechargePackage> page(RechargePackageFilter filter);

    RechargePackage find(Long id);

    List<RechargePackage> listEnabled();

    boolean existsName(String name, Long excludeId);

    RechargePackage insert(RechargePackage value);

    boolean update(RechargePackage value);

    boolean delete(Long id);
}
