package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

import java.time.LocalDateTime;

/** 装修模板持久化对象。 */
@Data
@TableName("mall_promotion_diy_template")
public class DiyTemplateDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;
    private Boolean used;
    private LocalDateTime usedTime;
    private String remark;
    private String previewPicUrls;
    private String property;
}
