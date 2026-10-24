package com.lemnt.mall.infrastructure.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lemnt.common.infrastructure.domain.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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