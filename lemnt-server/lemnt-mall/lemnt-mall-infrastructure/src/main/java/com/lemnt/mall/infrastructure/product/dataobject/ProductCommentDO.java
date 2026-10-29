package com.lemnt.mall.infrastructure.product.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/** 商品评价表。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mall_product_comment")
public class ProductCommentDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long spuId;
    private Long skuId;
    private String orderNo;
    private Long memberId;
    private Integer score;
    private String content;
    private Boolean visible;
    private String replyContent;
    private Long replyUserId;
    private LocalDateTime replyTime;
    private Integer descriptionScores;
    private Integer benefitScores;
    private String picUrls;
    private Boolean anonymous;
    private String userNickname;
    private String userAvatar;
    private Long orderId;
    private Long orderItemId;
    private String spuName;
    private String skuPicUrl;
    private String skuProperties;
    private Boolean replyStatus;
    @TableLogic
    private Boolean deleted;
}
