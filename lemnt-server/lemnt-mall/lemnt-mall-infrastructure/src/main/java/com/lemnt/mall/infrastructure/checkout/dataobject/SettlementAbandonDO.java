package com.lemnt.mall.infrastructure.checkout.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/** mall_settlement_abandon 表数据对象，联合主键通过条件构造器操作。 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_settlement_abandon")
public class SettlementAbandonDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会员编号。 */
    private Long memberId;

    /** 结算编号。 */
    private String settlementId;

    /** 创建时间。 */
    private LocalDateTime createTime;
}
