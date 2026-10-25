package com.lemnt.trade.infrastructure.checkout.mapper;

import com.lemnt.trade.infrastructure.checkout.dataobject.*;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.time.LocalDateTime;

@Mapper
public interface CheckoutMapper {
    @Select("SELECT CURRENT_TIMESTAMP")
    LocalDateTime now();

    @Insert(
            "INSERT INTO trade_checkout_account(member_id) VALUES(#{memberId}) ON DUPLICATE KEY"
                + " UPDATE member_id=member_id")
    int ensureMember(Long memberId);

    @Select("SELECT member_id FROM trade_checkout_account WHERE member_id=#{memberId} FOR UPDATE")
    Long lockMember(Long memberId);

    @Select(
            "SELECT COUNT(*) FROM trade_checkout_cart c JOIN trade_order o ON o.order_no=c.order_no"
                + " WHERE c.cart_id=#{id} AND o.status='RESERVING'")
    int cartInCheckout(Long id);

    @Select("SELECT * FROM trade_cart WHERE member_id=#{memberId} ORDER BY id LIMIT 201")
    List<CartDO> cart(Long memberId);

    @Select("SELECT * FROM trade_cart WHERE member_id=#{memberId} AND product_id=#{productId}")
    CartDO cartItem(@Param("memberId") Long memberId, @Param("productId") Long productId);

    @Insert(
            "INSERT INTO trade_cart(member_id,product_id,quantity,selected,version)"
                + " VALUES(#{memberId},#{productId},#{quantity},#{selected},0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCart(CartDO row);

    @Update(
            "UPDATE trade_cart SET quantity=#{quantity},selected=#{selected},version=version+1"
                + " WHERE id=#{id} AND member_id=#{memberId}")
    int updateCart(CartDO row);

    @Delete("DELETE FROM trade_cart WHERE member_id=#{memberId} AND product_id=#{productId}")
    int deleteCart(@Param("memberId") Long memberId, @Param("productId") Long productId);

    @Delete(
            "DELETE FROM trade_cart WHERE member_id=#{memberId} AND id=#{id} AND"
                + " version=#{version}")
    int consumeCart(
            @Param("memberId") Long memberId, @Param("id") Long id, @Param("version") long version);

    @Select("SELECT * FROM trade_coupon_template ORDER BY id DESC LIMIT 1001")
    List<CouponTemplateDO> templates();

    @Select("SELECT * FROM trade_coupon_template WHERE id=#{id}")
    CouponTemplateDO template(Long id);

    @Select("SELECT * FROM trade_coupon_template WHERE id=#{id} FOR UPDATE")
    CouponTemplateDO lockTemplate(Long id);

    @Insert(
            "INSERT INTO"
                + " trade_coupon_template(code,name,kind,threshold,fixed_amount,discount_percent,discount_limit,total_count,issued_count,valid_from,valid_until,enabled)"
                + " VALUES(#{code},#{name},#{kind},#{threshold},#{fixedAmount},#{discountPercent},#{discountLimit},#{totalCount},0,#{validFrom},#{validUntil},TRUE)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertTemplate(CouponTemplateDO row);

    @Update("UPDATE trade_coupon_template SET enabled=#{enabled} WHERE id=#{id}")
    int enableTemplate(@Param("id") Long id, @Param("enabled") boolean enabled);

    @Update(
            "UPDATE trade_coupon_template SET issued_count=issued_count+1 WHERE id=#{id} AND"
                + " enabled=TRUE AND valid_from<=CURRENT_TIMESTAMP AND"
                + " valid_until>CURRENT_TIMESTAMP AND issued_count<total_count")
    int takeQuota(Long id);

    @Select("SELECT * FROM trade_coupon WHERE member_id=#{memberId} ORDER BY id DESC LIMIT 1001")
    List<CouponDO> coupons(Long memberId);

    @Select("SELECT * FROM trade_coupon WHERE member_id=#{memberId} AND id=#{id}")
    CouponDO coupon(@Param("memberId") Long memberId, @Param("id") Long id);

    @Select("SELECT * FROM trade_coupon WHERE member_id=#{memberId} AND id=#{id} FOR UPDATE")
    CouponDO lockCouponRow(@Param("memberId") Long memberId, @Param("id") Long id);

    @Select("SELECT * FROM trade_coupon WHERE member_id=#{memberId} AND template_id=#{templateId}")
    CouponDO issued(@Param("memberId") Long memberId, @Param("templateId") Long templateId);

    @Insert(
            "INSERT INTO trade_coupon(template_id,member_id,status)"
                + " VALUES(#{templateId},#{memberId},'AVAILABLE')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCoupon(CouponDO row);

    @Update(
            "UPDATE trade_coupon SET status='LOCKED',order_no=#{orderNo} WHERE id=#{id} AND"
                + " member_id=#{memberId} AND status='AVAILABLE'")
    int lockCoupon(
            @Param("memberId") Long memberId,
            @Param("id") Long id,
            @Param("orderNo") String orderNo);

    @Update("UPDATE trade_coupon SET status='USED' WHERE order_no=#{orderNo} AND status='LOCKED'")
    int useCoupon(String orderNo);

    @Update(
            "UPDATE trade_coupon SET status='AVAILABLE',order_no=NULL WHERE order_no=#{orderNo} AND"
                + " status='LOCKED'")
    int releaseCoupon(String orderNo);

    @Select("SELECT * FROM trade_checkout WHERE member_id=#{memberId} AND request_no=#{requestNo}")
    CheckoutDO request(@Param("memberId") Long memberId, @Param("requestNo") String requestNo);

    @Select("SELECT * FROM trade_checkout WHERE order_no=#{orderNo}")
    CheckoutDO receipt(String orderNo);

    @Insert(
            "INSERT INTO"
                + " trade_checkout(order_no,member_id,request_no,fingerprint,original_amount,discount_amount,payable_amount,coupon_id)"
                + " VALUES(#{orderNo},#{memberId},#{requestNo},#{fingerprint},#{originalAmount},#{discountAmount},#{payableAmount},#{couponId})")
    int insertCheckout(CheckoutDO row);

    @Insert(
            "INSERT INTO trade_checkout_cart(order_no,cart_id,cart_version)"
                + " VALUES(#{orderNo},#{cartId},#{cartVersion})")
    int insertReference(CheckoutCartDO row);

    @Select("SELECT * FROM trade_checkout_cart WHERE order_no=#{orderNo} ORDER BY cart_id")
    List<CheckoutCartDO> references(String orderNo);
}
