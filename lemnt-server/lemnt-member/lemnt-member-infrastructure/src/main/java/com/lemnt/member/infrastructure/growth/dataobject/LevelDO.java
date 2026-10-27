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
 * 等级数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_level")
public class LevelDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 名称。 */
    private String name;
    /** 等级序号。 */
    private Integer level;
    /** 经验值。 */
    private Long experience;
    /** 折扣百分比，100表示原价。 */
    private Integer discountPercent;
    /** 等级图标地址。 */
    private String icon;
    /** 等级背景地址。 */
    private String backgroundUrl;
    /** 状态：0停用，1启用。 */
    private Integer status;
}
