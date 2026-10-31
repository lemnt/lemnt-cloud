package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.SeckillAppActivityDTO;
import com.lemnt.mall.application.promotion.dto.SeckillAppDetailDTO;
import com.lemnt.mall.application.promotion.dto.SeckillConfigDTO;
import com.lemnt.mall.application.promotion.dto.SeckillNowDTO;
import com.lemnt.mall.application.promotion.qry.SeckillAppActivityPageQry;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.domain.promotion.gateway.SeckillActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.SeckillConfigGateway;
import com.lemnt.mall.domain.promotion.gateway.SeckillProductGateway;
import com.lemnt.mall.domain.promotion.model.SeckillActivity;
import com.lemnt.mall.domain.promotion.model.SeckillConfig;
import com.lemnt.mall.domain.promotion.model.SeckillProduct;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** App 秒杀展示，按当前时段和活动时间过滤，价格取活动商品最低价。 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeckillAppQueryService {
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

    private LocalTime parse(String value) {
        return LocalTime.parse(value.length() == 5 ? value + ":00" : value);
    }

    private boolean inTime(SeckillConfig config, LocalDateTime now) {
        var time = now.toLocalTime();
        return !time.isBefore(parse(config.startTime())) && time.isBefore(parse(config.endTime()));
    }

    private SeckillConfig currentConfig(LocalDateTime now) {
        return configs.byStatus(0).stream().filter(config -> inTime(config, now)).findFirst().orElse(null);
    }

    public List<SeckillConfigDTO> configList() {
        return configs.byStatus(0).stream().map(config -> new SeckillConfigDTO(config.id(), config.name(),
                config.startTime(), config.endTime(), config.sliderPicUrls(), config.status(), null)).toList();
    }

    public SeckillNowDTO now() {
        var now = LocalDateTime.now();
        var config = currentConfig(now);
        if (config == null) {
            return new SeckillNowDTO(null, List.of());
        }
        var configDto = new SeckillConfigDTO(config.id(), config.name(), config.startTime(), config.endTime(),
                config.sliderPicUrls(), config.status(), null);
        return new SeckillNowDTO(configDto, summaries(activities.byConfigAndNow(config.id(), now)));
    }

    public PageResult<SeckillAppActivityDTO> page(SeckillAppActivityPageQry qry) {
        require(qry.pageNum() != null && qry.pageNum() >= 1 && qry.pageSize() != null
                && qry.pageSize() >= 1 && qry.pageSize() <= 100, "分页参数不合法");
        if (qry.configId() != null) {
            require(configs.find(qry.configId()) != null, "秒杀时段不存在");
        }
        var page = activities.appPage(qry.configId(), LocalDateTime.now(), qry.pageNum(), qry.pageSize());
        return PageResult.of(summaries(page.getRecords()), page.getTotal());
    }

    public SeckillAppDetailDTO detail(Long id) {
        var activity = activities.find(id);
        require(activity != null && Objects.equals(activity.status(), 0), "秒杀活动不存在");
        var enabled = configs.byStatus(0).stream()
                .filter(config -> activity.configIds().contains(config.id())).toList();
        var now = LocalDateTime.now();
        var config = enabled.stream().filter(item -> inTime(item, now)).findFirst()
                .orElse(enabled.isEmpty() ? null : enabled.get(enabled.size() - 1));
        require(config != null, "秒杀时段不存在");
        // 活动不在有效日期时，源站以结束日期展示时段，优先展示未开始。
        var date = !now.isBefore(activity.startTime()) && now.isBefore(activity.endTime())
                ? LocalDate.now() : activity.endTime().toLocalDate();
        var start = LocalDateTime.of(date, parse(config.startTime()));
        var end = LocalDateTime.of(date, parse(config.endTime()));
        var lines = products.byActivityId(id);
        return new SeckillAppDetailDTO(activity.id(), activity.name(), activity.status(), start, end,
                activity.spuId(), activity.totalLimitCount(), activity.singleLimitCount(), activity.stock(),
                activity.totalStock(), lines.stream().map(line -> new SeckillAppDetailDTO.Product(
                        line.skuId(), line.seckillPrice(), line.stock())).toList());
    }

    private List<SeckillAppActivityDTO> summaries(List<SeckillActivity> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        var ids = rows.stream().map(SeckillActivity::id).toList();
        var spuIds = rows.stream().map(SeckillActivity::spuId).distinct().toList();
        Map<Long, List<SeckillProduct>> productMap = products.byActivityIds(ids).stream()
                .collect(Collectors.groupingBy(SeckillProduct::activityId));
        Map<Long, ProductSpu> spuMap = spus.findSpus(spuIds).stream()
                .collect(Collectors.toMap(ProductSpu::id, value -> value));
        Map<Long, List<ProductSku>> skuMap = skus.skusBySpuIds(spuIds).stream()
                .collect(Collectors.groupingBy(ProductSku::spuId));
        return rows.stream().map(row -> {
            var spu = spuMap.get(row.spuId());
            var market = skuMap.getOrDefault(row.spuId(), List.of()).stream()
                    .map(ProductSku::price).min(BigDecimal::compareTo).orElse(null);
            var seckill = productMap.getOrDefault(row.id(), List.of()).stream()
                    .map(SeckillProduct::seckillPrice).min(BigDecimal::compareTo).orElse(null);
            return new SeckillAppActivityDTO(row.id(), row.name(), row.spuId(),
                    spu == null ? null : spu.name(), spu == null ? null : spu.image(), market,
                    row.status(), row.stock(), row.totalStock(), seckill);
        }).toList();
    }
}
