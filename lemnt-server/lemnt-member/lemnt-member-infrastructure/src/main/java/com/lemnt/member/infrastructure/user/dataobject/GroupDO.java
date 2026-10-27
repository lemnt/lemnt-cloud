package com.lemnt.member.infrastructure.user.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分组数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_group")
public class GroupDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 名称。 */
    private String name;
    /** 备注。 */
    private String remark;
    /** 状态：0停用，1启用。 */
    private Integer status;
}
