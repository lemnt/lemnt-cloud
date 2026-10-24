package com.lemnt.member.infrastructure.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.member.infrastructure.dataobject.MemberDO;
import org.apache.ibatis.annotations.Mapper;
@Mapper public interface MemberMapper extends BaseMapper<MemberDO> { }