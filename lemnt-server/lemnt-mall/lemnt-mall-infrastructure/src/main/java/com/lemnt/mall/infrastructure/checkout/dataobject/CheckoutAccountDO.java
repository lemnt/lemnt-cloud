package com.lemnt.mall.infrastructure.checkout.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/** mall_checkout_account 表数据对象。 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_checkout_account")
public class CheckoutAccountDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会员编号。 */
    @TableId(type = IdType.INPUT)
    private Long memberId;
}
