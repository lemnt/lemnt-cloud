package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.system.infrastructure.auth.dataobject.RoleMenuDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * RoleRelation 数据层
 *
 * @author wangxx
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenuDO> {}
