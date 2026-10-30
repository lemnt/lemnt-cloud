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
 * 等级记录数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_level_record")
public class LevelRecordDO extends BaseDO {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 会员编号。 */
    private Long userId;
    /** 等级编号。 */
    private Long levelId;
    /** 源记录经验，自动为门槛、手工为差值；历史未知为空。 */
    private Long experience;
    /** 变更后会员经验，区别于等级门槛。 */
    private Long userExperience;
    private Integer level;
    private Integer discountPercent;
    private String description;
    /** 变更原因。 */
    private String remark;

}
