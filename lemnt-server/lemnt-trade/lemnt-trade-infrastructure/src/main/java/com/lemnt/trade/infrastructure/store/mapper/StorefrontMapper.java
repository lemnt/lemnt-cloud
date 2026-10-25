package com.lemnt.trade.infrastructure.store.mapper;

import com.lemnt.trade.infrastructure.store.dataobject.CartAdditionDO;
import com.lemnt.trade.infrastructure.checkout.dataobject.*;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface StorefrontMapper {
    @Select("SELECT enabled FROM trade_coupon_public_claim WHERE template_id=#{id}")
    Boolean claimable(Long id);

    @Insert(
            "INSERT INTO trade_coupon_public_claim(template_id,enabled) VALUES(#{id},#{enabled}) ON"
                + " DUPLICATE KEY UPDATE enabled=#{enabled}")
    void setClaimable(@Param("id") Long id, @Param("enabled") boolean enabled);

    @Select(
            "SELECT template_id FROM trade_coupon_public_claim WHERE enabled=TRUE ORDER BY"
                + " template_id LIMIT 1001")
    List<Long> publicTemplateIds();

    String OFFER =
            " FROM trade_coupon_template t JOIN trade_coupon_public_claim p ON p.template_id=t.id"
                + " WHERE p.enabled=TRUE AND t.enabled=TRUE AND t.valid_from<=CURRENT_TIMESTAMP AND"
                + " t.valid_until>CURRENT_TIMESTAMP ";

    @Select("SELECT t.*" + OFFER + " ORDER BY t.id DESC LIMIT #{limit} OFFSET #{offset}")
    List<CouponTemplateDO> offers(@Param("offset") int offset, @Param("limit") int limit);

    @Select("SELECT COUNT(*)" + OFFER)
    long offerCount();

    @Select("SELECT * FROM trade_coupon WHERE member_id=#{memberId} AND template_id=#{templateId}")
    CouponDO issued(@Param("memberId") Long memberId, @Param("templateId") Long templateId);

    @Select(
            "SELECT * FROM trade_coupon WHERE member_id=#{memberId} ORDER BY id DESC LIMIT #{limit}"
                + " OFFSET #{offset}")
    List<CouponDO> wallet(
            @Param("memberId") Long memberId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM trade_coupon WHERE member_id=#{memberId}")
    long walletCount(Long memberId);

    @Select(
            "SELECT * FROM trade_cart_add_request WHERE member_id=#{memberId} AND"
                + " request_no=#{requestNo}")
    CartAdditionDO addition(@Param("memberId") Long memberId, @Param("requestNo") String requestNo);

    @Insert(
            "INSERT INTO"
                + " trade_cart_add_request(member_id,request_no,product_id,quantity,cart_id,result_quantity,selected,cart_version)"
                + " VALUES(#{memberId},#{requestNo},#{productId},#{quantity},#{cartId},#{resultQuantity},#{selected},#{cartVersion})")
    void additionSave(CartAdditionDO value);
}
