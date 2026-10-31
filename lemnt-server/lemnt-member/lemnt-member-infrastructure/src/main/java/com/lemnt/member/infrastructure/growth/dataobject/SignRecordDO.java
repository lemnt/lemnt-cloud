package com.lemnt.member.infrastructure.growth.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import com.lemnt.common.infrastructure.domain.BaseDO;
import java.time.LocalDate;

/**
 * 签到记录数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_sign_in_record")
public class SignRecordDO extends BaseDO {
    /** 后台查询关联的当前昵称，不写签到表。 */
    @com.baomidou.mybatisplus.annotation.TableField(exist=false)
    private String nickname;


    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 会员编号。 */
    private Long userId;
    /** 签到日期。 */
    private LocalDate signDate;
    /** 连续签到周期内的天数。 */
    @com.baomidou.mybatisplus.annotation.TableField("`day`")
    private Integer day;
    /** 奖励积分。 */
    private Long point;
    /** 经验值。 */
    private Long experience;
}
