package com.lemnt.member.infrastructure.address.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import com.lemnt.common.infrastructure.domain.BaseDO;

/**
 * 地址数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("member_address")
public class AddressDO extends BaseDO {

    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 会员编号。 */
    private Long userId;
    /** 名称。 */
    private String name;
    /** 手机号。 */
    private String mobile;
    /** 行政区划编号。 */
    private Long areaId;
    /** 详细地址。 */
    private String detailAddress;
    /** 是否为默认地址。 */
    private Boolean defaultStatus;
}
