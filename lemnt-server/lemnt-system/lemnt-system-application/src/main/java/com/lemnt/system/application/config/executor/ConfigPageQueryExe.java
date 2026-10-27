package com.lemnt.system.application.config.executor;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.application.config.converter.ConfigConverter;
import com.lemnt.system.application.config.dto.ConfigDTO;
import com.lemnt.system.application.config.qry.ConfigPageQry;
import com.lemnt.system.domain.config.gateway.ConfigGateway;
import com.lemnt.system.domain.config.model.Config;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 参数配置分页查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class ConfigPageQueryExe {
    private final ConfigGateway configGateway;

    /**
     * 执行分页查询
     *
     * @param qry 查询条件
     * @return 分页结果
     */
    public PageResult<ConfigDTO> execute(ConfigPageQry qry) {
        Config query = new Config();
        query.setConfigName(qry.configName());
        query.setConfigKey(qry.configKey());
        query.setConfigType(qry.configType());
        query.setBeginTime(qry.beginTime());
        query.setEndTime(qry.endTime());
        PageResult<Config> pageResult = configGateway.selectPage(query, new PageQry(qry.pageNum(), qry.pageSize()));
        List<ConfigDTO> dtoList = ConfigConverter.toDTOList(pageResult.getRecords());
        return PageResult.of(dtoList, pageResult.getTotal());
    }
}
