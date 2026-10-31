package com.lemnt.member.infrastructure.address.mapper;

import com.lemnt.member.infrastructure.address.dataobject.AddressDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** member_address 表数据访问接口，简单操作复用BaseMapper。 */
@Mapper
public interface MemberAddressMapper extends BaseMapper<AddressDO> {}
