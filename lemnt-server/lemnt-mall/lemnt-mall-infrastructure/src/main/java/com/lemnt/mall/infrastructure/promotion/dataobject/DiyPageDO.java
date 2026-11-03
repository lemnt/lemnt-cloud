package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** 装修页面持久化对象。 */
@Data
@TableName("mall_promotion_diy_page")
public class DiyPageDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long templateId;
    private String name;
    private String remark;
    private String previewPicUrls;
    private String property;
}
