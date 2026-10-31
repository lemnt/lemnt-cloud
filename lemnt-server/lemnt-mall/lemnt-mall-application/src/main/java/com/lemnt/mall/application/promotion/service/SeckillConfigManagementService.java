package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.mall.application.promotion.cmd.SeckillConfigCreateCmd;
import com.lemnt.mall.application.promotion.cmd.SeckillConfigUpdateCmd;
import com.lemnt.mall.application.promotion.dto.SeckillConfigDTO;
import com.lemnt.mall.application.promotion.qry.SeckillConfigPageQry;
import com.lemnt.mall.domain.promotion.gateway.SeckillActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.SeckillConfigGateway;
import com.lemnt.mall.domain.promotion.model.SeckillConfig;
import com.lemnt.mall.domain.promotion.model.SeckillConfigFilter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.LocalTime;
import java.util.List;

/** 秒杀时段直接管理，同一时间点只允许一个有效时段。 */
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SeckillConfigManagementService {
    private final SeckillConfigGateway configs;
    private final SeckillActivityGateway activities;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    private LocalTime time(String value, String label) {
        require(value != null && value.matches("^([01]\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$"), label + "格式不合法");
        return LocalTime.parse(value.length() == 5 ? value + ":00" : value);
    }

    private void validate(SeckillConfig value) {
        require(value.name() != null && !value.name().isBlank() && value.name().length() <= 100, "时段名称不合法");
        var start = time(value.startTime(), "开始时间");
        var end = time(value.endTime(), "结束时间");
        require(start.isBefore(end), "开始时间必须早于结束时间");
        require(value.status() != null && (value.status() == 0 || value.status() == 1), "状态不合法");
    }

    /** 先锁全部时段再比较区间，避免并发创建出重叠时段。 */
    private void validateConflict(SeckillConfig value) {
        var all = configs.all();
        var start = time(value.startTime(), "开始时间");
        var end = time(value.endTime(), "结束时间");
        for (var other : all) {
            if (value.id() != null && value.id().equals(other.id())) {
                continue;
            }
            var otherStart = time(other.startTime(), "开始时间");
            var otherEnd = time(other.endTime(), "结束时间");
            require(!(start.isBefore(otherEnd) && otherStart.isBefore(end)), "秒杀时段与其他时段重叠");
        }
    }

    private SeckillConfig config(Long id) {
        var value = configs.find(id);
        require(value != null, "秒杀时段不存在");
        return value;
    }

    public Long create(SeckillConfigCreateCmd cmd) {
        var value = new SeckillConfig(null, cmd.name(), cmd.startTime(), cmd.endTime(),
                cmd.sliderPicUrls(), cmd.status());
        validate(value);
        validateConflict(value);
        return configs.save(value).id();
    }

    public Boolean update(SeckillConfigUpdateCmd cmd) {
        config(cmd.id());
        var value = new SeckillConfig(cmd.id(), cmd.name(), cmd.startTime(), cmd.endTime(),
                cmd.sliderPicUrls(), cmd.status());
        validate(value);
        validateConflict(value);
        configs.save(value);
        return true;
    }

    public Boolean updateStatus(Long id, Integer status) {
        config(id);
        require(status != null && (status == 0 || status == 1), "状态不合法");
        configs.updateStatus(id, status);
        return true;
    }

    /** 被活动引用的时段不能删除，避免活动无法展示。 */
    public Boolean delete(Long id) {
        config(id);
        require(activities.all().stream().noneMatch(a -> a.configIds().contains(id)), "时段已关联秒杀活动");
        configs.delete(id);
        return true;
    }

    @Transactional(readOnly = true)
    public SeckillConfigDTO detail(Long id) {
        return dto(config(id));
    }

    @Transactional(readOnly = true)
    public List<SeckillConfigDTO> list() {
        return configs.all().stream().map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public List<SeckillConfigDTO> simpleList() {
        return configs.byStatus(0).stream().map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public PageResult<SeckillConfigDTO> page(SeckillConfigPageQry qry) {
        require(qry.pageNum() != null && qry.pageNum() >= 1 && qry.pageSize() != null
                && qry.pageSize() >= 1 && qry.pageSize() <= 100, "分页参数不合法");
        var page = configs.page(new SeckillConfigFilter(qry.name(), qry.status(), qry.pageNum(), qry.pageSize()));
        return PageResult.of(page.getRecords().stream().map(this::dto).toList(), page.getTotal());
    }

    private SeckillConfigDTO dto(SeckillConfig value) {
        var count = (int) activities.all().stream()
                .filter(activity -> activity.configIds().contains(value.id())).count();
        return new SeckillConfigDTO(value.id(), value.name(), value.startTime(), value.endTime(),
                value.sliderPicUrls(), value.status(), count);
    }
}
