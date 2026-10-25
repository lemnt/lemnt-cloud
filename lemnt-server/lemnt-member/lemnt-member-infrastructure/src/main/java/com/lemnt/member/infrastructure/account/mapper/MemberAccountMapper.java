package com.lemnt.member.infrastructure.account.mapper;

import com.lemnt.member.infrastructure.account.dataobject.*;
import org.apache.ibatis.annotations.*;
import java.time.*;
import java.util.List;

@Mapper
public interface MemberAccountMapper {
    @Select("SELECT id FROM member_config WHERE id=1 FOR UPDATE")
    Integer lockCatalog();

    @Select("SELECT CURRENT_TIMESTAMP")
    LocalDateTime now();

    @Select("SELECT * FROM member_user WHERE id=#{id}")
    ProfileDO profile(Long id);

    @Select("SELECT * FROM member_user WHERE id=#{id} FOR UPDATE")
    ProfileDO lock(Long id);

    @Select(
            "<script>SELECT * FROM member_user <where><if test='mobile !="
                    + " null'>mobile=#{mobile}</if></where> ORDER BY id DESC LIMIT #{limit} OFFSET"
                    + " #{offset}</script>")
    List<ProfileDO> profiles(
            @Param("mobile") String mobile, @Param("offset") int offset, @Param("limit") int limit);

    @Select(
            "<script>SELECT COUNT(*) FROM member_user <where><if test='mobile !="
                    + " null'>mobile=#{mobile}</if></where></script>")
    long profileCount(String mobile);

    @Update(
            "UPDATE member_user SET"
                + " nickname=#{nickname},email=#{email},avatar=#{avatar},status=#{status},sex=#{sex},birthday=#{birthday}"
                + " WHERE id=#{id}")
    int profileUpdate(ProfileDO value);

    @Select(
            "SELECT * FROM member_address WHERE member_id=#{memberId} ORDER BY default_status"
                    + " DESC,id DESC")
    List<AddressDO> addresses(Long memberId);

    @Select("SELECT * FROM member_address WHERE member_id=#{memberId} AND id=#{id}")
    AddressDO address(@Param("memberId") Long memberId, @Param("id") Long id);

