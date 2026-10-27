package com.lemnt.mall.infrastructure.checkout.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/** mall_settlement_order 表数据对象。 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_settlement_order")
public class SettlementOrderDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 结算编号。 */
    @TableId(type = IdType.INPUT)
    private String settlementId;

    /** 会员编号。 */
    private Long memberId;

    /** 订单号。 */
    private String orderNo;
}
