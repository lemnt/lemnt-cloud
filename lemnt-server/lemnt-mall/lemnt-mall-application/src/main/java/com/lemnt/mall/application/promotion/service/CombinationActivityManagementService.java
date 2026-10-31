package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.cmd.CombinationActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.CombinationActivityUpdateCmd;
import com.lemnt.mall.application.promotion.cmd.CombinationProductCmd;
import com.lemnt.mall.application.promotion.dto.CombinationActivityDTO;
import com.lemnt.mall.application.promotion.dto.CombinationAppActivityDTO;
import com.lemnt.mall.application.promotion.dto.CombinationProductDTO;
import com.lemnt.mall.application.promotion.qry.CombinationActivityPageQry;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.domain.promotion.gateway.CombinationActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.CombinationProductGateway;
import com.lemnt.mall.domain.promotion.gateway.CombinationRecordGateway;
import com.lemnt.mall.domain.promotion.model.CombinationActivity;
import com.lemnt.mall.domain.promotion.model.CombinationFilter;
import com.lemnt.mall.domain.promotion.model.CombinationProduct;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** 拼团活动直接管理，价格低于原价、同商品只能参加一个开启活动。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CombinationActivityManagementService {
    private final CombinationActivityGateway activities;
    private final CombinationProductGateway products;
    private final CombinationRecordGateway records;
    private final ProductSpuGateway spus;
    private final ProductSkuGateway skus;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    private CombinationActivity activity(Long id) {
        var value = activities.find(id);
        require(value != null, "拼团活动不存在");
        return value;
    }

    private void validateActivity(Long spuId, String name, Integer totalLimitCount, Integer singleLimitCount,
            LocalDateTime start, LocalDateTime end, Integer userSize, Boolean virtualGroup,
            Integer limitDuration) {
        require(spuId != null && spus.findSpu(spuId) != null, "拼团商品不存在");
        require(name != null && !name.isBlank() && name.length() <= 100, "活动名称不合法");
        require(totalLimitCount != null && totalLimitCount > 0, "总限购数量必须大于零");
        require(singleLimitCount != null && singleLimitCount > 0, "单次限购数量必须大于零");
        require(start != null && end != null && end.isAfter(start), "活动结束时间必须晚于开始时间");
        require(userSize != null && userSize >= 2, "拼团人数不能少于两人");
        require(virtualGroup != null, "虚拟成团配置不能为空");
        require(limitDuration != null && limitDuration > 0, "限制时长必须大于零");
    }

    /** 商品必须属于活动 SPU，拼团价低于原价，并且同一商品不能同时参加多个开启活动。 */
    private List<CombinationProduct> validateProducts(Long activityId, Long spuId, List<CombinationProductCmd> lines,
            LocalDateTime start, LocalDateTime end) {
        require(lines != null && !lines.isEmpty(), "拼团商品不能为空");
        var skuMap = skus.catalogSkus(spuId).stream().collect(Collectors.toMap(ProductSku::id, sku -> sku));
        var seen = new HashSet<Long>();
        var result = new ArrayList<CombinationProduct>();
        for (var line : lines) {
            require(line.skuId() != null && seen.add(line.skuId()), "SKU 编号重复或不合法");
            var sku = skuMap.get(line.skuId());
            require(sku != null, "SKU 不属于该商品");
            require(line.combinationPrice() != null && line.combinationPrice().signum() > 0
                    && line.combinationPrice().scale() <= 2
                    && line.combinationPrice().compareTo(sku.price()) < 0, "拼团价必须低于原价");
            result.add(new CombinationProduct(null, activityId, spuId, line.skuId(), line.combinationPrice(), 0,
                    start, end));
        }
        var conflicts = activities.enabled().stream()
                .filter(other -> !Objects.equals(other.id(), activityId))
                .anyMatch(other -> Objects.equals(other.spuId(), spuId));
        require(!conflicts, "商品已参加其他开启的拼团活动");
        return result;
    }

    public Long create(CombinationActivityCreateCmd cmd) {
        validateActivity(cmd.spuId(), cmd.name(), cmd.totalLimitCount(), cmd.singleLimitCount(),
                cmd.startTime(), cmd.endTime(), cmd.userSize(), cmd.virtualGroup(), cmd.limitDuration());
        var lines = validateProducts(null, cmd.spuId(), cmd.products(), cmd.startTime(), cmd.endTime());
        var saved = activities.save(new CombinationActivity(null, cmd.name(), cmd.spuId(), cmd.totalLimitCount(),
                cmd.singleLimitCount(), cmd.startTime(), cmd.endTime(), cmd.userSize(), cmd.virtualGroup(), 0,
                cmd.limitDuration()));
        products.replace(saved.id(), lines);
        return saved.id();
    }

    public Boolean update(CombinationActivityUpdateCmd cmd) {
        var current = activity(cmd.id());
        require(Objects.equals(current.status(), 0), "拼团活动已关闭，不能修改");
        validateActivity(cmd.spuId(), cmd.name(), cmd.totalLimitCount(), cmd.singleLimitCount(),
                cmd.startTime(), cmd.endTime(), cmd.userSize(), cmd.virtualGroup(), cmd.limitDuration());
        var lines = validateProducts(cmd.id(), cmd.spuId(), cmd.products(), cmd.startTime(), cmd.endTime());
        activities.save(new CombinationActivity(cmd.id(), cmd.name(), cmd.spuId(), cmd.totalLimitCount(),
                cmd.singleLimitCount(), cmd.startTime(), cmd.endTime(), cmd.userSize(), cmd.virtualGroup(), 0,
                cmd.limitDuration()));
        products.replace(cmd.id(), lines);
        return true;
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
    public CombinationActivityDTO detail(Long id) {
        var value = activity(id);
        return dto(value);
    }

    @Transactional(readOnly = true)
    public PageResult<CombinationActivityDTO> page(CombinationActivityPageQry qry) {
        require(qry.pageNum() != null && qry.pageNum() >= 1 && qry.pageSize() != null
                && qry.pageSize() >= 1 && qry.pageSize() <= 100, "分页参数不合法");
        var page = activities.page(new CombinationFilter(qry.name(), qry.status(), qry.spuId(),
                qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(this::dto).toList(), page.getTotal());
    }

    /** App 活动列表，仅展示开启活动；每项取最低拼团价。 */
    @Transactional(readOnly = true)
    public PageResult<CombinationAppActivityDTO> appPage(int pageNum, int pageSize) {
        require(pageNum >= 1 && pageSize >= 1 && pageSize <= 100, "分页参数不合法");
        var all = activities.enabled();
        var start = Math.min((pageNum - 1) * pageSize, all.size());
        var end = Math.min(start + pageSize, all.size());
        var slice = all.subList(start, end);
        var productMap = products.byActivityIds(slice.stream().map(CombinationActivity::id).toList()).stream()
                .collect(Collectors.groupingBy(CombinationProduct::activityId));
        var spuMap = spus.findSpus(slice.stream().map(CombinationActivity::spuId).distinct().toList()).stream()
                .collect(Collectors.toMap(ProductSpu::id, value -> value));
        var skuMap = skus.skusBySpuIds(slice.stream().map(CombinationActivity::spuId).distinct().toList()).stream()
                .collect(Collectors.groupingBy(ProductSku::spuId));
        var records = slice.stream().map(value -> {
            var spu = spuMap.get(value.spuId());
            var min = productMap.getOrDefault(value.id(), List.of()).stream()
                    .map(CombinationProduct::combinationPrice).min(BigDecimal::compareTo).orElse(null);
            var market = skuMap.getOrDefault(value.spuId(), List.of()).stream()
                    .map(ProductSku::price).min(BigDecimal::compareTo).orElse(null);
            return new CombinationAppActivityDTO(value.id(), value.name(), value.spuId(),
                    spu == null ? null : spu.name(), spu == null ? null : spu.image(), market, min,
                    value.status(), value.startTime(), value.endTime(), value.userSize());
        }).toList();
        return PageResult.of(records, all.size());
    }

    @Transactional(readOnly = true)
    public CombinationActivityDTO appDetail(Long id) {
        var value = activity(id);
        require(Objects.equals(value.status(), 0), "拼团活动已关闭");
        return dto(value);
    }

    @Transactional(readOnly = true)
    public long successCount(Long activityId) {
        return records.successCount(activityId);
    }

    private CombinationActivityDTO dto(CombinationActivity value) {
        var spu = spus.findSpu(value.spuId());
        var lines = products.byActivityId(value.id());
        var min = lines.stream().map(CombinationProduct::combinationPrice).min(BigDecimal::compareTo).orElse(null);
        return new CombinationActivityDTO(value.id(), value.name(), value.spuId(),
                spu == null ? null : spu.name(), spu == null ? null : spu.image(), min,
                value.totalLimitCount(), value.singleLimitCount(), value.startTime(), value.endTime(),
                value.userSize(), value.virtualGroup(), value.status(), value.limitDuration(),
                lines.stream().map(line -> new CombinationProductDTO(line.skuId(), line.combinationPrice()))
                        .toList());
    }

    /** 活动商品映射，供记录创建时读取拼团价。 */
    @Transactional(readOnly = true)
    public Map<Long, CombinationProduct> productMap(Long activityId) {
        return products.byActivityId(activityId).stream()
                .collect(Collectors.toMap(CombinationProduct::skuId, value -> value));
    }
}
