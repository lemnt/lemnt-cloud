package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.dto.BargainActivityDTO;
import com.lemnt.mall.application.promotion.dto.BargainDetailDTO;
import com.lemnt.mall.application.promotion.dto.BargainHelpDTO;
import com.lemnt.mall.application.promotion.dto.BargainJoinDTO;
import com.lemnt.mall.application.promotion.dto.BargainRecordDTO;
import com.lemnt.mall.application.promotion.qry.BargainHelpPageQry;
import com.lemnt.mall.application.promotion.qry.BargainRecordPageQry;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.domain.promotion.gateway.BargainActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.BargainHelpGateway;
import com.lemnt.mall.domain.promotion.gateway.BargainRecordGateway;
import com.lemnt.mall.domain.promotion.model.BargainActivity;
import com.lemnt.mall.domain.promotion.model.BargainHelp;
import com.lemnt.mall.domain.promotion.model.BargainRecord;
import com.lemnt.mall.domain.promotion.model.BargainRecordFilter;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/** 砍价记录：发起砍价、好友助力、成功后下单及活动到期失败。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class BargainRecordService {
    private static final int IN_PROGRESS = 1;
    private static final int SUCCESS = 2;
    private static final int FAILED = 3;

    private final BargainRecordGateway records;
    private final BargainHelpGateway helps;
    private final BargainActivityGateway activities;
    private final BargainActivityManagementService management;
    private final ProductSpuGateway spus;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    private BargainRecord record(Long id) {
        var value = records.find(id);
        require(value != null, "砍价记录不存在");
        return value;
    }

    public Long create(Long userId, Long activityId) {
        require(userId != null && userId > 0, "会员编号不合法");
        var activity = management.requireJoinable(activityId, LocalDateTime.now());
        require(records.byUserAndActivity(userId, activityId).stream()
                .noneMatch(item -> Objects.equals(item.status(), IN_PROGRESS)), "您已有进行中的砍价");
        require(records.countByUserAndActivityAndStatus(userId, activityId, SUCCESS)
                < activity.totalLimitCount(), "超过砍价限购数量");
        return records.save(new BargainRecord(null, userId, activityId, activity.spuId(), activity.skuId(),
                activity.bargainFirstPrice(), activity.bargainFirstPrice(), IN_PROGRESS, null, null)).id();
    }

    /** 好友助力按随机区间砍价，砍到底价或助力人数达到上限即成功。 */
    public BargainHelpDTO help(Long userId, Long recordId) {
        require(userId != null && userId > 0, "会员编号不合法");
        var record = record(recordId);
        require(Objects.equals(record.status(), IN_PROGRESS), "该砍价已结束");
        require(!Objects.equals(record.userId(), userId), "不能给自己砍价");
        var activity = activities.find(record.activityId());
        require(activity != null, "砍价活动不存在");
        require(helps.countByUserAndActivity(userId, activity.id()) < activity.bargainCount(), "帮砍次数已用完");
        require(record.bargainPrice().compareTo(activity.bargainMinPrice()) > 0, "已砍到底价");
        require(helps.byRecordAndUser(recordId, userId) == null, "您已助力过该砍价");
        var reduce = reducePrice(activity, record.bargainPrice());
        BargainHelp help;
        try {
            help = helps.save(new BargainHelp(null, activity.id(), recordId, userId, reduce));
        } catch (DuplicateKeyException exception) {
            throw new ServiceException("您已助力过该砍价", 409);
        }
        var price = record.bargainPrice().subtract(reduce);
        var count = helps.byRecordId(recordId).size();
        var success = price.compareTo(activity.bargainMinPrice()) <= 0 || count >= activity.helpMaxCount();
        require(records.updatePrice(recordId, record.bargainPrice(), price, success ? SUCCESS : IN_PROGRESS,
                success ? LocalDateTime.now() : null), "多人同时砍价，请重试");
        return helpDto(help);
    }

    /** 在随机区间取分，最高不超过剩余可砍金额。 */
    private BigDecimal reducePrice(BargainActivity activity, BigDecimal current) {
        var minCents = activity.randomMinPrice().movePointRight(2).longValueExact();
        var maxCents = activity.randomMaxPrice().movePointRight(2).longValueExact();
        var cents = maxCents <= minCents ? minCents : ThreadLocalRandom.current().nextLong(minCents, maxCents + 1);
        var reduce = BigDecimal.valueOf(cents).movePointLeft(2);
        var remaining = current.subtract(activity.bargainMinPrice());
        var capped = reduce.min(remaining).setScale(2, RoundingMode.DOWN);
        return capped.signum() > 0 ? capped : remaining;
    }

    /** 砍价成功且未下单时允许以砍后价购买。 */
    @Transactional(readOnly = true)
    public BargainJoinDTO validateJoin(Long userId, Long recordId, Long skuId) {
        var record = record(recordId);
        require(Objects.equals(record.userId(), userId), "砍价记录不属于当前会员");
        require(Objects.equals(record.status(), SUCCESS), "砍价尚未成功");
        require(record.orderId() == null, "砍价已下单");
        var activity = management.requireJoinable(record.activityId(), LocalDateTime.now());
        require(Objects.equals(activity.skuId(), skuId), "砍价商品不匹配");
        return new BargainJoinDTO(activity.id(), activity.name(), record.bargainPrice());
    }

    public Boolean bindOrder(Long recordId, Long orderId) {
        require(orderId != null && orderId > 0, "订单编号不合法");
        require(records.bindOrder(recordId, orderId), "砍价记录不存在或已下单");
        return true;
    }

    @Transactional(readOnly = true)
    public BargainDetailDTO detail(Long recordId, Long userId) {
        var record = record(recordId);
        var activity = activities.find(record.activityId());
        require(activity != null, "砍价活动不存在");
        var helped = userId == null ? Boolean.FALSE : helps.byRecordAndUser(recordId, userId) != null;
        return new BargainDetailDTO(activityDto(activity), dto(record),
                helps.byRecordId(recordId).stream().map(this::helpDto).toList(), helped);
    }

    @Transactional(readOnly = true)
    public PageResult<BargainRecordDTO> page(BargainRecordPageQry qry) {
        require(qry.pageNum() != null && qry.pageNum() >= 1 && qry.pageSize() != null
                && qry.pageSize() >= 1 && qry.pageSize() <= 100, "分页参数不合法");
        var page = records.page(new BargainRecordFilter(qry.userId(), qry.activityId(), qry.status(),
                qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(this::dto).toList(), page.getTotal());
    }

    @Transactional(readOnly = true)
    public PageResult<BargainHelpDTO> helpPage(BargainHelpPageQry qry) {
        require(qry.pageNum() != null && qry.pageNum() >= 1 && qry.pageSize() != null
                && qry.pageSize() >= 1 && qry.pageSize() <= 100, "分页参数不合法");
        var page = helps.page(qry.recordId(), qry.userId(), qry.activityId(), qry.pageNum(), qry.pageSize());
        return PageResult.of(page.getRecords().stream().map(this::helpDto).toList(), page.getTotal());
    }

    /** 活动已结束且仍进行中的记录统一失败。 */
    public int expire() {
        var now = LocalDateTime.now();
        var handled = 0;
        for (var activity : activities.all()) {
            if (activity.endTime().isAfter(now)) {
                continue;
            }
            for (var record : records.byActivityAndStatus(activity.id(), IN_PROGRESS)) {
                if (records.updatePrice(record.id(), record.bargainPrice(), record.bargainPrice(), FAILED, now)) {
                    handled++;
                }
            }
        }
        return handled;
    }

    @Transactional(readOnly = true)
    public List<BargainRecordDTO> latest(Long activityId, Integer status, int count) {
        require(count >= 1 && count <= 20, "数量不合法");
        return records.latest(activityId, status, count).stream().map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public List<BargainHelpDTO> helpList(Long recordId) {
        return helps.byRecordId(recordId).stream().map(this::helpDto).toList();
    }

    private BargainActivityDTO activityDto(BargainActivity value) {
        var spu = spus.findSpu(value.spuId());
        return new BargainActivityDTO(value.id(), value.name(), value.startTime(), value.endTime(),
                value.status(), value.spuId(), spu == null ? null : spu.name(),
                spu == null ? null : spu.image(), null, value.skuId(), value.bargainFirstPrice(),
                value.bargainMinPrice(), value.stock(), value.totalStock(), value.helpMaxCount(),
                value.bargainCount(), value.totalLimitCount(), value.randomMinPrice(),
                value.randomMaxPrice(), records.userCount(value.id(), SUCCESS));
    }

    private BargainRecordDTO dto(BargainRecord value) {
        ProductSpu spu = spus.findSpu(value.spuId());
        return new BargainRecordDTO(value.id(), value.userId(), value.activityId(), value.spuId(),
                spu == null ? null : spu.name(), spu == null ? null : spu.image(), value.skuId(),
                value.bargainFirstPrice(), value.bargainPrice(), value.status(), value.endTime(),
                value.orderId());
    }

    private BargainHelpDTO helpDto(BargainHelp value) {
        return new BargainHelpDTO(value.id(), value.activityId(), value.recordId(), value.userId(),
                value.reducePrice());
    }
}
