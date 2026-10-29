package com.lemnt.mall.application.product.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.domain.member.gateway.MemberLockGateway;
import com.lemnt.mall.domain.product.gateway.ProductFavoriteGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.application.product.cmd.ProductFavoriteCreateCmd;
import com.lemnt.mall.application.product.qry.ProductFavoritePageQry;
import com.lemnt.mall.application.product.dto.ProductFavoriteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/** 商品收藏应用编排，网关认证后按会员隔离数据。 */
@Component
@RequiredArgsConstructor
public class ProductFavoriteService {
    private final ProductFavoriteGateway gateway;
    private final MemberLockGateway locks;
    private final ProductSpuGateway spus;
    private final ProductSkuGateway skus;

    /** 重复收藏按源项目报已存在，复用基础会员锁覆盖事务。 */
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProductFavoriteCreateCmd cmd) {
        member(cmd.userId());
        ProductCatalogValidation.id(cmd.spuId());
        locks.lockMember(cmd.userId());
        if (gateway.find(cmd.userId(), cmd.spuId()) != null) {
            throw new ServiceException("商品已收藏", 409);
        }
        return gateway.save(cmd.userId(), cmd.spuId()).id();
    }

    /** 删除始终同时匹配会员和SPU。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean remove(Long userId, Long spuId) {
        member(userId);
        ProductCatalogValidation.id(spuId);
        locks.lockMember(userId);
        if (!gateway.remove(userId, spuId)) {
            throw new ServiceException("商品收藏不存在", 404);
        }
        return true;
    }

    /** 读取状态供写入结果不确定时核验。 */
    public boolean exists(Long userId, Long spuId) {
        member(userId);
        ProductCatalogValidation.id(spuId);
        return gateway.find(userId, spuId) != null;
    }

    /** 本人收藏数量。 */
    public long count(Long userId) {
        member(userId);
        return gateway.count(userId);
    }

    /** 查询范围由受保护适配层提供；批量补充商品资料，不制造缺失价格。 */
    @Transactional(readOnly = true)
    public PageResult<ProductFavoriteDTO> page(ProductFavoritePageQry qry) {
        if (qry.pageNum() < 1 || qry.pageSize() < 1 || qry.pageSize() > 100
                || (qry.userId() != null && qry.userId() <= 0) || (qry.spuId() != null && qry.spuId() <= 0)) {
            throw new ServiceException("分页条件不合法", 400);
        }
        var page = gateway.page(qry.userId(), qry.spuId(), qry.pageNum(), qry.pageSize());
        if (page.getRecords().isEmpty()) {
            return PageResult.of(List.of(), page.getTotal());
        }
        var ids = page.getRecords().stream().map(value -> value.spuId()).distinct().toList();
        var products = spus.findSpus(ids).stream().collect(Collectors.toMap(value -> value.id(), value -> value));
        var prices = skus.skusBySpuIds(ids).stream().filter(value -> value.price() != null)
                .collect(Collectors.toMap(value -> value.spuId(), value -> value.price(), BigDecimal::min));
        return PageResult.of(page.getRecords().stream().map(value -> {
            var product = products.get(value.spuId());
            return new ProductFavoriteDTO(value.id(), value.userId(), value.spuId(), product == null ? null : product.name(),
                    product == null ? null : product.image(), product == null ? null : prices.get(value.spuId()),
                    value.createTime());
        }).toList(), page.getTotal());
    }

    private void member(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ServiceException("请先登录", 401);
        }
    }
}
