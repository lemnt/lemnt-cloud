package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.member.service.MemberValidationService;
import com.lemnt.mall.application.promotion.cmd.CombinationRecordCreateCmd;
import com.lemnt.mall.application.promotion.dto.CombinationRecordDTO;
import com.lemnt.mall.application.promotion.dto.CombinationRecordDetailDTO;
import com.lemnt.mall.application.promotion.dto.CombinationSummaryDTO;
import com.lemnt.mall.application.promotion.qry.CombinationRecordPageQry;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.promotion.gateway.CombinationActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.CombinationProductGateway;
import com.lemnt.mall.domain.promotion.gateway.CombinationRecordGateway;
import com.lemnt.mall.domain.promotion.model.CombinationActivity;
import com.lemnt.mall.domain.promotion.model.CombinationProduct;
import com.lemnt.mall.domain.promotion.model.CombinationRecord;
import com.lemnt.mall.domain.promotion.model.CombinationRecordFilter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** 拼团记录：参团校验、开团/参团、过期失败或虚拟成团。 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class CombinationRecordService {
    private final CombinationRecordGateway records;
    private final CombinationActivityGateway activities;
    private final CombinationProductGateway products;
    private final ProductSpuGateway spus;
    private final ProductSkuGateway skus;
    private final MemberValidationService members;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    private CombinationRecord record(Long id) {
        var value = records.find(id);
        require(value != null, "拼团记录不存在");
        return value;
    }

    /** 校验活动、父团、商品库存及本人限购后可加入。 */
    @Transactional(readOnly = true)
    public CombinationProduct validateJoin(Long userId, Long activityId, Long headId, Long skuId, Integer count) {
        require(userId != null && userId > 0, "会员编号不合法");
        require(count != null && count > 0, "购买数量不合法");
        var activity = activities.find(activityId);
        require(activity != null, "拼团活动不存在");
        require(Objects.equals(activity.status(), 0), "拼团活动已关闭");
        var now = LocalDateTime.now();
        require(!now.isBefore(activity.startTime()), "拼团活动尚未开始");
        require(count <= activity.singleLimitCount(), "超过单次限购数量");
        if (headId != null && headId != CombinationRecord.HEAD_ID_GROUP) {
            var head = records.inProgressHead(headId);
            require(head != null, "拼团不存在或已结束");
            require(head.userCount() < head.userSize(), "该团已满");
            require(now.isBefore(head.expireTime()), "该团已过期");
        } else {
            require(now.isBefore(activity.endTime()), "拼团活动已结束");
        }
        var product = products.byActivityAndSku(activityId, skuId);
        require(product != null, "拼团商品不存在");
        var sku = skus.catalogSku(skuId);
        require(sku != null && count <= sku.stock(), "拼团商品库存不足");
        var joined = records.byUserAndActivity(userId, activityId).stream()
                .filter(item -> !Objects.equals(item.status(), 2)).toList();
        require(joined.stream().noneMatch(item -> Objects.equals(item.status(), 0)), "您已有正在进行的拼团");
        var total = joined.stream().mapToInt(CombinationRecord::count).sum();
        require(total + count <= activity.totalLimitCount(), "超过总限购数量");
        return product;
    }

    /** 订单支付后创建拼团记录；团长开团或加入父团，满员即成功。 */
    public CombinationRecordDTO create(CombinationRecordCreateCmd cmd) {
        var member = members.requireActive(cmd.userId());
        var product = validateJoin(cmd.userId(), cmd.activityId(), cmd.headId(), cmd.skuId(), cmd.count());
        var activity = activities.find(cmd.activityId());
        var spu = spus.findSpu(cmd.spuId());
        require(spu != null, "拼团商品不存在");
        var joining = cmd.headId() != null && cmd.headId() != CombinationRecord.HEAD_ID_GROUP;
        var head = joining ? records.lockById(cmd.headId()) : null;
        var now = LocalDateTime.now();
        var record = records.save(new CombinationRecord(null, activity.id(), product.combinationPrice(),
                spu.id(), spu.name(), spu.image(), cmd.skuId(), cmd.count(), cmd.userId(),
                member == null ? null : member.nickname(), member == null ? null : member.avatar(),
                joining ? head.id() : CombinationRecord.HEAD_ID_GROUP, 0, null, activity.userSize(), 1,
                false, joining ? head.expireTime() : now.plusHours(activity.limitDuration()),
                joining ? head.startTime() : now, null));
        var groupCount = (int) (records.byHeadId(joining ? head.id() : record.id()).size() + 1);
        finishGroup(joining ? head.id() : record.id(), groupCount, activity, false);
        return dto(records.find(record.id()));
    }

    /** 满员时整团成功，否则仅同步人数，保证各团员看到同一进度。 */
    private void finishGroup(Long headId, int count, CombinationActivity activity, boolean virtual) {
        var success = count >= activity.userSize();
        var now = LocalDateTime.now();
        var head = records.find(headId);
        require(head != null, "团长记录不存在");
        records.finish(headId, success ? 1 : 0, count, virtual, success ? now : null);
        for (var item : records.byHeadId(headId)) {
            records.finish(item.id(), success ? 1 : 0, count, virtual, success ? now : null);
        }
    }

    public Boolean bindOrder(Long recordId, Long orderId) {
        require(orderId != null && orderId > 0, "订单编号不合法");
        require(records.bindOrder(recordId, orderId), "拼团记录不存在");
        return true;
    }

    /** 处理过期团长记录：虚拟成团补齐人数，否则整团失败。 */
    public int expire() {
        var heads = records.expiredHeads(LocalDateTime.now());
        var handled = 0;
        for (var head : heads) {
            try {
                var activity = activities.find(head.activityId());
                if (activity == null) {
                    continue;
                }
                if (Boolean.TRUE.equals(activity.virtualGroup())) {
                    virtualGroup(head, activity);
                } else {
                    var expiredAt = LocalDateTime.now();
                    records.finish(head.id(), 2, head.userCount(), false, expiredAt);
                    for (var item : records.byHeadId(head.id())) {
                        records.finish(item.id(), 2, head.userCount(), false, expiredAt);
                    }
                }
                handled++;
            } catch (RuntimeException exception) {
                log.error("拼团过期处理失败, recordId={}", head.id(), exception);
            }
        }
        return handled;
    }

    /** 虚拟成团补齐差额记录后整团成功。 */
    private void virtualGroup(CombinationRecord head, CombinationActivity activity) {
        var current = records.byHeadId(head.id()).size() + 1;
        for (int index = current; index < activity.userSize(); index++) {
            records.save(new CombinationRecord(null, head.activityId(), head.combinationPrice(), head.spuId(),
                    head.spuName(), head.picUrl(), head.skuId(), 1, 0L, "虚拟用户", null, head.id(), 1,
                    null, activity.userSize(), activity.userSize(), true, head.expireTime(), head.startTime(),
                    LocalDateTime.now()));
        }
        finishGroup(head.id(), activity.userSize(), activity, true);
    }

    @Transactional(readOnly = true)
    public PageResult<CombinationRecordDTO> page(CombinationRecordPageQry qry) {
        require(qry.pageNum() != null && qry.pageNum() >= 1 && qry.pageSize() != null
                && qry.pageSize() >= 1 && qry.pageSize() <= 100, "分页参数不合法");
        var page = records.page(new CombinationRecordFilter(qry.userId(), qry.activityId(), qry.status(),
                qry.headId(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(this::dto).toList(), page.getTotal());
    }

    @Transactional(readOnly = true)
    public CombinationRecordDetailDTO detail(Long id, Long userId) {
        var value = record(id);
        var headId = Objects.equals(value.headId(), CombinationRecord.HEAD_ID_GROUP) ? value.id() : value.headId();
        var head = record(headId);
        var members = records.byHeadId(headId);
        var orderId = Objects.equals(value.userId(), userId) ? value.orderId() : null;
        return new CombinationRecordDetailDTO(dto(head), members.stream().map(this::dto).toList(), orderId);
    }

    /** 最近 n 条团长记录，用于活动详情展示可参与的团。 */
    @Transactional(readOnly = true)
    public List<CombinationRecordDTO> heads(Long activityId, Integer status, int count) {
        require(count >= 1 && count <= 20, "数量不合法");
        var page = records.page(new CombinationRecordFilter(null, activityId, status,
                CombinationRecord.HEAD_ID_GROUP, 1, count));
        return page.getRecords().stream().map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public CombinationSummaryDTO summary() {
        var total = records.userCount();
        var avatars = records.latest(10).stream().map(CombinationRecord::avatar)
                .filter(Objects::nonNull).toList();
        return new CombinationSummaryDTO(total, avatars);
    }

    private CombinationRecordDTO dto(CombinationRecord value) {
        return new CombinationRecordDTO(value.id(), value.activityId(), value.combinationPrice(), value.spuId(),
                value.spuName(), value.picUrl(), value.skuId(), value.count(), value.userId(), value.nickname(),
                value.avatar(), value.headId(), value.status(), value.orderId(), value.userSize(),
                value.userCount(), value.virtualGroup(), value.expireTime(), value.startTime(), value.endTime());
    }

    /** 拼团记录的团长及成员，供订单集成按订单号回查。 */
    @Transactional(readOnly = true)
    public List<CombinationRecordDTO> byHead(Long headId) {
        var result = new ArrayList<>(records.byHeadId(headId));
        var head = records.find(headId);
        if (head != null) {
            result.add(0, head);
        }
        return result.stream().map(this::dto).toList();
    }
}
