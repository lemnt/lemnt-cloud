package com.lemnt.system.start;

import com.lemnt.common.security.annotation.RequiresPermissions;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ControllerMappingTest {

    /** 全量注册拆分后的接口，核对原有路径、HTTP 方法和权限，并检测映射冲突。 */
    @Test
    void businessControllersPreserveHttpRoutesAndPermissions() throws Exception {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        var controllers = new ArrayList<Object>();
        for (var definition : scanner.findCandidateComponents("com.lemnt.system.adapter")) {
            var type = Class.forName(definition.getBeanClassName());
            var constructor = type.getConstructors()[0];
            var dependencies = Arrays.stream(constructor.getParameterTypes()).map(t -> mock(t)).toArray();
            controllers.add(constructor.newInstance(dependencies));
        }
        var mvc = MockMvcBuilders.standaloneSetup(controllers.toArray()).build();
        var mappings = mvc.getDispatcherServlet().getWebApplicationContext()
                .getBean(RequestMappingHandlerMapping.class);
        var actual = new TreeSet<String>();
        mappings.getHandlerMethods().forEach((mapping, handler) -> {
            var permission = handler.getMethodAnnotation(RequiresPermissions.class);
            var permissions = permission == null ? "" : String.join(",", permission.value());
            for (var method : mapping.getMethodsCondition().getMethods()) {
                for (var path : mapping.getPatternValues()) {
                    actual.add(method.name() + " " + path + " " + permissions);
                }
            }
        });
        var expected = new TreeSet<>(Arrays.asList("""
                DELETE /configurations/refresh-cache system:config:remove
                DELETE /configurations/{configIds} system:config:remove
                DELETE /departments/{deptId} system:dept:remove
                DELETE /dictionary-data/{dictCodes} system:dict:remove
                DELETE /dictionary-types/refresh-cache system:dict:remove
                DELETE /dictionary-types/{dictIds} system:dict:remove
                DELETE /login-logs/clean system:logininfor:remove
                DELETE /login-logs/{infoIds} system:logininfor:remove
                DELETE /menus/{menuId} system:menu:remove
                DELETE /notices/{noticeIds} system:notice:remove
                DELETE /operation-logs/clean system:operlog:remove
                DELETE /operation-logs/{operIds} system:operlog:remove
                DELETE /posts/{postIds} system:post:remove
                DELETE /roles/{roleIds} system:role:remove
                DELETE /users/{userIds} system:user:remove
                GET /configurations system:config:list
                GET /configurations/config-key/{configKey} system:config:query
                GET /configurations/{configId} system:config:query
                GET /departments system:dept:list
                GET /departments/exclude/{deptId} system:dept:list
                GET /departments/{deptId} system:dept:query
                GET /dictionary-data system:dict:list
                GET /dictionary-data/type/{dictType}
                GET /dictionary-data/{dictCode}
                GET /dictionary-types system:dict:list
                GET /dictionary-types/options
                GET /dictionary-types/{dictId}
                GET /login-logs system:logininfor:list
                GET /menus system:menu:list
                GET /menus/role-menu-tree-select/{roleId} system:role:query
                GET /menus/routers
                GET /menus/tree-select
                GET /menus/{menuId} system:menu:query
                GET /notices system:notice:list
                GET /notices/read-users system:notice:list
                GET /notices/top
                GET /notices/{noticeId}
                GET /operation-logs system:operlog:list
                GET /posts system:post:list
                GET /posts/{postId}
                GET /roles system:role:list
                GET /roles/auth-user/allocated-list system:role:list
                GET /roles/auth-user/unallocated-list system:role:list
                GET /roles/dept-tree/{roleId} system:role:query
                GET /roles/options system:role:query
                GET /roles/{roleId} system:role:query
                GET /users system:user:list
                GET /users/auth-role/{userId} system:user:query
                GET /users/current-info
                GET /users/dept-tree system:user:list
                GET /users/options system:user:query
                GET /users/profile
                GET /users/{userId} system:user:query
                POST /configurations system:config:add
                POST /departments system:dept:add
                POST /dictionary-data system:dict:add
                POST /dictionary-types system:dict:add
                POST /files/upload
                POST /menus system:menu:add
                POST /notices system:notice:add
                POST /notices/mark-read
                POST /notices/mark-read-all
                POST /posts system:post:add
                POST /roles system:role:add
                POST /users system:user:add
                POST /users/profile/avatar
                PUT /configurations system:config:edit
                PUT /departments system:dept:edit
                PUT /departments/update-sort system:dept:edit
                PUT /dictionary-data system:dict:edit
                PUT /dictionary-types system:dict:edit
                PUT /menus system:menu:edit
                PUT /notices system:notice:edit
                PUT /posts system:post:edit
                PUT /roles system:role:edit
                PUT /roles/auth-user/cancel system:role:edit
                PUT /roles/auth-user/cancel-all system:role:edit
                PUT /roles/auth-user/select-all system:role:edit
                PUT /roles/change-status system:role:edit
                PUT /roles/data-scope system:role:edit
                PUT /users system:user:edit
                PUT /users/auth-role system:user:edit
                PUT /users/change-status system:user:edit
                PUT /users/profile
                PUT /users/profile/update-password
                PUT /users/reset-password system:user:resetPwd
                PUT /users/unlock/{userName} system:user:edit
                """.split("\n")));
        // 文本块会移除行尾空格，空权限与带权限的路由统一去掉行尾空白。
        var normalized = new TreeSet<String>();
        actual.forEach(route -> normalized.add(route.stripTrailing()));
        assertEquals(expected, normalized);
    }
}
