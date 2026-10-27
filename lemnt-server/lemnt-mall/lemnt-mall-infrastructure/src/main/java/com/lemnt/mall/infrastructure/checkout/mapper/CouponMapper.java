package com.lemnt.mall.infrastructure.checkout.mapper;

import com.lemnt.mall.infrastructure.checkout.dataobject.CouponDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** Coupon 表数据访问接口。 */
@Mapper
public interface CouponMapper extends BaseMapper<CouponDO> {
    /**
     * 查询优惠券。
     */
    default List<CouponDO> coupons(Long memberId) {
        return this.selectList(Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getMemberId, memberId)
                .orderByDesc(CouponDO::getId)
                .last("LIMIT 1001"));
    }

    /**
     * 优惠券。
     */
    default CouponDO coupon(Long memberId, Long id) {
        return this.selectOne(Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getMemberId, memberId)
                .eq(CouponDO::getId, id));
    }

    /**
     * 锁定优惠券数据行。
     */
    @Select("SELECT * FROM mall_coupon WHERE member_id=#{memberId} AND id=#{id} FOR UPDATE")
    CouponDO lockCouponRow(@Param("memberId") Long memberId, @Param("id") Long id);

    /**
     * 已签发。
     */
    default CouponDO checkoutIssued(Long memberId, Long templateId) {
        return this.selectOne(Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getMemberId, memberId)
                .eq(CouponDO::getTemplateId, templateId));
    }

    /**
     * 插入优惠券。
     */
    @Insert(
            "INSERT INTO mall_coupon(template_id,member_id,status)"
            + " VALUES(#{templateId},#{memberId},'AVAILABLE')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCoupon(CouponDO row);

    /**
     * 锁定优惠券。
     */
    default int lockCoupon(
            Long memberId,
            Long id,
            String orderNo) {
        return this.update(null, Wrappers.<CouponDO>lambdaUpdate()
                .set(CouponDO::getStatus, "LOCKED")
                .set(CouponDO::getOrderNo, orderNo)
                .eq(CouponDO::getId, id)
                .eq(CouponDO::getMemberId, memberId)
                .eq(CouponDO::getStatus, "AVAILABLE"));
    }

    /**
     * 使用优惠券。
     */
    default int useCoupon(String orderNo) {
        return this.update(null, Wrappers.<CouponDO>lambdaUpdate()
                .set(CouponDO::getStatus, "USED")
                .eq(CouponDO::getOrderNo, orderNo)
                .eq(CouponDO::getStatus, "LOCKED"));
    }

    /**
     * 释放优惠券。
     */
    default int releaseCoupon(String orderNo) {
        return this.update(null, Wrappers.<CouponDO>lambdaUpdate()
                .set(CouponDO::getStatus, "AVAILABLE")
                .set(CouponDO::getOrderNo, null)
                .eq(CouponDO::getOrderNo, orderNo)
                .eq(CouponDO::getStatus, "LOCKED"));
    }

    /**
     * 已签发。
     */
    default CouponDO storefrontIssued(Long memberId, Long templateId) {
        return this.selectOne(Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getMemberId, memberId)
                .eq(CouponDO::getTemplateId, templateId));
    }

    /**
     * 券包。
     */
    default List<CouponDO> wallet(
            Long memberId,
            int offset,
            int limit) {
        return this.selectPage(new Page<>((long) offset / limit + 1, limit, false),
                Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getMemberId, memberId)
                .orderByDesc(CouponDO::getId)).getRecords();
    }

    /**
     * 券包数量。
     */
    default long walletCount(Long memberId) {
        return this.selectCount(Wrappers.<CouponDO>lambdaQuery()
                .eq(CouponDO::getMemberId, memberId));
    }
}
