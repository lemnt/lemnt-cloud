package com.lemnt.mall.application.product.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.product.converter.CommentDTOConverter;
import com.lemnt.mall.application.product.dto.ProductCommentDTO;
import com.lemnt.mall.application.product.qry.CommentPublicPageQry;
import com.lemnt.mall.domain.product.gateway.ProductCommentGateway;
import com.lemnt.mall.domain.product.model.PublicCommentFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 公开商品评价查询，不复用后台可控制visible/会员条件的查询。 */
@Component
@RequiredArgsConstructor
public class CommentPublicPageQueryExe {
    private final ProductCommentGateway gateway;
    /** 好评>=4、中评=3、差评<3，固定只查可见评价。 */
    public PageResult<ProductCommentDTO> execute(CommentPublicPageQry qry) {
        if (qry == null || qry.spuId() == null || qry.spuId() <= 0 || qry.type() < 0 || qry.type() > 3
                || qry.pageNum() < 1 || qry.pageNum() > 1000000 || qry.pageSize() < 1 || qry.pageSize() > 100) {
            throw new ServiceException("评价查询参数不合法", 400);
        }
        var result = gateway.pageVisible(new PublicCommentFilter(qry.spuId(), qry.type(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(result.getRecords().stream().map(CommentDTOConverter::toDTO).toList(), result.getTotal());
    }
}
