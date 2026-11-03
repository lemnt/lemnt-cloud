package com.lemnt.trade.infrastructure.fulfillment.mapper;

import com.lemnt.trade.infrastructure.fulfillment.dataobject.InventoryConfirmationDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/** InventoryConfirmation 表数据访问接口。 */
@Mapper
public interface InventoryConfirmationMapper extends BaseMapper<InventoryConfirmationDO> {

    /**
     * 确认。
     */
    int confirm(@Param("orderNo") String orderNo, @Param("time") LocalDateTime time);
}
