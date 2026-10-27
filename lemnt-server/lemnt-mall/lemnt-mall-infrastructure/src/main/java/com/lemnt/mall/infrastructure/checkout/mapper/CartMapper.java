package com.lemnt.mall.infrastructure.checkout.mapper;

import com.lemnt.mall.infrastructure.checkout.dataobject.CartDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** Cart 表数据访问接口。 */
@Mapper
public interface CartMapper extends BaseMapper<CartDO> {
    /**
     * 购物车。
     */
    default List<CartDO> cart(Long memberId) {
        return this.selectList(Wrappers.<CartDO>lambdaQuery()
                .eq(CartDO::getMemberId, memberId)
                .orderByAsc(CartDO::getId)
                .last("LIMIT 201"));
    }

    /**
     * 购物车明细。
     */
    default CartDO cartItem(Long memberId, Long productId) {
        return this.selectOne(Wrappers.<CartDO>lambdaQuery()
                .eq(CartDO::getMemberId, memberId)
                .eq(CartDO::getProductId, productId));
    }

    /**
     * 插入购物车。
     */
    @Insert(
            "INSERT INTO mall_cart(member_id,product_id,quantity,selected,version)"
            + " VALUES(#{memberId},#{productId},#{quantity},#{selected},0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCart(CartDO row);

    /**
     * 更新购物车。
     */
    @Update(
            "UPDATE mall_cart SET quantity=#{quantity},selected=#{selected},version=version+1"
            + " WHERE id=#{id} AND member_id=#{memberId}")
    int updateCart(CartDO row);

    /**
     * 删除购物车。
     */
    default int deleteCart(Long memberId, Long productId) {
        return this.delete(Wrappers.<CartDO>lambdaQuery()
                .eq(CartDO::getMemberId, memberId)
                .eq(CartDO::getProductId, productId));
    }

    /**
     * 消费购物车。
     */
    default int consumeCart(
            Long memberId, Long id, long version) {
        return this.delete(Wrappers.<CartDO>lambdaQuery()
                .eq(CartDO::getMemberId, memberId)
                .eq(CartDO::getId, id)
                .eq(CartDO::getVersion, version));
    }
}
