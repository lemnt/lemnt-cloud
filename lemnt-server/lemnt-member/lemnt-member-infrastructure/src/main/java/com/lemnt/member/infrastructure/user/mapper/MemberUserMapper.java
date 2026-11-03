package com.lemnt.member.infrastructure.user.mapper;

import com.lemnt.member.domain.user.model.MemberPageFilter;
import com.lemnt.member.infrastructure.user.dataobject.MemberDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/** member_user 表数据访问接口。 */
@Mapper
public interface MemberUserMapper extends BaseMapper<MemberDO> {

    /** 真实数据库时间，不由应用时钟推算。 */
    LocalDateTime now();

    /** 锁定会员行，原有写操作序列化不变。 */
    MemberDO lock(@Param("id") Long id);

    /** 参数化标签EXISTS及资料组合分页。 */
    List<MemberDO> profiles(@Param("f") MemberPageFilter filter);

    /** 与资料分页使用同一条件，不重复计算标签命中。 */
    long profileCount(@Param("f") MemberPageFilter filter);
}
