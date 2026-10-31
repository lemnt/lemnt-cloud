package com.lemnt.member.infrastructure.growth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.member.infrastructure.growth.dataobject.ConfigDO;
import org.apache.ibatis.annotations.Mapper;

/** 积分配置简单CRUD复用MyBatis-Plus。 */
@Mapper
public interface MemberConfigMapper extends BaseMapper<ConfigDO> {}