    @Insert(
            "INSERT INTO"
                + " member_address(member_id,name,mobile,area_id,detail_address,default_status)"
                + " VALUES(#{memberId},#{name},#{mobile},#{areaId},#{detailAddress},#{defaultStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int addressInsert(AddressDO value);

    @Update(
            "UPDATE member_address SET"
                + " name=#{name},mobile=#{mobile},area_id=#{areaId},detail_address=#{detailAddress},default_status=#{defaultStatus}"
                + " WHERE id=#{id} AND member_id=#{memberId}")
    int addressUpdate(AddressDO value);

    @Delete("DELETE FROM member_address WHERE member_id=#{memberId} AND id=#{id}")
    int addressDelete(@Param("memberId") Long memberId, @Param("id") Long id);

    @Update("UPDATE member_address SET default_status=FALSE WHERE member_id=#{memberId}")
    int clearDefaultAddress(Long memberId);

    @Select(
            "SELECT c.* FROM member_credential c JOIN member_user u ON u.id=c.member_id WHERE"
                    + " u.mobile=#{mobile}")
    CredentialDO credential(String mobile);

    @Select("SELECT * FROM member_credential WHERE member_id=#{memberId} FOR UPDATE")
    CredentialDO credentialForMember(Long memberId);

    @Insert(
            "INSERT INTO member_credential(member_id,password_hash) VALUES(#{memberId},#{hash}) ON"
                    + " DUPLICATE KEY UPDATE password_hash=#{hash}")
    int password(@Param("memberId") Long memberId, @Param("hash") String hash);

    @Insert(
            "INSERT INTO"
                + " member_session(id,member_id,access_hash,refresh_hash,expires_at,refresh_expires_at)"
                + " VALUES(#{id},#{memberId},#{accessHash},#{refreshHash},#{expiresAt},#{refreshExpiresAt})")
    int sessionSave(SessionDO value);

    @Select("SELECT * FROM member_session WHERE access_hash=#{hash}")
    SessionDO sessionAccess(String hash);

    @Select("SELECT * FROM member_session WHERE refresh_hash=#{hash}")
    SessionDO sessionRefresh(String hash);

    @Select("SELECT * FROM member_session WHERE refresh_hash=#{hash} FOR UPDATE")
    SessionDO lockSession(String hash);

    @Delete("DELETE FROM member_session WHERE id=#{id}")
    int sessionDelete(String id);

    @Delete("DELETE FROM member_session WHERE member_id=#{memberId}")
    int sessionsRevoke(Long memberId);

    @Select(
            "SELECT * FROM member_asset_record WHERE member_id=#{memberId} AND"
                    + " asset_type=#{assetType} AND biz_type=#{bizType} AND biz_no=#{bizNo}")
    AssetRecordDO assetRequest(
            @Param("memberId") Long memberId,
            @Param("assetType") String assetType,
            @Param("bizType") String bizType,
            @Param("bizNo") String bizNo);

    @Insert(
            "INSERT INTO"
                + " member_asset_record(member_id,asset_type,delta,balance,biz_type,biz_no,reason,create_time)"
                + " VALUES(#{memberId},#{assetType},#{delta},#{balance},#{bizType},#{bizNo},#{reason},#{createTime})")
    int assetSave(AssetRecordDO value);

    @Update("UPDATE member_user SET points=#{balance} WHERE id=#{memberId}")
    int points(@Param("memberId") Long memberId, @Param("balance") Long balance);

    @Update("UPDATE member_user SET experience=#{balance} WHERE id=#{memberId}")
    int experience(@Param("memberId") Long memberId, @Param("balance") Long balance);

    @Select(
            "SELECT * FROM member_asset_record WHERE member_id=#{memberId} AND asset_type=#{type}"
                    + " ORDER BY id DESC LIMIT #{limit} OFFSET #{offset}")
    List<AssetRecordDO> assets(
            @Param("memberId") Long memberId,
            @Param("type") String type,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Select(
            "SELECT COUNT(*) FROM member_asset_record WHERE member_id=#{memberId} AND"
                    + " asset_type=#{type}")
    long assetCount(@Param("memberId") Long memberId, @Param("type") String type);

    @Select("SELECT * FROM member_level ORDER BY level")
    List<LevelDO> levels();

    @Select("SELECT * FROM member_level WHERE id=#{id}")
    LevelDO level(Long id);

    @Insert(
            "INSERT INTO"
                + " member_level(name,level,experience,discount_percent,icon,background_url,status)"
                + " VALUES(#{name},#{level},#{experience},#{discountPercent},#{icon},#{backgroundUrl},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int levelInsert(LevelDO value);

    @Update(
            "UPDATE member_level SET"
                + " name=#{name},level=#{level},experience=#{experience},discount_percent=#{discountPercent},icon=#{icon},background_url=#{backgroundUrl},status=#{status}"
                + " WHERE id=#{id}")
    int levelUpdate(LevelDO value);

    @Delete(
            "DELETE FROM member_level WHERE id=#{id} AND NOT EXISTS(SELECT 1 FROM member_user WHERE"
                    + " level_id=#{id})")
    int levelDelete(Long id);

    @Update("UPDATE member_user SET level_id=#{levelId} WHERE id=#{memberId}")
    int assignLevel(@Param("memberId") Long memberId, @Param("levelId") Long levelId);

    @Insert(
            "INSERT INTO member_level_record(member_id,level_id,experience,reason,create_time)"
                    + " VALUES(#{memberId},#{levelId},#{experience},#{reason},#{createTime})")
    int levelRecord(LevelRecordDO value);

    @Select(
            "SELECT * FROM member_level_record WHERE member_id=#{memberId} ORDER BY id DESC LIMIT"
                    + " #{limit} OFFSET #{offset}")
    List<LevelRecordDO> levelRecords(
            @Param("memberId") Long memberId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM member_level_record WHERE member_id=#{memberId}")
    long levelRecordCount(Long memberId);

    @Select("SELECT * FROM member_group ORDER BY id")
    List<GroupDO> groups();

    @Select("SELECT * FROM member_group WHERE id=#{id}")
    GroupDO group(Long id);

    @Insert("INSERT INTO member_group(name,remark,status) VALUES(#{name},#{remark},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int groupInsert(GroupDO value);

    @Update("UPDATE member_group SET name=#{name},remark=#{remark},status=#{status} WHERE id=#{id}")
    int groupUpdate(GroupDO value);

    @Delete(
            "DELETE FROM member_group WHERE id=#{id} AND NOT EXISTS(SELECT 1 FROM member_user WHERE"
                    + " group_id=#{id})")
    int groupDelete(Long id);

    @Update("UPDATE member_user SET group_id=#{groupId} WHERE id=#{memberId}")
    int assignGroup(@Param("memberId") Long memberId, @Param("groupId") Long groupId);

    @Select("SELECT * FROM member_tag ORDER BY id")
    List<TagDO> tags();

    @Select("SELECT * FROM member_tag WHERE id=#{id}")
    TagDO tag(Long id);

    @Insert("INSERT INTO member_tag(name,remark) VALUES(#{name},#{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int tagInsert(TagDO value);

    @Update("UPDATE member_tag SET name=#{name},remark=#{remark} WHERE id=#{id}")
    int tagUpdate(TagDO value);

    @Delete(
            "DELETE FROM member_tag WHERE id=#{id} AND NOT EXISTS(SELECT 1 FROM member_tag_link"
                    + " WHERE tag_id=#{id})")
    int tagDelete(Long id);

    @Select("SELECT tag_id FROM member_tag_link WHERE member_id=#{memberId} ORDER BY tag_id")
    List<Long> memberTags(Long memberId);

    @Delete("DELETE FROM member_tag_link WHERE member_id=#{memberId}")
    int clearTags(Long memberId);

    @Insert("INSERT INTO member_tag_link(member_id,tag_id) VALUES(#{memberId},#{tagId})")
    int addTag(@Param("memberId") Long memberId, @Param("tagId") Long tagId);

    @Select("SELECT * FROM member_sign_config ORDER BY signin_day")
    List<SignConfigDO> signConfigs();

    @Insert(
            "INSERT INTO member_sign_config(signin_day,point,experience,status)"
                + " VALUES(#{signinDay},#{point},#{experience},#{status}) ON DUPLICATE KEY UPDATE"
                + " point=#{point},experience=#{experience},status=#{status}")
    int signConfigSave(SignConfigDO value);

    @Delete("DELETE FROM member_sign_config WHERE signin_day=#{day}")
    int signConfigDelete(Integer day);

    @Select("SELECT * FROM member_sign_record WHERE member_id=#{memberId} AND sign_date=#{date}")
    SignRecordDO signDate(@Param("memberId") Long memberId, @Param("date") LocalDate date);

    @Select(
            "SELECT * FROM member_sign_record WHERE member_id=#{memberId} ORDER BY sign_date DESC"
                    + " LIMIT 1")
    SignRecordDO lastSign(Long memberId);

    @Insert(
            "INSERT INTO member_sign_record(member_id,sign_date,signin_day,point,experience)"
                    + " VALUES(#{memberId},#{signDate},#{signinDay},#{point},#{experience})")
    int signSave(SignRecordDO value);

    @Select(
            "SELECT * FROM member_sign_record WHERE member_id=#{memberId} ORDER BY id DESC LIMIT"
                    + " #{limit} OFFSET #{offset}")
    List<SignRecordDO> signs(
            @Param("memberId") Long memberId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM member_sign_record WHERE member_id=#{memberId}")
    long signCount(Long memberId);

    @Select(
            "SELECT"
                + " point_trade_deduct_enable,point_trade_deduct_unit_price,point_trade_deduct_max_price,point_trade_give_point"
                + " FROM member_config WHERE id=1")
    ConfigDO config();

    @Update(
            "UPDATE member_config SET"
                + " point_trade_deduct_enable=#{pointTradeDeductEnable},point_trade_deduct_unit_price=#{pointTradeDeductUnitPrice},point_trade_deduct_max_price=#{pointTradeDeductMaxPrice},point_trade_give_point=#{pointTradeGivePoint}"
                + " WHERE id=1")
    int configSave(ConfigDO value);

    @Insert(
            "INSERT INTO member_login_attempt(login_hash) VALUES(#{hash}) ON DUPLICATE KEY UPDATE"
                    + " login_hash=login_hash")
    int ensureLogin(String hash);

    @Select("SELECT * FROM member_login_attempt WHERE login_hash=#{hash} FOR UPDATE")
    LoginAttemptDO loginAttempt(String hash);

    @Update(
            "UPDATE member_login_attempt SET failures=#{failures},window_start=#{windowStart} WHERE"
                    + " login_hash=#{loginHash}")
    int loginAttemptUpdate(LoginAttemptDO value);
}
