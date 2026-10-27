package com.lemnt.mall.infrastructure.checkout.mapper;

import com.lemnt.mall.infrastructure.checkout.dataobject.CheckoutAccountDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/** CheckoutAccount 表数据访问接口。 */
@Mapper
public interface CheckoutAccountMapper extends BaseMapper<CheckoutAccountDO> {
    /**
     * 获取数据库当前时间。
     */
    @Select("SELECT CURRENT_TIMESTAMP")
    LocalDateTime now();

    /**
     * 确保会员。
     */
    @Insert(
            "INSERT INTO mall_checkout_account(member_id) VALUES(#{memberId}) ON DUPLICATE KEY"
            + " UPDATE member_id=member_id")
    int ensureMember(Long memberId);

    /**
     * 锁定会员。
     */
    @Select("SELECT member_id FROM mall_checkout_account WHERE member_id=#{memberId} FOR UPDATE")
    Long lockMember(Long memberId);
}
