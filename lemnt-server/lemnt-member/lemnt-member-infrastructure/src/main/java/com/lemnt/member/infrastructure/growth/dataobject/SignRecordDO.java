package com.lemnt.member.infrastructure.growth.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * 签到记录数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_sign_record")
public class SignRecordDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 会员编号。 */
    private Long memberId;
    /** 签到日期。 */
    private LocalDate signDate;
    /** 连续签到周期内的天数。 */
    private Integer signinDay;
    /** 奖励积分。 */
    private Long point;
    /** 经验值。 */
    private Long experience;
}
