package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.cmd.SeckillActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.SeckillActivityUpdateCmd;
import com.lemnt.mall.application.promotion.cmd.SeckillProductCmd;
import com.lemnt.mall.application.promotion.dto.SeckillActivityDTO;
import com.lemnt.mall.application.promotion.dto.SeckillProductDTO;
import com.lemnt.mall.application.promotion.qry.SeckillActivityPageQry;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.promotion.gateway.SeckillActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.SeckillConfigGateway;
import com.lemnt.mall.domain.promotion.gateway.SeckillProductGateway;
import com.lemnt.mall.domain.promotion.model.SeckillActivity;
import com.lemnt.mall.domain.promotion.model.SeckillFilter;
import com.lemnt.mall.domain.promotion.model.SeckillProduct;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** 秒杀活动直接管理，负责商品校验、时段冲突和库存合计。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SeckillActivityManagementService {
    private final SeckillActivityGateway activities;
    private final SeckillConfigGateway configs;
    private final SeckillProductGateway products;
    private final ProductSpuGateway spus;
    private final ProductSkuGateway skus;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    private SeckillActivity activity(Long id) {
        var value = activities.find(id);
        require(value != null, "秒杀活动不存在");
        return value;
    }

    /** 商品必须属于活动 SPU，价格低于原价，库存不能为负。 */
    private List<SeckillProduct> validate(Long activityId, Long spuId, List<Long> configIds,
            List<SeckillProductCmd> lines, LocalDateTime start, LocalDateTime end) {
        require(spus.findSpu(spuId) != null, "秒杀商品不存在");
        require(configIds != null && !configIds.isEmpty(), "秒杀时段不能为空");
        var distinct = new HashSet<>(configIds);
        require(distinct.size() == configIds.size(), "秒杀时段不能重复");
        var found = configs.byIds(configIds);
        require(found.size() == distinct.size(), "秒杀时段不存在");
        require(found.stream().allMatch(config -> Objects.equals(config.status(), 0)), "秒杀时段已关闭");

        require(lines != null && !lines.isEmpty(), "秒杀商品不能为空");
        var skuMap = skus.catalogSkus(spuId).stream().collect(java.util.stream.Collectors.toMap(
                ProductSku::id, sku -> sku));
        var seen = new HashSet<Long>();
        var result = new java.util.ArrayList<SeckillProduct>();
        for (var line : lines) {
            require(line.skuId() != null && seen.add(line.skuId()), "SKU 编号重复或不合法");
            var sku = skuMap.get(line.skuId());
            require(sku != null, "SKU 不属于该商品");
            require(line.seckillPrice() != null && line.seckillPrice().signum() > 0
                    && line.seckillPrice().scale() <= 2
                    && line.seckillPrice().compareTo(sku.price()) < 0, "秒杀价必须低于原价");
            require(line.stock() != null && line.stock() >= 0, "秒杀库存不合法");
            result.add(new SeckillProduct(null, activityId, configIds, spuId, line.skuId(),
                    line.seckillPrice(), line.stock(), 0, start, end));
        }
        require(result.stream().mapToInt(SeckillProduct::stock).sum() > 0, "秒杀库存合计必须大于零");

        var conflicts = activities.enabled().stream()
                .filter(other -> !Objects.equals(other.id(), activityId))
                .filter(other -> Objects.equals(other.spuId(), spuId))
                .anyMatch(other -> other.configIds().stream().anyMatch(distinct::contains));
        require(!conflicts, "商品在同一时段已参加其他秒杀活动");
        return result;
    }

    private void validateActivity(Long spuId, String name, String remark, LocalDateTime start,
            LocalDateTime end, Integer sort, Integer totalLimitCount, Integer singleLimitCount) {
        require(spuId != null && spuId > 0, "秒杀商品不能为空");
        require(name != null && !name.isBlank() && name.length() <= 100, "活动名称不合法");
        require(remark == null || remark.length() <= 500, "备注过长");
        require(start != null && end != null && end.isAfter(start), "活动结束时间必须晚于开始时间");
        require(sort != null, "排序不能为空");
        require(totalLimitCount != null && totalLimitCount > 0, "总限购数量必须大于零");
        require(singleLimitCount != null && singleLimitCount > 0, "单次限购数量必须大于零");
    }

    private static int sum(List<SeckillProduct> values) {
        return values.stream().mapToInt(SeckillProduct::stock).sum();
    }

    public Long create(SeckillActivityCreateCmd cmd) {
        validateActivity(cmd.spuId(), cmd.name(), cmd.remark(), cmd.startTime(), cmd.endTime(), cmd.sort(),
                cmd.totalLimitCount(), cmd.singleLimitCount());
        var lines = validate(null, cmd.spuId(), cmd.configIds(), cmd.products(), cmd.startTime(), cmd.endTime());
        var stock = sum(lines);
        var saved = activities.save(new SeckillActivity(null, cmd.spuId(), cmd.name(), 0, cmd.remark(),
                cmd.startTime(), cmd.endTime(), cmd.sort(), cmd.configIds(), cmd.totalLimitCount(),
                cmd.singleLimitCount(), stock, stock));
        products.replace(saved.id(), lines.stream().map(line -> copy(line, saved.id())).toList());
        return saved.id();
    }

    public Boolean update(SeckillActivityUpdateCmd cmd) {
        var current = activity(cmd.id());
        require(Objects.equals(current.status(), 0), "活动已关闭，不能修改");
        validateActivity(cmd.spuId(), cmd.name(), cmd.remark(), cmd.startTime(), cmd.endTime(), cmd.sort(),
                cmd.totalLimitCount(), cmd.singleLimitCount());
        var lines = validate(cmd.id(), cmd.spuId(), cmd.configIds(), cmd.products(), cmd.startTime(),
                cmd.endTime());
        var stock = sum(lines);
        // 源站仅在库存增加时更新总库存，避免修改活动造成总库存回退。
        var totalStock = stock > current.totalStock() ? stock : current.totalStock();
        activities.save(new SeckillActivity(cmd.id(), cmd.spuId(), cmd.name(), 0, cmd.remark(),
                cmd.startTime(), cmd.endTime(), cmd.sort(), cmd.configIds(), cmd.totalLimitCount(),
                cmd.singleLimitCount(), stock, totalStock));
        products.replace(cmd.id(), lines.stream().map(line -> copy(line, cmd.id())).toList());
        return true;
    }

    private SeckillProduct copy(SeckillProduct value, Long activityId) {
        return new SeckillProduct(null, activityId, value.configIds(), value.spuId(), value.skuId(),
                value.seckillPrice(), value.stock(), 0, value.activityStartTime(), value.activityEndTime());
    }

    public Boolean close(Long id) {
        activity(id);
        activities.close(id);
        return true;
    }

    public Boolean delete(Long id) {
        require(Objects.equals(activity(id).status(), 1), "请先关闭活动");
        products.deleteByActivity(id);
        activities.delete(id);
        return true;
    }

    @Transactional(readOnly = true)
    public SeckillActivityDTO detail(Long id) {
        return dto(activity(id));
    }

    @Transactional(readOnly = true)
    public PageResult<SeckillActivityDTO> page(SeckillActivityPageQry qry) {
        require(qry.pageNum() != null && qry.pageNum() >= 1 && qry.pageSize() != null
                && qry.pageSize() >= 1 && qry.pageSize() <= 100, "分页参数不合法");
        var page = activities.page(new SeckillFilter(qry.name(), qry.status(), qry.configId(),
                qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(this::dto).toList(), page.getTotal());
    }

    private SeckillActivityDTO dto(SeckillActivity value) {
        var spu = spus.findSpu(value.spuId());
        var lines = products.byActivityId(value.id());
        var min = lines.stream().map(SeckillProduct::seckillPrice).min(BigDecimal::compareTo).orElse(null);
        return new SeckillActivityDTO(value.id(), value.spuId(), spu == null ? null : spu.name(),
                spu == null ? null : spu.image(), min, value.name(), value.status(), value.remark(),
                value.startTime(), value.endTime(), value.sort(), value.configIds(),
                value.totalLimitCount(), value.singleLimitCount(), value.stock(), value.totalStock(),
                lines.stream().map(line -> new SeckillProductDTO(line.id(), line.skuId(), line.spuId(),
                        line.seckillPrice(), line.stock(), line.activityStartTime(),
                        line.activityEndTime())).toList());
    }
}
