package com.lemnt.member.infrastructure.user.mapper;

import com.lemnt.member.infrastructure.user.dataobject.GroupDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Group表简单CRUD复用BaseMapper，跨表删除在XML。 */
@Mapper
public interface MemberGroupMapper extends BaseMapper<GroupDO> {
    /** 原子删除没有会员使用的记录。 */
    int groupDelete(@Param("id") Long id);
}
