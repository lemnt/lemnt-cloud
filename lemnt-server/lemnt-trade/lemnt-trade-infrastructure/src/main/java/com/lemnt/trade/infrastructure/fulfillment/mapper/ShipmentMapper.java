package com.lemnt.trade.infrastructure.fulfillment.mapper;

import com.lemnt.trade.infrastructure.fulfillment.dataobject.ShipmentDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/** Shipment 表数据访问接口。 */
@Mapper
public interface ShipmentMapper extends BaseMapper<ShipmentDO> {
    /**
     * 发货。
     */
    default ShipmentDO shipment(String orderNo) {
        return this.selectOne(Wrappers.<ShipmentDO>lambdaQuery()
                .eq(ShipmentDO::getOrderNo, orderNo));
    }

    /**
     * 插入发货。
     */
    default int insertShipment(ShipmentDO row) {
        return this.insert(row);
    }

    /**
     * 接收。
     */
    @Update(
            "UPDATE trade_shipment SET received_at=#{time} WHERE order_no=#{orderNo} AND"
            + " received_at IS NULL")
    int receive(@Param("orderNo") String orderNo, @Param("time") LocalDateTime time);
}
