package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 营销请求数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_promotion_admin_request")
public class PromotionRequestDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String action;
    private String fingerprint;
    private Long resultId;
    private Long resultRevision;
    private String resultState;
    private Integer resultActiveVersion;
    private Integer resultLastVersion;

    /** 操作人编号。 */
    private Long operatorId;

    /** 请求幂等号。 */
    private String requestNo;

    /** 创建时间，毫秒时间戳。 */
    private Long createdAtEpochMs;

    /** 完成时间，毫秒时间戳。 */
    private Long completedAtEpochMs;
}
