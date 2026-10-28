package com.lemnt.mall.infrastructure.cart.mapper;

import com.lemnt.mall.infrastructure.cart.dataobject.CartDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** Cart 表数据访问接口。 */
@Mapper
public interface CartMapper extends BaseMapper<CartDO> {
}
