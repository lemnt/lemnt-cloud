package com.lemnt.system.application.config.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.config.cmd.DictTypeUpdateCmd;
import com.lemnt.system.domain.config.gateway.DictDataGateway;
import com.lemnt.system.domain.config.gateway.DictTypeGateway;
import com.lemnt.system.domain.config.model.DictType;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * 修改字典类型命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictTypeUpdateCmdExe {
    private final DictTypeGateway dictTypeGateway;
    private final DictDataGateway dictDataGateway;

    /** 修改前检查类型冲突，保持自身类型时允许保存。 */
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Boolean execute(DictTypeUpdateCmd cmd) {
        DictType existing = dictTypeGateway.checkDictTypeUnique(cmd.dictType());
        if (existing != null && !Objects.equals(existing.getDictId(), cmd.dictId())) {
            throw new ServiceException("修改字典'" + cmd.dictName() + "'失败，字典类型已存在");
        }
        DictType entity = new DictType();
        entity.setDictId(cmd.dictId());
        entity.setDictName(cmd.dictName());
        entity.setDictType(cmd.dictType());
        entity.setStatus(cmd.status());
        entity.setRemark(cmd.remark());
        entity.setUpdateBy(SecurityUtils.getUsername());
        DictType previous = dictTypeGateway.selectById(entity.getDictId());
        if (previous != null) {
            dictDataGateway.renameType(previous.getDictType(), entity.getDictType());
        }
        Boolean updated = dictTypeGateway.update(entity);
        if (Boolean.TRUE.equals(updated)) {
            dictDataGateway.refreshByType(entity.getDictType());
        }
        return updated;
    }
}
