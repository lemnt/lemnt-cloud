package com.lemnt.member.start;

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

class MemberControllerMappingTest {

    /** 全量注册拆分后的接口，核对原有路径、HTTP 方法和权限，并检测映射冲突。 */
    @Test
    void businessControllersPreserveHttpRoutesAndPermissions() throws Exception {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        var controllers = new ArrayList<Object>();
        for (var definition : scanner.findCandidateComponents("com.lemnt.member.adapter")) {
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
                DELETE /app/addresses/{id}
                DELETE /groups/{id} member:group:delete
                DELETE /growth/levels/{id} member:level:delete
                DELETE /growths/signin-configs/{day} member:sign-in-config:delete
                DELETE /tags/{id} member:tag:delete
                GET /app/addresses
                GET /app/addresses/default
                GET /app/addresses/{id}
                GET /app/growths/experience-records
                GET /app/growths/levels
                GET /app/growths/point-records
                GET /app/growths/signin/config
                GET /app/growths/signin/records
                GET /app/growths/signin/summary
                GET /app/users/user
                GET /groups member:group:query
                GET /groups/groups/page member:group:query
                GET /groups/list-all-simple member:group:query
                GET /groups/tags/page member:tag:query
                GET /groups/{id} member:group:query
                GET /growth/levels member:level:query
                GET /growth/levels/list-all-simple member:level:query
                GET /growth/levels/{id} member:level:query
                GET /growths/config member:config:query
                GET /growths/signin-configs member:sign-in-config:query
                GET /growths/signin-configs/{day} member:sign-in-config:query
                GET /member-users member:user:query
                GET /member-users/{id} member:user:query
                GET /member-users/{id}/addresses member:address:query
                GET /member-users/{id}/level-records member:level-record:query
                GET /member-users/{id}/points-records member:point-record:query
                GET /member-users/{id}/signin-records member:sign-in-record:query
                GET /member-users/{id}/tags member:user:query
                GET /member/admin/experience-records member:experience:query
                GET /member/admin/experience-records/{id} member:experience:query
                GET /member/admin/level-records member:level:query
                GET /member/admin/level-records/{id} member:level:query
                GET /member/admin/point-records member:point:query
                GET /member/admin/point-records/{id} member:point:query
                GET /member/admin/signin-records member:signin:query
                GET /tags member:tag:query
                GET /tags/list-all-simple member:tag:query
                GET /tags/{id} member:tag:query
                POST /app/addresses
                POST /app/growths/signin
                POST /groups member:group:create
                POST /growth/levels member:level:create
                POST /tags member:tag:create
                PUT /app/addresses/{id}
                PUT /app/users/user
                PUT /app/users/user/password
                PUT /groups/{id} member:group:update
                PUT /growth/levels/{id} member:level:update
                PUT /growths/config member:config:update
                PUT /growths/signin-configs member:sign-in-config:update
                PUT /member-users/{id} member:user:update
                PUT /member-users/{id}/group member:user:update
                PUT /member-users/{id}/level member:user:update-level
                PUT /member-users/{id}/points member:user:update-point
                PUT /member-users/{id}/tags member:user:update
                PUT /tags/{id} member:tag:update
                """.split("\n")));
        // 文本块会移除行尾空格，空权限与带权限的路由统一去掉行尾空白。
        var normalized = new TreeSet<String>();
        actual.forEach(route -> normalized.add(route.stripTrailing()));
        assertEquals(expected, normalized);
    }
}
