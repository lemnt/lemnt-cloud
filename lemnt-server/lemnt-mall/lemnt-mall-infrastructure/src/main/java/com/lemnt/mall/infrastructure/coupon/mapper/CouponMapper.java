package com.lemnt.mall.infrastructure.coupon.mapper;

import com.lemnt.mall.infrastructure.coupon.dataobject.CouponDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/** Coupon 表数据访问接口。 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponDO> {

    /**
     * 锁定优惠券数据行。
     */
    CouponDO lockCouponRow(@Param("memberId") Long memberId, @Param("id") Long id);

    /** 使用数据库时钟记录核销和恢复有效性。 */
    LocalDateTime now();

}
