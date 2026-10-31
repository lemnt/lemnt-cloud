package com.lemnt.mall.application.product.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.member.gateway.MemberLockGateway;
import com.lemnt.mall.domain.product.gateway.ProductBrowseHistoryGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.application.product.cmd.ProductBrowseHistoryCreateCmd;
import com.lemnt.mall.application.product.qry.ProductBrowseHistoryPageQry;
import com.lemnt.mall.application.product.dto.ProductBrowseHistoryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/** 浏览足迹应用编排，网关认证后按会员隔离数据。 */
@Component
@RequiredArgsConstructor
public class ProductBrowseHistoryService {
    private final ProductBrowseHistoryGateway gateway;
    private final MemberLockGateway locks;
    private final ProductSpuGateway spus;
    private final ProductSkuGateway skus;

    /** 浏览重排/裁剪必须在同一个会员锁事务内。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean create(ProductBrowseHistoryCreateCmd cmd) {
        member(cmd.userId());
        ProductCatalogValidation.id(cmd.spuId());
        locks.lockMember(cmd.userId());
        gateway.record(cmd.userId(), cmd.spuId());
        return true;
    }

    /** 批量删除仅隐藏本人，空数组不允许隐式清空。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean hide(Long userId, List<Long> spuIds) {
        member(userId);
        if (spuIds == null || spuIds.isEmpty() || spuIds.size() > 100
                || spuIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new ServiceException("请选择1至100个有效商品", 400);
        }
        locks.lockMember(userId);
        gateway.hide(userId, spuIds.stream().distinct().toList());
        return true;
    }

    /** 显式清空，只隐藏本人记录。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean clear(Long userId) {
        member(userId);
        locks.lockMember(userId);
        gateway.hide(userId, null);
        return true;
    }

    /** 查询范围由受保护适配层提供；批量补充商品资料，不制造缺失价格。 */
    @Transactional(readOnly = true)
    public PageResult<ProductBrowseHistoryDTO> page(ProductBrowseHistoryPageQry qry) {
        if (qry.pageNum() < 1 || qry.pageSize() < 1 || qry.pageSize() > 100
                || (qry.userId() != null && qry.userId() <= 0) || (qry.spuId() != null && qry.spuId() <= 0)) {
            throw new ServiceException("分页条件不合法", 400);
        }
        var page = gateway.page(qry.userId(), qry.spuId(), qry.userDeleted(), qry.pageNum(), qry.pageSize());
        if (page.getRecords().isEmpty()) {
            return PageResult.of(List.of(), page.getTotal());
        }
        var ids = page.getRecords().stream().map(value -> value.spuId()).distinct().toList();
        var products = spus.findSpus(ids).stream().collect(Collectors.toMap(value -> value.id(), value -> value));
        var prices = skus.skusBySpuIds(ids).stream().filter(value -> value.price() != null)
                .collect(Collectors.toMap(value -> value.spuId(), value -> value.price(), BigDecimal::min));
        return PageResult.of(page.getRecords().stream().map(value -> {
            var product = products.get(value.spuId());
            return new ProductBrowseHistoryDTO(value.id(), value.userId(), value.spuId(), product == null ? null : product.name(),
                    product == null ? null : product.image(), product == null ? null : prices.get(value.spuId()),
                    value.userDeleted(), value.createTime());
        }).toList(), page.getTotal());
    }

    private void member(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ServiceException("请先登录", 401);
        }
    }
}
