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
        if (qry.orderId() != null) {
            ProductCatalogValidation.id(qry.orderId());
        }
        if ((qry.createTimeStart() != null && qry.createTimeEnd() != null && qry.createTimeStart().isAfter(qry.createTimeEnd()))
                || (qry.userNickname() != null && qry.userNickname().length() > 255)
                || (qry.spuName() != null && qry.spuName().length() > 255)) {
            throw new com.lemnt.common.core.exception.ServiceException("评价筛选名称或时间范围不合法", 400);
        }
        var page = productCommentGateway.page(new CommentFilter(qry.spuId(), qry.memberId(), qry.score(), qry.visible(),
                qry.replied(), qry.pageNum(), qry.pageSize(), text(qry.userNickname()), qry.orderId(),
                text(qry.spuName()), qry.createTimeStart(), qry.createTimeEnd()));
        return PageResult.of(page.getRecords().stream().map(CommentDTOConverter::toDTO).toList(), page.getTotal());
    }
    /** 空白搜索词不构造模糊查询。 */
    private String text(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
