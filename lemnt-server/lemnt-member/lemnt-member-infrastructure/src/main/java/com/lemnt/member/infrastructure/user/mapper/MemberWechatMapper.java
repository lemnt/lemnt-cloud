package com.lemnt.member.infrastructure.user.mapper;

import com.lemnt.member.infrastructure.user.dataobject.MemberWechatDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * member_wechat 表数据访问接口，简单操作复用BaseMapper。
 */
@Mapper
public interface MemberWechatMapper extends BaseMapper<MemberWechatDO> {
}
