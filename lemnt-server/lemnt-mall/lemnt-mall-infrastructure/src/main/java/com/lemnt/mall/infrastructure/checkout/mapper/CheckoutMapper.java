package com.lemnt.mall.infrastructure.checkout.mapper;

import com.lemnt.mall.infrastructure.checkout.dataobject.CheckoutDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

/** Checkout 表数据访问接口。 */
@Mapper
public interface CheckoutMapper extends BaseMapper<CheckoutDO> {
    /**
     * 请求。
     */
    default CheckoutDO request(Long memberId, String requestNo) {
        return this.selectOne(Wrappers.<CheckoutDO>lambdaQuery()
                .eq(CheckoutDO::getMemberId, memberId)
                .eq(CheckoutDO::getRequestNo, requestNo));
    }

    /**
     * 回执。
     */
    default CheckoutDO receipt(String orderNo) {
        return this.selectOne(Wrappers.<CheckoutDO>lambdaQuery()
                .eq(CheckoutDO::getOrderNo, orderNo));
    }

    /**
     * 插入结算。
     */
    default int insertCheckout(CheckoutDO row) {
        return this.insert(row);
    }
}
