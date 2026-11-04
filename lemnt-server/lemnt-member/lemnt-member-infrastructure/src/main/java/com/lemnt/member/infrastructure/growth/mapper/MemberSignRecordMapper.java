package com.lemnt.member.infrastructure.growth.mapper;

import com.lemnt.member.domain.growth.model.SignRecordPageFilter;
import com.lemnt.member.infrastructure.growth.dataobject.SignRecordDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * member_sign_in_record 表数据访问接口。
 */
@Mapper
public interface MemberSignRecordMapper extends BaseMapper<SignRecordDO> {

    /**
     * 查询签到记录。
     */
    List<SignRecordDO> signsPage(@Param("f") SignRecordPageFilter f);

    /**
     * 总数与关联分页使用相同条件。
     */
    long signsCount(@Param("f") SignRecordPageFilter f);
}
