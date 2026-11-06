package com.lemnt.system.infrastructure.log.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.log.gateway.LogininforGateway;
import com.lemnt.system.domain.log.model.Logininfor;
import com.lemnt.system.infrastructure.log.converter.LogininforConverter;
import com.lemnt.system.infrastructure.log.dataobject.LogininforDO;
import com.lemnt.system.infrastructure.log.mapper.LogininforMapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 登录日志网关实现 — infrastructure 层适配器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class LogininforGatewayImpl implements LogininforGateway {
    private final LogininforMapper logininforMapper;

    @Override
    public Boolean insert(Logininfor entity) {
        return logininforMapper.insert(LogininforConverter.toDO(entity)) > 0;
    }

    @Override
    public PageResult<Logininfor> selectPage(Logininfor query, PageQry pageQry) {
        Page<LogininforDO> page = new Page<>(pageQry.getPageNum(), pageQry.getPageSize());
        IPage<LogininforDO> result = logininforMapper.selectPage(page, query);
        List<Logininfor> records = LogininforConverter.toEntityList(result.getRecords());
        return PageResult.of(records, result.getTotal());
    }

    @Override
    public Boolean deleteByIds(List<Long> infoIds) {
        if (CollUtil.isEmpty(infoIds)) {
            return false;
        }
        return logininforMapper.deleteByIds(infoIds) > 0;
    }

    @Override
    public void clean() {
        logininforMapper.cleanLogininfor();
    }
}
