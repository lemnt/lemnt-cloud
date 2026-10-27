package com.lemnt.mall.infrastructure.store.mapper;

import com.lemnt.mall.infrastructure.store.dataobject.CartAdditionDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

/** CartAddRequest 表数据访问接口。 */
@Mapper
public interface CartAddRequestMapper extends BaseMapper<CartAdditionDO> {
    /**
     * 增量加购。
     */
    default CartAdditionDO addition(Long memberId, String requestNo) {
        return this.selectOne(Wrappers.<CartAdditionDO>lambdaQuery()
                .eq(CartAdditionDO::getMemberId, memberId)
                .eq(CartAdditionDO::getRequestNo, requestNo));
    }

    /**
     * 增量加购保存。
     */
    default void additionSave(CartAdditionDO value) {
        this.insert(value);
    }
}
