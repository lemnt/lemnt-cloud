package com.lemnt.member.domain.growth.gateway;

import com.lemnt.member.domain.growth.model.OrderBenefitsSnapshot;

/** 读取订单所属服务保存的原金额及积分，不反查当前规则。 */
public interface OrderBenefitsSnapshotGateway {
    OrderBenefitsSnapshot find(String orderNo);
}
