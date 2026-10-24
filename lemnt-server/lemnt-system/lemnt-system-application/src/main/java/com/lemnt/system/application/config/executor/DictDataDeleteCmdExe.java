package com.lemnt.system.application.config.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.config.gateway.DictDataGateway;

import java.util.List;

/**
 * 删除字典数据命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictDataDeleteCmdExe {
    private final DictDataGateway dictDataGateway;

    public Boolean execute(List<Long> dictCodes) {
        return dictDataGateway.deleteByIds(dictCodes);
    }
}
