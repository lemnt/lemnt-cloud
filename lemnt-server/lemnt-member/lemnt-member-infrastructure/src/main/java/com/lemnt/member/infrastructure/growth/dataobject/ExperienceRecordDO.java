package com.lemnt.member.infrastructure.growth.dataobject;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import com.lemnt.common.infrastructure.domain.BaseDO;
import lombok.EqualsAndHashCode;
/** 源经验表字段；兼容保留原业务码和原因，不与标题混淆。 */
@Data @EqualsAndHashCode(callSuper=false) @TableName("member_experience_record")
public class ExperienceRecordDO extends BaseDO {
    @TableId(type=IdType.AUTO) private Long id;
    private Long userId;
    private String bizId;
    private Integer bizType;
    private String title;
    private String description;
    private Long experience;
    private Long totalExperience;
    private String bizCode;
    private String reason;
    @TableField(exist=false) private String nickname;
}
