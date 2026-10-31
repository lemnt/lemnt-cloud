package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.cmd.PointActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.PointActivityUpdateCmd;
import com.lemnt.mall.application.promotion.cmd.PointProductCmd;
import com.lemnt.mall.application.promotion.dto.PointActivityDTO;
import com.lemnt.mall.application.promotion.dto.PointJoinDTO;
import com.lemnt.mall.application.promotion.dto.PointProductDTO;
import com.lemnt.mall.application.promotion.qry.PointActivityPageQry;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.domain.promotion.gateway.PointActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.PointProductGateway;
import com.lemnt.mall.domain.promotion.model.PointActivity;
import com.lemnt.mall.domain.promotion.model.PointFilter;
import com.lemnt.mall.domain.promotion.model.PointProduct;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** 积分商城活动直接管理，同一商品只能参加一个开启的积分活动。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class PointActivityManagementService {
    private final PointActivityGateway activities;
    private final PointProductGateway products;
    private final ProductSpuGateway spus;
    private final ProductSkuGateway skus;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    private PointActivity activity(Long id) {
        var value = activities.find(id);
        require(value != null, "积分商城活动不存在");
        return value;
    }

    private List<PointProduct> validate(Long activityId, Long spuId, Integer sort, List<PointProductCmd> lines) {
        require(spus.findSpu(spuId) != null, "积分商品不存在");
        require(sort != null && sort >= 0, "排序必须为非负整数");
        require(lines != null && !lines.isEmpty(), "积分商品不能为空");
        var skuMap = skus.catalogSkus(spuId).stream().collect(Collectors.toMap(ProductSku::id, value -> value));
        var seen = new HashSet<Long>();
        var result = new ArrayList<PointProduct>();
        for (var line : lines) {
            require(line.skuId() != null && seen.add(line.skuId()), "SKU 编号重复或不合法");
            require(skuMap.containsKey(line.skuId()), "SKU 不属于该商品");
            require(line.exchangeCount() != null && line.exchangeCount() > 0, "可兑换次数必须大于零");
            require(line.point() != null && line.point() >= 0, "兑换积分不能为负");
            require(line.price() != null && line.price().signum() >= 0 && line.price().scale() <= 2,
                    "补差金额不能为负且最多两位小数");
            require(line.stock() != null && line.stock() >= 0, "积分商品库存不能为负");
            result.add(new PointProduct(null, activityId, spuId, line.skuId(), line.exchangeCount(),
                    line.point(), line.price(), line.stock(), 0));
        }
        require(result.stream().mapToInt(PointProduct::stock).sum() > 0, "积分活动库存合计必须大于零");
        var conflicts = activities.enabled().stream()
                .filter(other -> !Objects.equals(other.id(), activityId))
                .anyMatch(other -> Objects.equals(other.spuId(), spuId));
        require(!conflicts, "商品已参加其他开启的积分商城活动");
        return result;
    }

    public Long create(PointActivityCreateCmd cmd) {
        var lines = validate(null, cmd.spuId(), cmd.sort(), cmd.products());
        var stock = lines.stream().mapToInt(PointProduct::stock).sum();
        var saved = activities.save(new PointActivity(null, cmd.spuId(), 0, cmd.remark(), cmd.sort(), stock,
                stock));
        products.replace(saved.id(), lines);
        return saved.id();
    }

    public Boolean update(PointActivityUpdateCmd cmd) {
        var current = activity(cmd.id());
        require(Objects.equals(current.status(), 0), "积分活动已关闭，不能修改");
        var lines = validate(cmd.id(), cmd.spuId(), cmd.sort(), cmd.products());
        var stock = lines.stream().mapToInt(PointProduct::stock).sum();
        var totalStock = Math.max(current.totalStock(), stock);
        activities.save(new PointActivity(cmd.id(), cmd.spuId(), 0, cmd.remark(), cmd.sort(), stock,
                totalStock));
        products.replace(cmd.id(), lines);
        return true;
    }

    public Boolean close(Long id) {
        activity(id);
        activities.close(id);
        products.updateStatus(id, 1);
        return true;
    }

    public Boolean delete(Long id) {
        require(Objects.equals(activity(id).status(), 1), "请先关闭活动");
        products.deleteByActivity(id);
        activities.delete(id);
        return true;
    }

    @Transactional(readOnly = true)
    public PointActivityDTO detail(Long id) {
        return dto(activity(id));
    }

    @Transactional(readOnly = true)
    public PageResult<PointActivityDTO> page(PointActivityPageQry qry) {
        require(qry.pageNum() != null && qry.pageNum() >= 1 && qry.pageSize() != null
                && qry.pageSize() >= 1 && qry.pageSize() <= 100, "分页参数不合法");
        var page = activities.page(new PointFilter(null, qry.status(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(this::dto).toList(), page.getTotal());
    }

    @Transactional(readOnly = true)
    public PageResult<PointActivityDTO> appPage(int pageNum, int pageSize) {
        require(pageNum >= 1 && pageSize >= 1 && pageSize <= 100, "分页参数不合法");
        var all = activities.enabled();
        var start = Math.min((pageNum - 1) * pageSize, all.size());
        var end = Math.min(start + pageSize, all.size());
        return PageResult.of(all.subList(start, end).stream().map(this::dto).toList(), all.size());
    }

    @Transactional(readOnly = true)
    public PointActivityDTO appDetail(Long id) {
        var value = activity(id);
        require(Objects.equals(value.status(), 0), "积分活动已关闭");
        return dto(value);
    }

    /** 下单前校验活动、限兑数量和库存后返回所需积分与补差金额。 */
    @Transactional(readOnly = true)
    public PointJoinDTO validateJoin(Long activityId, Long skuId, Integer count) {
        require(count != null && count > 0, "兑换数量不合法");
        var activity = activity(activityId);
        require(Objects.equals(activity.status(), 0), "积分活动已关闭");
        var product = products.byActivityAndSku(activityId, skuId);
        require(product != null, "积分商品不存在");
        require(count <= product.exchangeCount(), "超过可兑换次数");
        require(count <= product.stock(), "积分商品库存不足");
        return new PointJoinDTO(activityId, product.point(), product.price(), product.exchangeCount());
    }

    public void decreaseStock(Long activityId, Long skuId, Integer count) {
        require(count != null && count > 0, "扣减数量不合法");
        require(activities.lockById(activityId) != null, "积分活动不存在");
        var product = products.byActivityAndSku(activityId, skuId);
        require(product != null && count <= product.stock(), "积分商品库存不足");
        require(products.decreaseStock(activityId, skuId, count), "积分商品库存不足");
        require(activities.decreaseStock(activityId, count), "积分活动库存不足");
    }

    public void increaseStock(Long activityId, Long skuId, Integer count) {
        if (count == null || count <= 0) {
            return;
        }
        products.increaseStock(activityId, skuId, count);
        activities.increaseStock(activityId, count);
    }

    private PointActivityDTO dto(PointActivity value) {
        var spu = spus.findSpu(value.spuId());
        var lines = products.byActivityId(value.id());
        var min = lines.stream().min(java.util.Comparator.comparing(PointProduct::point)).orElse(null);
        var market = spus.findSpus(List.of(value.spuId())).stream()
                .flatMap(item -> skus.catalogSkus(item.id()).stream())
                .map(ProductSku::price).min(BigDecimal::compareTo).orElse(null);
        return new PointActivityDTO(value.id(), value.spuId(), spu == null ? null : spu.name(),
                spu == null ? null : spu.image(), value.status(), value.remark(), value.sort(), value.stock(),
                value.totalStock(), min == null ? null : min.point(), min == null ? null : min.price(),
                market, lines.stream().map(line -> new PointProductDTO(line.id(), line.skuId(), line.spuId(),
                        line.exchangeCount(), line.point(), line.price(), line.stock())).toList());
    }
}
