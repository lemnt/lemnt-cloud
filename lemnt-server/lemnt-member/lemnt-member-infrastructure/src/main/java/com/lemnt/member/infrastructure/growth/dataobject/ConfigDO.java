package com.lemnt.member.infrastructure.growth.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import com.lemnt.common.infrastructure.domain.BaseDO;

/**
 * 配置数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_config")
public class ConfigDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 表主键。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 是否启用积分抵扣。 */
    private Boolean pointTradeDeductEnable;
    /** 积分抵扣单位金额。 */
    private Integer pointTradeDeductUnitPrice;
    /** 每单最多抵扣积分数，0不限。 */
    private Integer pointTradeDeductMaxPrice;
    /** 交易赠送积分配置。 */
    private Integer pointTradeGivePoint;
}
