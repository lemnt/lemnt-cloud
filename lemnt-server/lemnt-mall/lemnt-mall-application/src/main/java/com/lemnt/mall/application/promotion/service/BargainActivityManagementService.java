package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.cmd.BargainActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.BargainActivityUpdateCmd;
import com.lemnt.mall.application.promotion.dto.BargainActivityDTO;
import com.lemnt.mall.application.promotion.qry.BargainActivityPageQry;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.domain.promotion.gateway.BargainActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.BargainRecordGateway;
import com.lemnt.mall.domain.promotion.model.BargainActivity;
import com.lemnt.mall.domain.promotion.model.BargainFilter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/** 砍价活动直接管理，单一 SKU 且同一商品只能参加一个开启活动。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class BargainActivityManagementService {
    private final BargainActivityGateway activities;
    private final BargainRecordGateway records;
    private final ProductSpuGateway spus;
    private final ProductSkuGateway skus;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    private BargainActivity activity(Long id) {
        var value = activities.find(id);
        require(value != null, "砍价活动不存在");
        return value;
    }

    /** 校验商品、时间、价格区间、随机金额和限购人数。 */
    private void validate(Long activityId, String name, LocalDateTime start, LocalDateTime end, Long spuId,
            Long skuId, BigDecimal firstPrice, BigDecimal minPrice, Integer stock, Integer helpMaxCount,
            Integer bargainCount, Integer totalLimitCount, BigDecimal randomMin, BigDecimal randomMax) {
        var spu = spus.findSpu(spuId);
        require(spu != null, "砍价商品不存在");
        var sku = skus.catalogSku(skuId);
        require(sku != null && Objects.equals(sku.spuId(), spuId), "SKU 不属于该商品");
        require(name != null && !name.isBlank() && name.length() <= 100, "活动名称不合法");
        require(start != null && end != null && end.isAfter(start), "活动结束时间必须晚于开始时间");
        require(firstPrice != null && firstPrice.signum() > 0 && firstPrice.scale() <= 2
                && firstPrice.compareTo(sku.price()) <= 0, "砍价起始价必须大于零且不高于原价");
        require(minPrice != null && minPrice.signum() >= 0 && minPrice.scale() <= 2
                && minPrice.compareTo(firstPrice) < 0, "砍价底价必须低于起始价");
        require(stock != null && stock >= 0, "砍价库存不合法");
        require(helpMaxCount != null && helpMaxCount > 0, "砍价人数必须大于零");
        require(bargainCount != null && bargainCount > 0, "帮砍次数必须大于零");
        require(totalLimitCount != null && totalLimitCount > 0, "总限购数量必须大于零");
        require(randomMin != null && randomMin.signum() > 0 && randomMin.scale() <= 2
                && randomMax != null && randomMax.compareTo(randomMin) >= 0 && randomMax.scale() <= 2
                && randomMax.compareTo(firstPrice) <= 0, "随机砍价金额区间不合法");
        var conflicts = activities.enabled().stream()
                .filter(other -> !Objects.equals(other.id(), activityId))
                .anyMatch(other -> Objects.equals(other.spuId(), spuId));
        require(!conflicts, "商品已参加其他开启的砍价活动");
    }

    public Long create(BargainActivityCreateCmd cmd) {
        validate(null, cmd.name(), cmd.startTime(), cmd.endTime(), cmd.spuId(), cmd.skuId(),
                cmd.bargainFirstPrice(), cmd.bargainMinPrice(), cmd.stock(), cmd.helpMaxCount(),
                cmd.bargainCount(), cmd.totalLimitCount(), cmd.randomMinPrice(), cmd.randomMaxPrice());
        return activities.save(new BargainActivity(null, cmd.name(), cmd.startTime(), cmd.endTime(), 0,
                cmd.spuId(), cmd.skuId(), cmd.bargainFirstPrice(), cmd.bargainMinPrice(), cmd.stock(),
                cmd.stock(), cmd.helpMaxCount(), cmd.bargainCount(), cmd.totalLimitCount(),
                cmd.randomMinPrice(), cmd.randomMaxPrice())).id();
    }

    public Boolean update(BargainActivityUpdateCmd cmd) {
        var current = activity(cmd.id());
        require(Objects.equals(current.status(), 0), "砍价活动已关闭，不能修改");
        validate(cmd.id(), cmd.name(), cmd.startTime(), cmd.endTime(), cmd.spuId(), cmd.skuId(),
                cmd.bargainFirstPrice(), cmd.bargainMinPrice(), cmd.stock(), cmd.helpMaxCount(),
                cmd.bargainCount(), cmd.totalLimitCount(), cmd.randomMinPrice(), cmd.randomMaxPrice());
        var totalStock = Math.max(current.totalStock(), cmd.stock());
        activities.save(new BargainActivity(cmd.id(), cmd.name(), cmd.startTime(), cmd.endTime(), 0,
                cmd.spuId(), cmd.skuId(), cmd.bargainFirstPrice(), cmd.bargainMinPrice(), cmd.stock(),
                totalStock, cmd.helpMaxCount(), cmd.bargainCount(), cmd.totalLimitCount(),
                cmd.randomMinPrice(), cmd.randomMaxPrice()));
        return true;
    }

    public Boolean close(Long id) {
        activity(id);
        activities.close(id);
        return true;
    }

    public Boolean delete(Long id) {
        require(Objects.equals(activity(id).status(), 1), "请先关闭活动");
        activities.delete(id);
        return true;
    }

    public void decreaseStock(Long id, int count) {
        require(count > 0, "扣减数量不合法");
        require(activity(id) != null && activities.decreaseStock(id, count), "砍价库存不足");
    }

    public void increaseStock(Long id, int count) {
        if (count > 0) {
            activities.increaseStock(id, count);
        }
    }

    @Transactional(readOnly = true)
    public BargainActivityDTO detail(Long id) {
        return dto(activity(id));
    }

    @Transactional(readOnly = true)
    public PageResult<BargainActivityDTO> page(BargainActivityPageQry qry) {
        require(qry.pageNum() != null && qry.pageNum() >= 1 && qry.pageSize() != null
                && qry.pageSize() >= 1 && qry.pageSize() <= 100, "分页参数不合法");
        var page = activities.page(new BargainFilter(qry.name(), qry.status(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(this::dto).toList(), page.getTotal());
    }

    @Transactional(readOnly = true)
    public PageResult<BargainActivityDTO> appPage(int pageNum, int pageSize) {
        require(pageNum >= 1 && pageSize >= 1 && pageSize <= 100, "分页参数不合法");
        var all = activities.enabled();
        var start = Math.min((pageNum - 1) * pageSize, all.size());
        var end = Math.min(start + pageSize, all.size());
        return PageResult.of(all.subList(start, end).stream().map(this::dto).toList(), all.size());
    }

    @Transactional(readOnly = true)
    public BargainActivityDTO appDetail(Long id) {
        var value = activity(id);
        require(Objects.equals(value.status(), 0), "砍价活动已关闭");
        return dto(value);
    }

    private BargainActivityDTO dto(BargainActivity value) {
        var spu = spus.findSpu(value.spuId());
        var sku = skus.catalogSku(value.skuId());
        return new BargainActivityDTO(value.id(), value.name(), value.startTime(), value.endTime(),
                value.status(), value.spuId(), spu == null ? null : spu.name(),
                spu == null ? null : spu.image(), sku == null ? null : sku.price(), value.skuId(),
                value.bargainFirstPrice(), value.bargainMinPrice(), value.stock(), value.totalStock(),
                value.helpMaxCount(), value.bargainCount(), value.totalLimitCount(),
                value.randomMinPrice(), value.randomMaxPrice(),
                records.userCount(value.id(), 2));
    }

    /** 记录服务需要的活动与商品快照。 */
    @Transactional(readOnly = true)
    public BargainActivity requireJoinable(Long id, LocalDateTime now) {
        var value = activity(id);
        require(Objects.equals(value.status(), 0), "砍价活动已关闭");
        require(!now.isBefore(value.startTime()) && now.isBefore(value.endTime()), "砍价活动不在进行时间");
        require(value.stock() > 0, "砍价库存不足");
        return value;
    }

    @Transactional(readOnly = true)
    public ProductSpu spu(Long spuId) {
        return spus.findSpu(spuId);
    }
}
