package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.system.infrastructure.auth.dataobject.RoleDeptDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * RoleRelation 数据层
 *
 * @author wangxx
 */
@Mapper
public interface RoleDeptMapper extends BaseMapper<RoleDeptDO> {}
