package com.lemnt.member.infrastructure.user.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 会员微信小程序标识数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("member_wechat")
public class MemberWechatDO extends BaseDO {

    /**
     * 会员编号。
     */
    @TableId(value = "member_id", type = IdType.INPUT)
    private Long memberId;

    /**
     * 微信小程序 openid。
     */
    private String openId;
}
