package com.lemnt.mall.start;

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
        for (var definition : scanner.findCandidateComponents("com.lemnt.mall.adapter")) {
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
                DELETE /mall/admin/members/{memberId}/cart/{productId} mall:cart:edit
                DELETE /mall/app/cart/{productId}
                DELETE /mall/brands/{id} mall:brand:remove
                DELETE /mall/categories/{id} mall:category:remove
                GET /mall/admin/checkouts/{orderNo} mall:checkout:query
                GET /mall/admin/coupon-templates mall:coupon-template:list
                GET /mall/admin/coupon-templates/public-claim mall:coupon-template:list
                GET /mall/admin/members/{memberId}/cart mall:cart:query
                GET /mall/admin/members/{memberId}/coupons mall:coupon:list
                GET /mall/admin/promotions mall:promotion:list
                GET /mall/admin/promotions/{id} mall:promotion:query
                GET /mall/app/cart
                GET /mall/app/categories
                GET /mall/app/checkout/requests/{requestNo}
                GET /mall/app/coupon-center
                GET /mall/app/coupon-wallet
                GET /mall/app/coupons
                GET /mall/app/orders/{orderNo}
                GET /mall/app/products
                GET /mall/app/products/{id}
                GET /mall/brands mall:brand:list
                GET /mall/brands/{id} mall:brand:query
                GET /mall/categories mall:category:list
                GET /mall/categories/{id} mall:category:query
                GET /mall/products/{id} mall:product:query
                GET /mall/spus mall:spu:list
                GET /mall/spus/{id} mall:spu:query
                POST /mall/admin/coupon-templates mall:coupon-template:add
                POST /mall/admin/members/{memberId}/checkout mall:checkout:create
                POST /mall/admin/members/{memberId}/checkout/quote mall:checkout:quote
                POST /mall/admin/members/{memberId}/coupons/{templateId} mall:coupon:issue
                POST /mall/admin/promotions mall:promotion:create
                POST /mall/admin/promotions/{id}/publish mall:promotion:publish
                POST /mall/app/cart/{productId}/add
                POST /mall/app/checkout
                POST /mall/app/checkout/abandon
                POST /mall/app/checkout/quote
                POST /mall/app/coupon-center/{templateId}/claim
                POST /mall/brands mall:brand:add
                POST /mall/categories mall:category:add
                POST /mall/orders mall:order:create
                POST /mall/products mall:product:add
                POST /mall/skus/{id}/stock mall:sku:stock
                POST /mall/spus mall:spu:add
                PUT /mall/admin/coupon-templates/{id}/enabled mall:coupon-template:edit
                PUT /mall/admin/coupon-templates/{id}/public-claim mall:coupon-template:edit
                PUT /mall/admin/members/{memberId}/cart/{productId} mall:cart:edit
                PUT /mall/admin/promotions/{id}/draft mall:promotion:update
                PUT /mall/admin/promotions/{id}/state mall:promotion:state
                PUT /mall/app/cart/{productId}
                PUT /mall/brands/{id} mall:brand:edit
                PUT /mall/categories/{id} mall:category:edit
                PUT /mall/products/{id}/shelf mall:product:edit
                PUT /mall/skus/{id} mall:sku:edit
                PUT /mall/spus/{id} mall:spu:edit
                PUT /mall/spus/{id}/shelf mall:spu:edit

                """.split("\n")));
        // 文本块会移除行尾空格，空权限与带权限的路由统一去掉行尾空白。
        var normalized = new TreeSet<String>();
        actual.forEach(route -> normalized.add(route.stripTrailing()));
        assertEquals(expected, normalized);
    }
}
