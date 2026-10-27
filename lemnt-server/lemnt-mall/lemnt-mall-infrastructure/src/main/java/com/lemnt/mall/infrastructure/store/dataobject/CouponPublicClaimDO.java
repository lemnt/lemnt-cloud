package com.lemnt.mall.infrastructure.store.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/** mall_coupon_public_claim 表数据对象。 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_coupon_public_claim")
public class CouponPublicClaimDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 优惠券模板编号。 */
    @TableId(type = IdType.INPUT)
    private Long templateId;

    /** 是否启用。 */
    private Boolean enabled;
}
