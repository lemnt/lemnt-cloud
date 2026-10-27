package com.lemnt.member.infrastructure.growth.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 签到配置数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_sign_config")
public class SignConfigDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 连续签到周期内的天数。 */
    @TableId(type = IdType.INPUT)
    private Integer signinDay;
    /** 奖励积分。 */
    private Long point;
    /** 经验值。 */
    private Long experience;
    /** 状态：0停用，1启用。 */
    private Integer status;
}
