package com.lemnt.trade.infrastructure.fulfillment.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** trade_inventory_confirmation 表数据对象。 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("trade_inventory_confirmation")
public class InventoryConfirmationDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 订单号。 */
    @TableId(type = IdType.INPUT)
    private String orderNo;

    /** 库存确认时间。 */
    private LocalDateTime confirmedAt;
}
