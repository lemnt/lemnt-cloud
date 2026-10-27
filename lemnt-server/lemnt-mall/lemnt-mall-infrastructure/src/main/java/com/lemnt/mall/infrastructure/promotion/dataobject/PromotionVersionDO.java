package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 营销版本数据对象。
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("mall_promotion_version")
public class PromotionVersionDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long rootId;
    private Integer version;
    private String draftJson;
    private String ruleHash;
    private String algorithmVersion;
    private Long startsAtEpochMs;
    private Long endsAtEpochMs;
    private Long operatorId;
    private Long publishedAtEpochMs;
}
