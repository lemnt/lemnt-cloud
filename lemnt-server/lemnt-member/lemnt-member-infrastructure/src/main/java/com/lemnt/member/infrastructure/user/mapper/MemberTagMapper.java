package com.lemnt.member.infrastructure.user.mapper;

import com.lemnt.member.infrastructure.user.dataobject.TagDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Tag表简单CRUD复用BaseMapper，跨表删除在XML。 */
@Mapper
public interface MemberTagMapper extends BaseMapper<TagDO> {
    /** 原子删除没有会员使用的记录。 */
    int tagDelete(@Param("id") Long id);
}
