package com.lemnt.system.infrastructure.log.gateway;

import cn.hutool.core.collection.CollUtil;
import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.log.model.OperLog;
import com.lemnt.system.domain.log.gateway.OperLogGateway;
import com.lemnt.system.infrastructure.log.converter.OperLogConverter;
import com.lemnt.system.infrastructure.log.dataobject.OperLogDO;
import com.lemnt.system.infrastructure.log.mapper.OperLogMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 操作日志网关实现 — infrastructure 层适配器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class OperLogGatewayImpl implements OperLogGateway {

    private final OperLogMapper operLogMapper;

    @Override
    public PageResult<OperLog> selectPage(OperLog query, PageQry pageQry) {
        Page<OperLogDO> page = new Page<>(pageQry.getPageNum(), pageQry.getPageSize());
        IPage<OperLogDO> result = operLogMapper.selectPage(page, query);
        List<OperLog> records = OperLogConverter.toEntityList(result.getRecords());
        return PageResult.of(records, result.getTotal());
    }

    @Override
    public Boolean insert(OperLog entity) {
        return operLogMapper.insert(OperLogConverter.toDO(entity)) > 0;
    }

    @Override
    public Boolean deleteByIds(List<Long> operIds) {
        if (CollUtil.isEmpty(operIds)) {
            return false;
        }
        return operLogMapper.deleteByIds(operIds) > 0;
    }

    @Override
    public OperLog selectById(Long operId) {
        return OperLogConverter.toEntity(operLogMapper.selectById(operId));
    }

    @Override
    public void clean() {
        operLogMapper.cleanOperLog();
    }
}
