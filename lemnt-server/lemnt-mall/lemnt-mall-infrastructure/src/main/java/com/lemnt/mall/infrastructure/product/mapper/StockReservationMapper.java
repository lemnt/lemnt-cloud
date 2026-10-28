package com.lemnt.mall.infrastructure.product.mapper;

import com.lemnt.mall.infrastructure.product.dataobject.StockReservationDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** StockReservation 表数据访问接口。 */
@Mapper
public interface StockReservationMapper extends BaseMapper<StockReservationDO> {
}
