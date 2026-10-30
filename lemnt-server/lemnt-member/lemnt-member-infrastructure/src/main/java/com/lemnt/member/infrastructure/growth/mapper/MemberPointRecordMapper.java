package com.lemnt.member.infrastructure.growth.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.member.infrastructure.growth.dataobject.PointRecordDO;
import com.lemnt.member.domain.growth.model.AssetRecordPageFilter;
import org.apache.ibatis.annotations.*;
import java.util.List;
/** 积分表Mapper，简单CRUD直接使用BaseMapper，关联查询用XML。 */
@Mapper
public interface MemberPointRecordMapper extends BaseMapper<PointRecordDO> {
    List<PointRecordDO> page(@Param("f") AssetRecordPageFilter filter);
    long count(@Param("f") AssetRecordPageFilter filter);
}
