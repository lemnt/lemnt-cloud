package com.lemnt.member.infrastructure.account.mapper;

import com.lemnt.member.infrastructure.account.dataobject.*;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MemberVerificationMapper {
    @Insert(
            "INSERT INTO member_sms_challenge(mobile,scene) VALUES(#{mobile},#{scene}) ON DUPLICATE"
                + " KEY UPDATE mobile=mobile")
    int ensureSms(@Param("mobile") String mobile, @Param("scene") String scene);

    @Select(
            "SELECT * FROM member_sms_challenge WHERE mobile=#{mobile} AND scene=#{scene} FOR"
                + " UPDATE")
    SmsChallengeDO lockSms(@Param("mobile") String mobile, @Param("scene") String scene);

    @Update(
            "UPDATE member_sms_challenge SET"
                + " code_hash=#{codeHash},expires_at=#{expiresAt},attempts=#{attempts},last_sent_at=#{lastSentAt},sent_date=#{sentDate},daily_count=#{dailyCount}"
                + " WHERE mobile=#{mobile} AND scene=#{scene}")
    int smsSave(SmsChallengeDO value);

    @Insert(
            "INSERT INTO member_sms_budget(budget_day) VALUES(#{day}) ON DUPLICATE KEY UPDATE"
                + " budget_day=budget_day")
    int ensureBudget(java.time.LocalDate day);

    @Select("SELECT requests FROM member_sms_budget WHERE budget_day=#{day} FOR UPDATE")
    int lockBudget(java.time.LocalDate day);

    @Update("UPDATE member_sms_budget SET requests=requests+1 WHERE budget_day=#{day}")
    int addBudget(java.time.LocalDate day);

    @Insert(
            "INSERT INTO member_social_state(state_hash,provider,purpose,member_id,expires_at)"
                + " VALUES(#{stateHash},#{provider},#{purpose},#{memberId},#{expiresAt})")
    int stateSave(SocialStateDO value);

    @Select("SELECT * FROM member_social_state WHERE state_hash=#{hash} FOR UPDATE")
    SocialStateDO lockState(String hash);

    @Delete("DELETE FROM member_social_state WHERE state_hash=#{hash}")
    int stateDelete(String hash);

    @Select("SELECT * FROM member_social_binding WHERE provider=#{provider} AND subject=#{subject}")
    SocialBindingDO binding(@Param("provider") String provider, @Param("subject") String subject);

    @Select("SELECT * FROM member_social_binding WHERE member_id=#{memberId} ORDER BY provider")
    List<SocialBindingDO> bindings(Long memberId);

    @Insert(
            "INSERT INTO member_social_binding(provider,subject,member_id)"
                + " VALUES(#{provider},#{subject},#{memberId})")
    int bind(SocialBindingDO value);

    @Delete(
            "DELETE FROM member_social_binding WHERE member_id=#{memberId} AND"
                + " provider=#{provider}")
    int unbind(@Param("memberId") Long memberId, @Param("provider") String provider);

    @Update("UPDATE member_user SET mobile=#{mobile} WHERE id=#{memberId}")
    int updateMobile(@Param("memberId") Long memberId, @Param("mobile") String mobile);
}
