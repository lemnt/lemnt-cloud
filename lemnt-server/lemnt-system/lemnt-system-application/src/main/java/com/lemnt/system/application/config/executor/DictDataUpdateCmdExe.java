package com.lemnt.system.application.config.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.config.cmd.DictDataUpdateCmd;
import com.lemnt.system.domain.config.gateway.DictDataGateway;
import com.lemnt.system.domain.config.model.DictData;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 修改字典数据命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictDataUpdateCmdExe {
    private final DictDataGateway dictDataGateway;
    public Boolean execute(DictDataUpdateCmd cmd) {
        DictData entity = new DictData();
        entity.setDictCode(cmd.dictCode());
        entity.setDictSort(cmd.dictSort());
        entity.setDictLabel(cmd.dictLabel());
        entity.setDictValue(cmd.dictValue());
        entity.setDictType(cmd.dictType());

        // 完整表单清空可选文本后会传 null，转为空串以免 updateById 跳过更新。
        entity.setCssClass(cmd.cssClass() == null ? "" : cmd.cssClass());
        entity.setListClass(cmd.listClass());
        entity.setIsDefault(cmd.isDefault());
        entity.setStatus(cmd.status());
        entity.setRemark(cmd.remark() == null ? "" : cmd.remark());
        entity.setUpdateBy(SecurityUtils.getUsername());
        return dictDataGateway.update(entity);
    }
}
