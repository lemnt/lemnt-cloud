package com.lemnt.mall.infrastructure.product.dataobject;

import com.lemnt.common.infrastructure.domain.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/** mall_product_browse_history数据对象。 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mall_product_browse_history")
public class ProductBrowseHistoryDO extends BaseDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long spuId;
    /** 与源BaseDO一致，删除保留历史行。 */
    @TableLogic
    private Boolean deleted;
    private Boolean userDeleted;
}
