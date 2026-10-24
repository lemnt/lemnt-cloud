package com.lemnt.system.adapter.web;

import com.lemnt.common.security.aspect.PreAuthorizeAspect;
import com.lemnt.common.security.auth.AuthLogic;
import com.lemnt.common.security.auth.AuthUtil;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.security.exception.NotPermissionException;
import com.lemnt.common.security.model.SecurityUser;
import com.lemnt.system.adapter.web.auth.MenuController;
import com.lemnt.system.adapter.web.config.ConfigController;
import com.lemnt.system.application.auth.executor.MenuRoleTreeQueryExe;
import com.lemnt.system.application.config.dto.ConfigDTO;
import com.lemnt.system.application.config.executor.ConfigDetailQueryExe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

/** 隔离验收：真实权限切面加合成数据，不访问网关、Redis 或业务数据库。 */
class ConfigRoleQueryPermissionTest {
    private AuthLogic previousLogic;

    @BeforeEach
    void initializeOrdinarySession() {
        previousLogic = AuthUtil.authLogic;
        AuthUtil.authLogic = new AuthLogic();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SecurityConstants.AUTHORIZATION_HEADER, "Bearer acceptance-synthetic");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER,
                new SecurityUser().setUserId(100L).setPermissions(Set.of("example:record:list")));
    }

    @AfterEach
    void clearSession() {
        AuthUtil.authLogic = previousLogic;
        SecurityContextHolder.remove();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldRejectProtectedConfigListWithoutPermission() throws Exception {
        ConfigController controller = proxied(ConfigController.class, null);
        assertThatThrownBy(() -> controller.list(null)).isInstanceOf(NotPermissionException.class);
    }

    @Test
    void shouldRejectConfigDetailWithoutPermission() throws Exception {
        ConfigController controller = proxied(ConfigController.class, configDetail());
        assertThatThrownBy(() -> controller.getInfo(9999L)).isInstanceOf(NotPermissionException.class);
    }

    @Test
    void shouldRejectConfigKeyWithoutPermission() throws Exception {
        ConfigController controller = proxied(ConfigController.class, configDetail());
        assertThatThrownBy(() -> controller.getConfigKey("acceptance.synthetic"))
                .isInstanceOf(NotPermissionException.class);
    }

    @Test
    void shouldRejectOtherRoleMenuSelectionWithoutPermission() throws Exception {
        MenuRoleTreeQueryExe query = new MenuRoleTreeQueryExe(null, null, null) {
            @Override
            public Map<String, Object> execute(Long roleId) {
                return Map.of("checkedKeys", List.of(9999L), "menus", List.of());
            }
        };
        MenuController controller = proxied(MenuController.class, query);
        assertThatThrownBy(() -> controller.roleMenuTreeSelect(1L))
                .isInstanceOf(NotPermissionException.class);
    }

    @Test
    void shouldAllowGrantedConfigDetailAndKeyQueries() throws Exception {
        SecurityContextHolder.get(SecurityConstants.LOGIN_USER, SecurityUser.class)
                .setPermissions(Set.of("system:config:query"));
        ConfigController controller = proxied(ConfigController.class, configDetail());

        assertThat(controller.getInfo(9999L).getData().configId()).isEqualTo(9999L);
        assertThat(controller.getConfigKey("acceptance.synthetic").getData().configKey())
                .isEqualTo("acceptance.synthetic");
    }

    @Test
    void shouldAllowGrantedRoleMenuQuery() throws Exception {
        SecurityContextHolder.get(SecurityConstants.LOGIN_USER, SecurityUser.class)
                .setPermissions(Set.of("system:role:query"));
        MenuRoleTreeQueryExe query = new MenuRoleTreeQueryExe(null, null, null) {
            @Override
            public Map<String, Object> execute(Long roleId) {
                return Map.of("checkedKeys", List.of(9999L), "menus", List.of());
            }
        };

        assertThat(proxied(MenuController.class, query).roleMenuTreeSelect(1L).getData().checkedKeys())
                .containsExactly(9999L);
    }

    @Test
    void shouldNotAcceptRoleQueryPermissionForConfigQueries() throws Exception {
        SecurityContextHolder.get(SecurityConstants.LOGIN_USER, SecurityUser.class)
                .setPermissions(Set.of("system:role:query"));
        ConfigController controller = proxied(ConfigController.class, configDetail());

        assertThatThrownBy(() -> controller.getInfo(9999L)).isInstanceOf(NotPermissionException.class);
        assertThatThrownBy(() -> controller.getConfigKey("acceptance.synthetic"))
                .isInstanceOf(NotPermissionException.class);
    }

    /** 合成详情结果只用于观察鉴权是否在控制器调用前执行。 */
    private ConfigDetailQueryExe configDetail() {
        return new ConfigDetailQueryExe(null) {
            @Override
            public ConfigDTO executeById(Long configId) {
                ConfigDTO dto = new ConfigDTO();
                dto.setConfigId(configId);
                return dto;
            }

            @Override
            public ConfigDTO executeByKey(String configKey) {
                ConfigDTO dto = new ConfigDTO();
                dto.setConfigKey(configKey);
                return dto;
            }
        };
    }

    /** 按构造器注入唯一查询桩，其他依赖仅用于被鉴权拦截的路径。 */
    private <T> T proxied(Class<T> type, Object query) throws Exception {
        var constructor = type.getConstructors()[0];
        var types = constructor.getParameterTypes();
        Object[] arguments = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            if (query != null && types[i].isInstance(query)) arguments[i] = query;
        }
        T target = type.cast(constructor.newInstance(arguments));
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.setProxyTargetClass(true);
        factory.addAspect(new PreAuthorizeAspect());
        return type.cast(factory.getProxy());
    }
}
