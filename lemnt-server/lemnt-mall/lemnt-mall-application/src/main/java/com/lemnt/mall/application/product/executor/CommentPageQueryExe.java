package com.lemnt.mall.application.product.executor;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.product.converter.CommentDTOConverter;
import com.lemnt.mall.application.product.dto.ProductCommentDTO;
import com.lemnt.mall.application.product.qry.CommentPageQry;
import com.lemnt.mall.domain.product.gateway.ProductCommentGateway;
import com.lemnt.mall.domain.product.model.CommentFilter;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 商品评价分页查询。 */
@Component
@RequiredArgsConstructor
public class CommentPageQueryExe {
    private final ProductCommentGateway productCommentGateway;

    /** 按商品、会员、评分、显示与回复状态筛选。 */
    public PageResult<ProductCommentDTO> execute(CommentPageQry qry) {
        if (qry == null || qry.pageNum() < 1 || qry.pageNum() > 1000000 || qry.pageSize() < 1 || qry.pageSize() > 100
                || (qry.score() != null && (qry.score() < 1 || qry.score() > 5))) {
            throw new IllegalArgumentException("评价查询参数不合法");
        }
        if (qry.spuId() != null) {
            ProductCatalogValidation.id(qry.spuId());
        }
        if (qry.memberId() != null) {
            ProductCatalogValidation.id(qry.memberId());
        }
        var page = productCommentGateway.page(new CommentFilter(qry.spuId(), qry.memberId(), qry.score(), qry.visible(),
                qry.replied(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(CommentDTOConverter::toDTO).toList(), page.getTotal());
    }
}
