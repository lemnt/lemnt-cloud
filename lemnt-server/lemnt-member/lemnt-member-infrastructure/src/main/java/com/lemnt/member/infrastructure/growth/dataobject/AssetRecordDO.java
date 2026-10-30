package com.lemnt.member.infrastructure.growth.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资产记录数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_asset_record")
public class AssetRecordDO implements Serializable {
    /** 当前会员昵称关联值，不写资产表。 */
    @com.baomidou.mybatisplus.annotation.TableField(exist=false)
    private String nickname;


    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 会员编号。 */
    private Long memberId;
    /** 资产类型：POINT积分，EXPERIENCE经验。 */
    private String assetType;
    /** 本次变更量。 */
    private Long delta;
    /** 变更后余额。 */
    private Long balance;
    /** 业务来源类型。 */
    private String bizType;
    /** 已确认的源积分枚举与写入时展示快照。 */
    private Integer pointBizType;
    private String title;
    private String description;
    /** 业务幂等编号。 */
    private String bizNo;
    /** 变更原因。 */
    private String reason;
    /** 创建时间。 */
    private LocalDateTime createTime;
}
