package com.lemnt.member.infrastructure.growth.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lemnt.common.infrastructure.domain.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 签到规则，天数唯一但不再承担主键。 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("member_sign_in_config")
public class SignConfigDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("`day`")
    private Integer day;
    private Long point;
    private Long experience;
    /** 1启用，0停用，沿本项目状态约定。 */
    private Integer status;
}
