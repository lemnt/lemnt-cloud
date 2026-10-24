package com.lemnt.member.infrastructure.dataobject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lemnt.common.infrastructure.domain.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
@Data @EqualsAndHashCode(callSuper=true) @Accessors(chain=true) @TableName("member_user")
public class MemberDO extends BaseDO {
 @TableId(type=IdType.AUTO) private Long id;
 private String memberNo; private String nickname; private String mobile; private String email; private String avatar; private Integer status; private Long points;
}