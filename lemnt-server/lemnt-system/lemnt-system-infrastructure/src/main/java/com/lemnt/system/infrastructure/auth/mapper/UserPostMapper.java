package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.system.infrastructure.auth.dataobject.UserPostDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserRelation 数据层
 *
 * @author wangxx
 */
@Mapper
public interface UserPostMapper extends BaseMapper<UserPostDO> {
}
