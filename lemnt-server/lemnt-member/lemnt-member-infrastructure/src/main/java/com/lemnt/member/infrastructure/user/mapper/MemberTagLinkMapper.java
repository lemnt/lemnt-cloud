package com.lemnt.member.infrastructure.user.mapper;

import com.lemnt.member.infrastructure.user.dataobject.MemberTagLinkDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** member_tag_link 表数据访问接口。 */
@Mapper
public interface MemberTagLinkMapper extends BaseMapper<MemberTagLinkDO> {}
