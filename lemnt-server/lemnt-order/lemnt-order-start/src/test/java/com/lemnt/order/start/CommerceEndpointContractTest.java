package com.lemnt.order.start;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.common.security.annotation.RequiresPermissions;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/** 网关保持无业务RPC依赖；在已有四模块测试装配中核对真实Controller归属和原权限。 */
class CommerceEndpointContractTest {
    @Test
    void allOriginalEndpointsPreserveOwnerMethodPathAndPermissions() throws Exception {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        var controllers = new ArrayList<Object>();
        for (var owner : List.of("product", "promotion", "order", "pay")) {
            for (var definition :
                    scanner.findCandidateComponents("com.lemnt." + owner + ".adapter")) {
                var type = Class.forName(definition.getBeanClassName());
                var constructor = type.getConstructors()[0];
                controllers.add(
                        constructor.newInstance(
                                Arrays.stream(constructor.getParameterTypes())
                                        .map(t -> mock(t))
                                        .toArray()));
            }
        }
        var mvc = MockMvcBuilders.standaloneSetup(controllers.toArray()).build();
        var mappings =
                mvc.getDispatcherServlet()
                        .getWebApplicationContext()
                        .getBean(RequestMappingHandlerMapping.class);
        var actual = new TreeMap<String, String>();
        mappings.getHandlerMethods()
                .forEach(
                        (mapping, handler) -> {
                            var permission = handler.getMethodAnnotation(RequiresPermissions.class);
                            String permissions =
                                    permission == null ? "" : String.join(",", permission.value());
                            String owner = handler.getBeanType().getName().split("\\.")[2];
                            for (var method : mapping.getMethodsCondition().getMethods()) {
                                for (var path : mapping.getPatternValues()) {
                                    String key = method.name() + " " + path;
                                    assertNull(
                                            actual.put(key, owner + " " + permissions),
                                            "业务端点重复: " + key);
                                }
                            }
                        });
        var expected = new TreeMap<String, String>();
        for (var endpoint :
                JsonUtils.toList(
                        new ClassPathResource("contracts/gateway-endpoint-contract.json")
                                .getContentAsString(StandardCharsets.UTF_8),
                        Endpoint.class)) {
            assertNull(
                    expected.put(
                            endpoint.method() + " " + endpoint.path(),
                            endpoint.owner() + " " + endpoint.permissions()));
        }
        assertEquals(313, expected.size());
        assertEquals(expected, actual);
    }

    public record Endpoint(
            String method,
            String path,
            String owner,
            String permissions,
            String source,
            Integer sourceLine) {}
}
