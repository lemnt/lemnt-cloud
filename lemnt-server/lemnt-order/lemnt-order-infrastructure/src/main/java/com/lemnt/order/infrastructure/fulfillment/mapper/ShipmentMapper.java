package com.lemnt.order.infrastructure.fulfillment.mapper;

import com.lemnt.order.infrastructure.fulfillment.dataobject.ShipmentDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * Shipment 表数据访问接口。
 */
@Mapper
public interface ShipmentMapper extends BaseMapper<ShipmentDO> {

    /**
     * 接收。
     */
    int receive(@Param("orderNo") String orderNo, @Param("time") LocalDateTime time);
}
