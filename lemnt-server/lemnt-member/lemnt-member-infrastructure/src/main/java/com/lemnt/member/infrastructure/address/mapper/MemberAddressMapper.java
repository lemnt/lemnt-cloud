package com.lemnt.member.infrastructure.address.mapper;

import com.lemnt.member.infrastructure.address.dataobject.AddressDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** 会员收货地址领域网关。 */
@Mapper
public interface MemberAddressMapper extends BaseMapper<AddressDO> {

    /**
     * 查询收货地址。
     */
    default List<AddressDO> addresses(Long memberId) {
        return this.selectList(Wrappers.<AddressDO>lambdaQuery()
                .eq(AddressDO::getMemberId, memberId)
                .orderByDesc(AddressDO::getDefaultStatus)
                .orderByDesc(AddressDO::getId));
    }

    /**
     * 地址。
     */
    default AddressDO address(Long memberId, Long id) {
        return this.selectOne(Wrappers.<AddressDO>lambdaQuery()
                .eq(AddressDO::getMemberId, memberId)
                .eq(AddressDO::getId, id));
    }

    /**
     * 地址插入。
     */
    default int addressInsert(AddressDO value) {
        return this.insert(value);
    }

    /**
     * 地址更新。
     */
    default int addressUpdate(AddressDO value) {
        return this.update(null, Wrappers.<AddressDO>lambdaUpdate()
                .set(AddressDO::getName, value.getName())
                .set(AddressDO::getMobile, value.getMobile())
                .set(AddressDO::getAreaId, value.getAreaId())
                .set(AddressDO::getDetailAddress, value.getDetailAddress())
                .set(AddressDO::getDefaultStatus, value.getDefaultStatus())
                .eq(AddressDO::getId, value.getId())
                .eq(AddressDO::getMemberId, value.getMemberId()));
    }

    /**
     * 地址删除。
     */
    default int addressDelete(Long memberId, Long id) {
        return this.delete(Wrappers.<AddressDO>lambdaQuery()
                .eq(AddressDO::getMemberId, memberId)
                .eq(AddressDO::getId, id));
    }

    /**
     * 清除默认地址。
     */
    default int clearDefaultAddress(Long memberId) {
        return this.update(null, Wrappers.<AddressDO>lambdaUpdate()
                .set(AddressDO::getDefaultStatus, false)
                .eq(AddressDO::getMemberId, memberId));
    }
}
