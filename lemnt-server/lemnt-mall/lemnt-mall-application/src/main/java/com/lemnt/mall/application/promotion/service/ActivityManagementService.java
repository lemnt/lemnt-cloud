package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.cmd.DiscountActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.DiscountActivityUpdateCmd;
import com.lemnt.mall.application.promotion.cmd.RewardActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.RewardActivityUpdateCmd;
import com.lemnt.mall.application.promotion.dto.DiscountActivityDTO;
import com.lemnt.mall.application.promotion.dto.RewardActivityDTO;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.promotion.gateway.DiscountActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.DiscountProductGateway;
import com.lemnt.mall.domain.promotion.gateway.RewardActivityGateway;
import com.lemnt.mall.domain.promotion.model.ActivityFilter;
import com.lemnt.mall.domain.promotion.model.DiscountActivity;
import com.lemnt.mall.domain.promotion.model.DiscountProduct;
import com.lemnt.mall.domain.promotion.model.RewardActivity;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** 按芋道直接管理活动及 SKU 折扣，不引入草稿版本和额度账本。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class ActivityManagementService {
    private final RewardActivityGateway rewards;
    private final DiscountActivityGateway discounts;
    private final DiscountProductGateway products;
    private final ProductSkuGateway skus;
    private final ProductSpuGateway spus;

    /** 活动起止时间及名称是两类活动共同的业务约束。 */
    private void validate(String name, LocalDateTime start, LocalDateTime end) {
        require(name != null && !name.isBlank() && name.length() <= 100, "活动名称不合法");
        require(start != null && end != null && end.isAfter(start), "活动结束时间必须晚于开始时间");
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    /** 关闭后的活动不再修改，也不提供重新开启入口。 */
    private void editable(Integer status) {
        require(status != null && status == 0, "活动已关闭，不能修改");
    }

    private RewardActivity reward(Long id) {
        var value = rewards.find(id);
        require(value != null, "满减活动不存在");
        return value;
    }

    private DiscountActivity discount(Long id) {
        var value = discounts.find(id);
        require(value != null, "折扣活动不存在");
        return value;
    }

    /** 校验门槛顺序及减免额，满金额与满件数采用同一阶梯列表。 */
    private void validateReward(RewardActivity value) {
        validate(value.name(), value.startTime(), value.endTime());
        require(value.conditionType() != null && Set.of("PRICE", "COUNT").contains(value.conditionType()), "满减条件类型不合法");
        require(value.productScope() != null && Set.of("ALL", "SPU_IDS",
            "CATEGORY_IDS").contains(value.productScope()), "商品范围不合法");
        require(value.productScopeValues() != null, "商品范围不能为空");
        if (!"ALL".equals(value.productScope())) {
            require(!value.productScopeValues().isEmpty() && value.productScopeValues().stream()
                    .allMatch(id -> id != null && id > 0), "请指定商品范围");
        }
        if ("SPU_IDS".equals(value.productScope())) {
            require(spus.existingSpuIds(value.productScopeValues()).size() == value.productScopeValues().size(),
                    "活动商品不存在");
        }
        require(value.rules() != null && !value.rules().isEmpty(), "满减规则不能为空");
        BigDecimal previous = BigDecimal.ZERO;
        for (var rule : value.rules()) {
            require(rule.limit() != null && rule.limit().compareTo(previous) > 0
                    && rule.discountPrice() != null && rule.discountPrice().signum() > 0
                    && rule.discountPrice().scale() <= 2, "满减规则门槛必须递增且减免金额合法");
            if ("COUNT".equals(value.conditionType())) {
                require(rule.limit().stripTrailingZeros().scale() <= 0, "满件数门槛必须为整数");
            } else {
                require(rule.limit().scale() <= 2 && rule.discountPrice().compareTo(rule.limit()) <= 0,
                        "满减金额不能超过门槛");
            }
            previous = rule.limit();
        }
        for (var other : rewards.enabled()) {
            if (Objects.equals(other.id(), value.id()) || !value.startTime().isBefore(other.endTime())
                    || !other.startTime().isBefore(value.endTime())) { continue; }
            require(!scopeOverlaps(other, value), "同一时段的满减商品范围不能重叠");
        }
    }

    /** 分类与商品交叉范围按商品所属分类判断，全部商品范围覆盖任何范围。 */
    private boolean scopeOverlaps(RewardActivity left, RewardActivity right) {
        if ("ALL".equals(left.productScope()) || "ALL".equals(right.productScope())) {
            return true;
        }
        if (left.productScope().equals(right.productScope())) {
            return left.productScopeValues().stream().anyMatch(right.productScopeValues()::contains);
        }
        var selected = "SPU_IDS".equals(left.productScope()) ? left : right;
        var categories = selected == left ? right : left;
        return spus.findSpus(selected.productScopeValues()).stream()
                .anyMatch(spu -> categories.productScopeValues().contains(spu.categoryId()));
    }

    /** SKU 锁按编号排序，避免并发创建两个有效折扣活动使用同一 SKU。 */
    private void validateProducts(Long activityId, List<DiscountProduct> values) {
        require(values != null && !values.isEmpty(), "折扣商品不能为空");
        var ids = values.stream().map(DiscountProduct::skuId).toList();
        require(ids.stream().allMatch(id -> id != null && id > 0)
                && ids.stream().distinct().count() == ids.size(), "SKU 编号重复或不合法");
        ids.stream().sorted().forEach(skus::lockById);
        for (var value : values) {
            var sku = skus.catalogSku(value.skuId());
            require(sku != null && Objects.equals(sku.spuId(), value.spuId()), "SKU 与商品不匹配");
            require(value.discountType() != null && Set.of("PRICE", "PERCENT").contains(value.discountType()),
                "折扣类型不合法");
            if ("PRICE".equals(value.discountType())) {
                require(value.discountPrice() != null && value.discountPrice().signum() > 0
                        && value.discountPrice().scale() <= 2
                        && value.discountPrice().compareTo(sku.price()) < 0, "减价金额不合法");
            } else {
                require(value.discountPercent() != null && value.discountPercent() >= 1
                        && value.discountPercent() <= 9999, "折扣比例必须为 1 至 9999 基点");
            }
        }
        var others = discounts.enabled().stream().filter(a -> !Objects.equals(a.id(), activityId))
                .map(DiscountActivity::id).toList();
        require(products.byActivities(others).stream().noneMatch(p -> ids.contains(p.skuId())),
                "商品已参加其他开启的折扣活动");
    }

    /** 创建满减活动。 */
    public Long createReward(RewardActivityCreateCmd cmd) {
        var value = new RewardActivity(null, cmd.name(), 0, cmd.startTime(), cmd.endTime(), cmd.remark(),
            cmd.conditionType(), cmd.productScope(), cmd.productScopeValues(), cmd.rules());
        validateReward(value);
        var saved = rewards.save(value);
        return saved.id();
    }

    /** 修改满减活动。 */
    public Boolean updateReward(RewardActivityUpdateCmd cmd) {
        editable(reward(cmd.id()).status());
        var value = new RewardActivity(cmd.id(), cmd.name(), 0, cmd.startTime(), cmd.endTime(), cmd.remark(),
            cmd.conditionType(), cmd.productScope(), cmd.productScopeValues(), cmd.rules());
        validateReward(value);
        var saved = rewards.save(value);
        return true;
    }

    /** 关闭活动，重复关闭保持原结果。 */
    public Boolean closeReward(Long id) {
        var value = reward(id);
        rewards.save(new RewardActivity(value.id(), value.name(), 1, value.startTime(), value.endTime(),
            value.remark(), value.conditionType(), value.productScope(), value.productScopeValues(), value.rules()));
        return true;
    }

    /** 仅删除已关闭活动。 */
    public Boolean deleteReward(Long id) {
        require(reward(id).status() == 1, "请先关闭活动");
        
        rewards.delete(id);
        return true;
    }

    @Transactional(readOnly = true)
    public RewardActivityDTO detailReward(Long id) {
        var value = reward(id);
        return rewardDto(value);
    }

    @Transactional(readOnly = true)
    public PageResult<RewardActivityDTO> pageReward(ActivityFilter filter) {
        require(filter.pageNum() >= 1 && filter.pageSize() >= 1 && filter.pageSize() <= 100, "分页参数不合法");
        var page = rewards.page(filter);
        return PageResult.of(page.getRecords().stream().map(this::rewardDto).toList(), page.getTotal());
    }

    private RewardActivityDTO rewardDto(RewardActivity value) {
        return new RewardActivityDTO(value.id(), value.name(), value.status(), value.startTime(), value.endTime(),
            value.remark(), value.conditionType(), value.productScope(), value.productScopeValues(), value.rules());
    }

    /** 创建折扣活动。 */
    public Long createDiscount(DiscountActivityCreateCmd cmd) {
        var value = new DiscountActivity(null, cmd.name(), 0, cmd.startTime(), cmd.endTime(), cmd.remark());
        validate(cmd.name(), cmd.startTime(), cmd.endTime());
        validateProducts(null, cmd.products());
        var saved = discounts.save(value);
        products.replace(saved.id(), cmd.products());
        return saved.id();
    }

    /** 修改折扣活动。 */
    public Boolean updateDiscount(DiscountActivityUpdateCmd cmd) {
        editable(discount(cmd.id()).status());
        var value = new DiscountActivity(cmd.id(), cmd.name(), 0, cmd.startTime(), cmd.endTime(), cmd.remark());
        validate(cmd.name(), cmd.startTime(), cmd.endTime());
        validateProducts(cmd.id(), cmd.products());
        var saved = discounts.save(value);
        products.replace(saved.id(), cmd.products());
        return true;
    }

    /** 关闭活动，重复关闭保持原结果。 */
    public Boolean closeDiscount(Long id) {
        var value = discount(id);
        discounts.save(new DiscountActivity(value.id(), value.name(), 1, value.startTime(), value.endTime(),
            value.remark()));
        return true;
    }

    /** 仅删除已关闭活动。 */
    public Boolean deleteDiscount(Long id) {
        require(discount(id).status() == 1, "请先关闭活动");
        products.deleteByActivity(id);
        discounts.delete(id);
        return true;
    }

    @Transactional(readOnly = true)
    public DiscountActivityDTO detailDiscount(Long id) {
        var value = discount(id);
        return discountDto(value);
    }

    @Transactional(readOnly = true)
    public PageResult<DiscountActivityDTO> pageDiscount(ActivityFilter filter) {
        require(filter.pageNum() >= 1 && filter.pageSize() >= 1 && filter.pageSize() <= 100, "分页参数不合法");
        var page = discounts.page(filter);
        return PageResult.of(page.getRecords().stream().map(this::discountDto).toList(), page.getTotal());
    }

    private DiscountActivityDTO discountDto(DiscountActivity value) {
        return new DiscountActivityDTO(value.id(), value.name(), value.status(), value.startTime(), value.endTime(),
            value.remark(), products.byActivities(List.of(value.id())));
    }
}
