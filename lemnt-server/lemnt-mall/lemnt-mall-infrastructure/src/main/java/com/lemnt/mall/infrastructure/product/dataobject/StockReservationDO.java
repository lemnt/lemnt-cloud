package com.lemnt.mall.infrastructure.product.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 库存预留数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mall_stock_reservation")
public class StockReservationDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String reservationNo;
    private Long productId;
    private Integer quantity;
    private String status;
}
