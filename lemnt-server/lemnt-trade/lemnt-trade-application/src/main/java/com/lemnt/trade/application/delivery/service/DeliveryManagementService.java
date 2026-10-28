package com.lemnt.trade.application.delivery.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.delivery.cmd.DeliveryChargeCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryExpressCreateCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryExpressTemplateCreateCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryExpressTemplateUpdateCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryExpressUpdateCmd;
import com.lemnt.trade.application.delivery.cmd.DeliveryFreeCmd;
import com.lemnt.trade.application.delivery.converter.DeliveryDTOConverter;
import com.lemnt.trade.application.delivery.dto.DeliveryExpressTemplateDetailDTO;
import com.lemnt.trade.domain.config.gateway.TradeConfigGateway;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressGateway;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressTemplateChargeGateway;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressTemplateFreeGateway;
import com.lemnt.trade.domain.delivery.gateway.DeliveryExpressTemplateGateway;
import com.lemnt.trade.domain.delivery.model.DeliveryExpress;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplate;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplateCharge;
import com.lemnt.trade.domain.delivery.model.DeliveryExpressTemplateFree;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 快递公司和模板主从表事务，使用配置行锁串行维护名称及编码唯一性。 */
@Component
@RequiredArgsConstructor
public class DeliveryManagementService {
    private final TradeConfigGateway config;
    private final DeliveryExpressGateway expresses;
    private final DeliveryExpressTemplateGateway templates;
    private final DeliveryExpressTemplateChargeGateway charges;
    private final DeliveryExpressTemplateFreeGateway frees;

    /** 新增快递公司，编码重复拒绝。 */
    @Transactional(rollbackFor = Exception.class)
    public Long createExpress(DeliveryExpressCreateCmd cmd) {
        config.lock();
        var value = new DeliveryExpress(null, text(cmd.code(), 40), text(cmd.name(), 100),
                cmd.logo(), cmd.sort(), cmd.status());
        validateExpress(value);
        require(expresses.byCode(value.code()) == null, "快递公司编码重复");
        return expresses.insert(value).id();
    }

    /** 修改公司信息，允许停用但不能覆盖另一公司编码。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateExpress(DeliveryExpressUpdateCmd cmd) {
        config.lock();
        require(expresses.find(cmd.id()) != null, "快递公司不存在");
        var value = new DeliveryExpress(cmd.id(), text(cmd.code(), 40), text(cmd.name(), 100),
                cmd.logo(), cmd.sort(), cmd.status());
        validateExpress(value);
        var same = expresses.byCode(value.code());
        require(same == null || same.id().equals(value.id()), "快递公司编码重复");
        return expresses.update(value);
    }

    /** 逻辑删除公司，已有物流记录仍保留公司编码。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteExpress(Long id) {
        config.lock();
        require(expresses.find(id) != null, "快递公司不存在");
        return expresses.delete(id);
    }

    /** 创建主模板及子规则。 */
    @Transactional(rollbackFor = Exception.class)
    public Long createTemplate(DeliveryExpressTemplateCreateCmd cmd) {
        config.lock();
        validateTemplate(null, cmd.name(), cmd.chargeMode(), cmd.sort(), cmd.charges(), cmd.frees());
        require(cmd.charges().stream().allMatch(row -> row.id() == null)
                && cmd.frees().stream().allMatch(row -> row.id() == null), "新增模板不能指定旧规则编号");
        var saved = templates.insert(new DeliveryExpressTemplate(null, text(cmd.name(), 100),
                cmd.chargeMode(), cmd.sort()));
        saveRules(saved.id(), cmd.chargeMode(), cmd.charges(), cmd.frees());
        return saved.id();
    }

    /** 锁定模板后更新规则，已有子项保留主键，省略的子项逻辑删除。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTemplate(DeliveryExpressTemplateUpdateCmd cmd) {
        config.lock();
        require(templates.lock(cmd.id()) != null, "运费模板不存在");
        validateTemplate(cmd.id(), cmd.name(), cmd.chargeMode(), cmd.sort(), cmd.charges(), cmd.frees());
        saveRules(cmd.id(), cmd.chargeMode(), cmd.charges(), cmd.frees());
        return templates.update(new DeliveryExpressTemplate(cmd.id(), text(cmd.name(), 100),
                cmd.chargeMode(), cmd.sort()));
    }

    /** 删除主模板和所有子项，保留历史审计。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTemplate(Long id) {
        config.lock();
        require(templates.lock(id) != null, "运费模板不存在");
        charges.deleteByTemplate(id);
        frees.deleteByTemplate(id);
        return templates.delete(id);
    }

    /** 在同一事务内读取主模板和完整规则。 */
    @Transactional(readOnly = true)
    public DeliveryExpressTemplateDetailDTO detail(Long id) {
        var row = templates.find(id);
        if (row == null) {
            return null;
        }
        return new DeliveryExpressTemplateDetailDTO(row.id(), row.name(), row.chargeMode(), row.sort(),
                charges.byTemplate(id).stream().map(DeliveryDTOConverter::toDTO).toList(),
                frees.byTemplate(id).stream().map(DeliveryDTOConverter::toDTO).toList());
    }

