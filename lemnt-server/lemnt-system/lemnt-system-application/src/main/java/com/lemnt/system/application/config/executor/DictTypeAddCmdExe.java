package com.lemnt.system.application.config.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.config.cmd.DictTypeAddCmd;
import com.lemnt.system.domain.config.gateway.DictTypeGateway;
import com.lemnt.system.domain.config.model.DictType;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 新增字典类型命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictTypeAddCmdExe {
    private final DictTypeGateway dictTypeGateway;

    /** 新增前校验类型，重复时返回明确的业务错误。 */
    public Boolean execute(DictTypeAddCmd cmd) {
        if (dictTypeGateway.checkDictTypeUnique(cmd.dictType()) != null) {
            throw new ServiceException("新增字典'" + cmd.dictName() + "'失败，字典类型已存在");
        }
        DictType entity = new DictType();
        entity.setDictName(cmd.dictName());
        entity.setDictType(cmd.dictType());
        entity.setStatus(cmd.status());
        entity.setRemark(cmd.remark());
        entity.setCreateBy(SecurityUtils.getUsername());
        return dictTypeGateway.insert(entity);
    }
}
