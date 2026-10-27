package com.lemnt.member.infrastructure.user.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/** member_tag_link 表数据对象，联合主键通过条件构造器操作。 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_tag_link")
public class MemberTagLinkDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会员编号。 */
    private Long memberId;

    /** 标签编号。 */
    private Long tagId;
}
