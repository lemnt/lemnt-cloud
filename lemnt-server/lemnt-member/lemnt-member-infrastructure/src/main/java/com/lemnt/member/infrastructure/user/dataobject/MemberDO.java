package com.lemnt.member.infrastructure.user.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;

/**
 * 会员数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("member_user")
public class MemberDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    /** 主键编号。 */
    private Long id;
    /** 会员业务编号。 */
    private String memberNo;
    /** 昵称。 */
    private String nickname;
    /** 手机号。 */
    private String mobile;
    /** 使用项目默认算法保存的密码哈希，不在诊断信息中输出。 */
    @ToString.Exclude
    private String password;
    /** 邮箱。 */
    private String email;
    /** 头像地址。 */
    private String avatar;
    /** 状态：0停用，1启用。 */
    private Integer status;
    /** 积分余额。 */
    private Long points;
    /** 经验值。 */
    private Long experience;
    /** 等级编号。 */
    private Long levelId;
    /** 分组编号。 */
    private Long groupId;
    /** 性别：0未知，1男，2女。 */
    private Integer sex;
    /** 出生日期。 */
    private LocalDate birthday;
}
