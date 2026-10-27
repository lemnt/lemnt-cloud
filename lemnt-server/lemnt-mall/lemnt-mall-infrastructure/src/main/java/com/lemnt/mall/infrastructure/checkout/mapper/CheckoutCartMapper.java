package com.lemnt.mall.infrastructure.checkout.mapper;

import com.lemnt.mall.infrastructure.checkout.dataobject.CheckoutCartDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** CheckoutCart 表数据访问接口。 */
@Mapper
public interface CheckoutCartMapper extends BaseMapper<CheckoutCartDO> {
    /** 读取引用指定购物车的提单记录，由网关查询交易服务状态。 */
    default List<CheckoutCartDO> ordersForCart(Long cartId) {
        return this.selectList(Wrappers.<CheckoutCartDO>lambdaQuery()
                .eq(CheckoutCartDO::getCartId, cartId));
    }

    /**
     * 插入引用。
     */
    default int insertReference(CheckoutCartDO row) {
        return this.insert(row);
    }

    /**
     * 引用。
     */
    default List<CheckoutCartDO> references(String orderNo) {
        return this.selectList(Wrappers.<CheckoutCartDO>lambdaQuery()
                .eq(CheckoutCartDO::getOrderNo, orderNo)
                .orderByAsc(CheckoutCartDO::getCartId));
    }
}
