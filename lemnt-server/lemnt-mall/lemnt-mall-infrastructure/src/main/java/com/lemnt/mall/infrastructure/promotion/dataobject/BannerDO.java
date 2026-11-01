package com.lemnt.mall.infrastructure.promotion.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/** Banner 持久化对象。 */
@Data
@TableName("mall_promotion_banner")
public class BannerDO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String title;
    private String url;
    private String picUrl;
    private Integer sort;
    private Integer status;
    private Integer position;
    private String memo;
    private Integer browseCount;
}
