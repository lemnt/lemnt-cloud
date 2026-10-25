package com.lemnt.member.infrastructure.account.mapper;

import com.lemnt.member.domain.account.model.MemberAdminFilter;
import com.lemnt.member.infrastructure.account.dataobject.*;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface MemberAdminQueryMapper {
    @Select(
            "<script>SELECT * FROM member_group WHERE 1=1  <if test=\"f.name != null\">AND name"
                + " LIKE CONCAT('%',#{f.name},'%')</if>  <if test=\"f.status != null\">AND"
                + " status=#{f.status}</if>  ORDER BY id DESC LIMIT #{f.limit} OFFSET"
                + " #{f.offset}</script>")
    List<GroupDO> groups(@Param("f") MemberAdminFilter f);

    @Select(
            "<script>SELECT COUNT(*) FROM member_group WHERE 1=1  <if test=\"f.name != null\">AND"
                + " name LIKE CONCAT('%',#{f.name},'%')</if>  <if test=\"f.status != null\">AND"
                + " status=#{f.status}</if> </script>")
    long groupsCount(@Param("f") MemberAdminFilter f);

    @Select(
            "<script>SELECT * FROM member_tag WHERE 1=1  <if test=\"f.name != null\">AND name LIKE"
                + " CONCAT('%',#{f.name},'%')</if>  ORDER BY id DESC LIMIT #{f.limit} OFFSET"
                + " #{f.offset}</script>")
    List<TagDO> tags(@Param("f") MemberAdminFilter f);

    @Select(
            "<script>SELECT COUNT(*) FROM member_tag WHERE 1=1  <if test=\"f.name != null\">AND"
                + " name LIKE CONCAT('%',#{f.name},'%')</if> </script>")
    long tagsCount(@Param("f") MemberAdminFilter f);

    @Select(
            "<script>SELECT * FROM member_asset_record WHERE 1=1  <if test=\"f.memberId !="
                + " null\">AND member_id=#{f.memberId}</if>  AND asset_type=#{f.assetType}  ORDER"
                + " BY id DESC LIMIT #{f.limit} OFFSET #{f.offset}</script>")
    List<AssetRecordDO> assets(@Param("f") MemberAdminFilter f);

    @Select(
            "<script>SELECT COUNT(*) FROM member_asset_record WHERE 1=1  <if test=\"f.memberId !="
                + " null\">AND member_id=#{f.memberId}</if>  AND asset_type=#{f.assetType}"
                + " </script>")
    long assetsCount(@Param("f") MemberAdminFilter f);

    @Select("SELECT * FROM member_asset_record WHERE id=#{id}")
    AssetRecordDO assetsDetail(Long id);

    @Select(
            "<script>SELECT * FROM member_level_record WHERE 1=1  <if test=\"f.memberId !="
                + " null\">AND member_id=#{f.memberId}</if>  ORDER BY id DESC LIMIT #{f.limit}"
                + " OFFSET #{f.offset}</script>")
    List<LevelRecordDO> levels(@Param("f") MemberAdminFilter f);

    @Select(
            "<script>SELECT COUNT(*) FROM member_level_record WHERE 1=1  <if test=\"f.memberId !="
                + " null\">AND member_id=#{f.memberId}</if> </script>")
    long levelsCount(@Param("f") MemberAdminFilter f);

    @Select("SELECT * FROM member_level_record WHERE id=#{id}")
    LevelRecordDO levelsDetail(Long id);

    @Select(
            "<script>SELECT * FROM member_sign_record WHERE 1=1  <if test=\"f.memberId !="
                + " null\">AND member_id=#{f.memberId}</if>  ORDER BY id DESC LIMIT #{f.limit}"
                + " OFFSET #{f.offset}</script>")
    List<SignRecordDO> signs(@Param("f") MemberAdminFilter f);

    @Select(
            "<script>SELECT COUNT(*) FROM member_sign_record WHERE 1=1  <if test=\"f.memberId !="
                + " null\">AND member_id=#{f.memberId}</if> </script>")
    long signsCount(@Param("f") MemberAdminFilter f);
}
