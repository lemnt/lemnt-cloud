package com.lemnt.member.infrastructure.growth.mapper;

import com.lemnt.member.infrastructure.growth.dataobject.SignConfigDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 简单CRUD复用MyBatis-Plus，不重复封装或使用注解SQL。 */
@Mapper
public interface MemberSignConfigMapper extends BaseMapper<SignConfigDO> {}
