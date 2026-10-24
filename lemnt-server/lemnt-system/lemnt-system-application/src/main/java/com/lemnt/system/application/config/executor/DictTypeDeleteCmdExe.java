package com.lemnt.system.application.config.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.config.gateway.DictTypeGateway;

import java.util.List;

/**
 * 删除字典类型命令执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictTypeDeleteCmdExe {
    private final DictTypeGateway dictTypeGateway;

    public Boolean execute(List<Long> dictIds) {
        return dictTypeGateway.deleteByIds(dictIds);
    }
}