    private void saveRules(Long id, Integer mode, List<DeliveryChargeCmd> chargeRows, List<DeliveryFreeCmd> freeRows) {
        var oldCharges = charges.byTemplate(id);
        var oldFrees = frees.byTemplate(id);
        Set<Long> retainedCharges = new HashSet<>();
        Set<Long> retainedFrees = new HashSet<>();
        for (var input : chargeRows) {
            var row = new DeliveryExpressTemplateCharge(input.id(), id, List.copyOf(input.areaIds()), mode,
                    input.startCount(), input.startPrice(), input.extraCount(), input.extraPrice());
            if (row.id() == null) {
                charges.insert(row);
            } else {
                require(retainedCharges.add(row.id()) && oldCharges.stream().anyMatch(old -> old.id().equals(row.id())),
                        "计费规则重复或不属于当前模板");
                require(charges.update(row), "计费规则保存失败");
            }
        }
        for (var input : freeRows) {
            var row = new DeliveryExpressTemplateFree(input.id(), id, List.copyOf(input.areaIds()),
                    input.freePrice(), input.freeCount());
            if (row.id() == null) {
                frees.insert(row);
            } else {
                require(retainedFrees.add(row.id()) && oldFrees.stream().anyMatch(old -> old.id().equals(row.id())),
                        "包邮规则重复或不属于当前模板");
                require(frees.update(row), "包邮规则保存失败");
            }
        }
        charges.deleteIds(oldCharges.stream().map(DeliveryExpressTemplateCharge::id)
                .filter(key -> !retainedCharges.contains(key)).toList());
        frees.deleteIds(oldFrees.stream().map(DeliveryExpressTemplateFree::id)
                .filter(key -> !retainedFrees.contains(key)).toList());
    }

    private void validateTemplate(Long id, String name, Integer mode, Integer sort,
            List<DeliveryChargeCmd> chargeRows, List<DeliveryFreeCmd> freeRows) {
        String validName = text(name, 100);
        require(mode != null && Set.of(1, 2, 3).contains(mode), "运费计费方式不合法");
        require(sort != null && sort >= 0, "排序不合法");
        require(templates.list(validName).stream().noneMatch(row -> row.name().equals(validName)
                && !row.id().equals(id)), "运费模板名称重复");
        require(chargeRows != null && freeRows != null && !chargeRows.isEmpty(), "至少配置一组计费规则");
        require(chargeRows.size() <= 100 && freeRows.size() <= 100, "模板规则过多");
        Set<Integer> chargeAreas = new HashSet<>();
        for (var row : chargeRows) {
            require(row != null, "计费规则不能为空");
            validateAreas(row.areaIds(), chargeAreas);
            positiveQuantity(row.startCount(), mode);
            positiveQuantity(row.extraCount(), mode);
            require(row.startPrice() != null && row.startPrice() >= 0
                    && row.extraPrice() != null && row.extraPrice() >= 0, "运费金额不能为负");
        }
        Set<Integer> freeAreas = new HashSet<>();
        for (var row : freeRows) {
            require(row != null, "包邮规则不能为空");
            validateAreas(row.areaIds(), freeAreas);
            require(row.freePrice() != null && row.freePrice() >= 0
                    && row.freeCount() != null && row.freeCount() >= 0, "包邮门槛不合法");
        }
    }

    private void validateAreas(List<Integer> ids, Set<Integer> used) {
        require(ids != null && !ids.isEmpty() && ids.size() <= 10000, "配送区域不能为空或过多");
        for (var id : ids) {
            require(id != null && id > 0 && used.add(id), "配送区域不合法或重复");
        }
    }

    private void positiveQuantity(BigDecimal value, Integer mode) {
        require(value != null && value.signum() > 0 && value.scale() <= 6
                && value.compareTo(new BigDecimal("999999999999")) <= 0, "首量或续量不合法");
        if (mode == 1) {
            require(value.stripTrailingZeros().scale() <= 0, "按件计费的数量必须为整数");
        }
    }

    private void validateExpress(DeliveryExpress row) {
        require(row.sort() != null && row.sort() >= 0 && row.status() != null
                && Set.of(0, 1).contains(row.status()), "快递公司状态或排序不合法");
        require(row.logo() == null || row.logo().length() <= 500, "快递公司图标过长");
    }

    private String text(String value, int max) {
        require(value != null && !value.isBlank() && value.trim().length() <= max, "名称或编码不能为空或过长");
        return value.trim();
    }

    private void require(boolean value, String message) {
        if (!value) {
            throw new ServiceException(message, 400);
        }
    }
}
