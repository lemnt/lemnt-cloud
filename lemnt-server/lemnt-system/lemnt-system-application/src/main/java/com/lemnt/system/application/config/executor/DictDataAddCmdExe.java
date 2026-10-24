package com.lemnt.system.application.config.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.application.config.cmd.DictDataAddCmd;
import com.lemnt.system.domain.config.model.DictData;
import com.lemnt.system.domain.config.gateway.DictDataGateway;

/**
 * 新增字典数据命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictDataAddCmdExe {
    private final DictDataGateway dictDataGateway;

    public Boolean execute(DictDataAddCmd cmd) {
        DictData entity = new DictData();
        entity.setDictSort(cmd.dictSort());
        entity.setDictLabel(cmd.dictLabel());
        entity.setDictValue(cmd.dictValue());
        entity.setDictType(cmd.dictType());
        entity.setCssClass(cmd.cssClass());
        entity.setListClass(cmd.listClass());
        entity.setIsDefault(cmd.isDefault());
        entity.setStatus(cmd.status());
        entity.setRemark(cmd.remark());
        entity.setCreateBy(SecurityUtils.getUsername());
        return dictDataGateway.insert(entity);
    }
}
