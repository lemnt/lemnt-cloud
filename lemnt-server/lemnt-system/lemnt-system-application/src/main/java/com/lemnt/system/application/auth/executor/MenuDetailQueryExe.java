package com.lemnt.system.application.auth.executor;

import com.lemnt.system.application.auth.converter.MenuConverter;
import com.lemnt.system.application.auth.dto.MenuDTO;
import com.lemnt.system.domain.auth.gateway.MenuGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 菜单详情查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class MenuDetailQueryExe {
    private final MenuGateway menuGateway;
    public MenuDTO execute(Long menuId) {
        return MenuConverter.toDTO(menuGateway.selectById(menuId));
    }
}
