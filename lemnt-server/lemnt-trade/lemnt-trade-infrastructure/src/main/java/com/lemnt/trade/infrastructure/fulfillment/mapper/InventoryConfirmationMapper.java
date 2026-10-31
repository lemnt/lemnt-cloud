package com.lemnt.trade.infrastructure.fulfillment.mapper;

import com.lemnt.trade.infrastructure.fulfillment.dataobject.InventoryConfirmationDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/** InventoryConfirmation 表数据访问接口。 */
@Mapper
public interface InventoryConfirmationMapper extends BaseMapper<InventoryConfirmationDO> {
    /**
     * 已确认。
     */
    default int confirmed(String orderNo) {
        return this.selectCount(Wrappers.<InventoryConfirmationDO>lambdaQuery()
                .eq(InventoryConfirmationDO::getOrderNo, orderNo)).intValue();
    }

    /**
     * 确认。
     */
    @Insert(
            "INSERT INTO trade_inventory_confirmation(order_no,confirmed_at)"
            + " VALUES(#{orderNo},#{time}) ON DUPLICATE KEY UPDATE order_no=order_no")
    int confirm(@Param("orderNo") String orderNo, @Param("time") LocalDateTime time);
}
