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
                GET /mall/promotion/reward-activities mall:promotion:list
                GET /mall/promotion/reward-activities/{id} mall:promotion:query
                POST /mall/promotion/reward-activities mall:promotion:create
                PUT /mall/promotion/reward-activities/{id} mall:promotion:update
                PUT /mall/promotion/reward-activities/{id}/close mall:promotion:update
                DELETE /mall/promotion/reward-activities/{id} mall:promotion:delete
                GET /mall/promotion/discount-activities mall:promotion:list
                GET /mall/promotion/discount-activities/{id} mall:promotion:query
                POST /mall/promotion/discount-activities mall:promotion:create
                PUT /mall/promotion/discount-activities/{id} mall:promotion:update
                PUT /mall/promotion/discount-activities/{id}/close mall:promotion:update
                DELETE /mall/promotion/discount-activities/{id} mall:promotion:delete
                GET /mall/promotion/seckill-configs mall:promotion:list
                GET /mall/promotion/seckill-configs/{id} mall:promotion:query
                POST /mall/promotion/seckill-configs mall:promotion:create
                PUT /mall/promotion/seckill-configs/{id} mall:promotion:update
                PUT /mall/promotion/seckill-configs/{id}/status mall:promotion:update
                DELETE /mall/promotion/seckill-configs/{id} mall:promotion:delete
                GET /mall/promotion/seckill-configs/list mall:promotion:query
                GET /mall/promotion/seckill-configs/simple-list
                GET /mall/promotion/seckill-activities mall:promotion:list
                GET /mall/promotion/seckill-activities/{id} mall:promotion:query
                POST /mall/promotion/seckill-activities mall:promotion:create
                PUT /mall/promotion/seckill-activities/{id} mall:promotion:update
                PUT /mall/promotion/seckill-activities/{id}/close mall:promotion:update
                DELETE /mall/promotion/seckill-activities/{id} mall:promotion:delete
                GET /mall/app/seckill/configs
                GET /mall/app/seckill/now
                GET /mall/app/seckill/activities
                GET /mall/app/seckill/activities/{id}
                GET /mall/promotion/combination-activities mall:promotion:list
                GET /mall/promotion/combination-activities/{id} mall:promotion:query
                POST /mall/promotion/combination-activities mall:promotion:create
                PUT /mall/promotion/combination-activities/{id} mall:promotion:update
                PUT /mall/promotion/combination-activities/{id}/close mall:promotion:update
                DELETE /mall/promotion/combination-activities/{id} mall:promotion:delete
                GET /mall/promotion/combination-records mall:promotion:list
                GET /mall/app/combination/activities
                GET /mall/app/combination/activities/{id}
                GET /mall/app/combination/activities/{id}/heads
                GET /mall/app/combination/records
                GET /mall/app/combination/records/{id}
                GET /mall/app/combination/summary
                GET /mall/promotion/bargain-activities mall:promotion:list
                GET /mall/promotion/bargain-activities/{id} mall:promotion:query
                POST /mall/promotion/bargain-activities mall:promotion:create
                PUT /mall/promotion/bargain-activities/{id} mall:promotion:update
                PUT /mall/promotion/bargain-activities/{id}/close mall:promotion:update
                DELETE /mall/promotion/bargain-activities/{id} mall:promotion:delete
                GET /mall/promotion/bargain-records mall:promotion:list
                GET /mall/promotion/bargain-helps mall:promotion:list
                GET /mall/app/bargain/activities
                GET /mall/app/bargain/activities/{id}
                GET /mall/app/bargain/activities/{id}/records
                POST /mall/app/bargain/records
                GET /mall/app/bargain/records
                GET /mall/app/bargain/records/{id}
                GET /mall/app/bargain/records/{id}/helps
                POST /mall/app/bargain/helps
                GET /mall/promotion/point-activities mall:promotion:list
                GET /mall/promotion/point-activities/{id} mall:promotion:query
                POST /mall/promotion/point-activities mall:promotion:create
                PUT /mall/promotion/point-activities/{id} mall:promotion:update
                PUT /mall/promotion/point-activities/{id}/close mall:promotion:update
                DELETE /mall/promotion/point-activities/{id} mall:promotion:delete
                GET /mall/app/point-mall/activities
                GET /mall/app/point-mall/activities/{id}
                POST /mall/app/seckill/orders
                POST /mall/app/combination/orders
                POST /mall/app/bargain/orders
                POST /mall/app/point-mall/orders
                POST /mall/app/point-mall/orders/batch
                DELETE /mall/app/cart/{productId}
                DELETE /mall/brands/{id} mall:brand:remove
                DELETE /mall/categories/{id} mall:category:remove
                GET /mall/admin/coupons mall:coupon:list
                GET /mall/admin/coupon-templates mall:coupon-template:list
                GET /mall/admin/coupon-templates/public-claim mall:coupon-template:list
                GET /mall/admin/members/{memberId}/coupons mall:coupon:list
                GET /mall/app/cart
                GET /mall/app/favorites
                GET /mall/app/favorites/exists
                GET /mall/app/favorites/count
                POST /mall/app/favorites
                DELETE /mall/app/favorites/{spuId:[1-9][0-9]*}
                GET /mall/app/browse-history
                POST /mall/app/browse-history
                DELETE /mall/app/browse-history
                DELETE /mall/app/browse-history/clear
                GET /mall/product-favorites mall:product-favorite:list
                GET /mall/product-browse-history mall:product-history:list
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
                DELETE /mall/specs/{id} mall:spec:remove
                DELETE /mall/spec-values/{id} mall:spec-value:remove
                DELETE /mall/spus/{id} mall:spu:remove
                GET /mall/specs mall:spec:list
                GET /mall/specs/simple mall:spec:query
                GET /mall/specs/{id} mall:spec:query
                GET /mall/spec-values mall:spec-value:list
                GET /mall/spec-values/simple mall:spec-value:query
                GET /mall/spec-values/{id} mall:spec-value:query
                GET /mall/spus/count mall:spu:list
                GET /mall/spus/export mall:spu:export
                GET /mall/spus/simple mall:spu:query
                GET /mall/spus/batch mall:spu:query
                GET /mall/brands/page mall:brand:list
                GET /mall/brands/simple mall:brand:query
                GET /mall/categories/tree mall:category:list
                POST /mall/comments mall:comment:create
                GET /mall/comments mall:comment:list
                GET /mall/comments/{id} mall:comment:query
                POST /mall/specs mall:spec:add
                POST /mall/spec-values mall:spec-value:add
                POST /mall/spus/{id}/recycle mall:spu:edit
                POST /mall/spus/{id}/restore mall:spu:edit
                GET /mall/app/comments
                POST /mall/app/comments
                PUT /mall/specs/{id} mall:spec:edit
                PUT /mall/spec-values/{id} mall:spec-value:edit
                PUT /mall/comments/{id}/reply mall:comment:reply
                PUT /mall/comments/{id}/visible mall:comment:edit
                GET /mall/spus mall:spu:list
                GET /mall/spus/{id} mall:spu:query
                POST /mall/admin/coupon-templates mall:coupon-template:add
                POST /mall/admin/members/{memberId}/coupons/{templateId} mall:coupon:issue
                POST /mall/app/checkout
                POST /mall/app/checkout/abandon
                POST /mall/app/checkout/quote
                POST /mall/app/coupon-center/{templateId}/claim
                POST /mall/brands mall:brand:add
                POST /mall/categories mall:category:add
                POST /mall/spus mall:spu:add
                PUT /mall/admin/coupon-templates/{id} mall:coupon-template:edit
                DELETE /mall/admin/coupon-templates/{id} mall:coupon-template:remove
                DELETE /mall/admin/members/{memberId}/coupons/{couponId} mall:coupon:remove
                PUT /mall/admin/coupon-templates/{id}/enabled mall:coupon-template:edit
                PUT /mall/admin/coupon-templates/{id}/public-claim mall:coupon-template:edit
                PUT /mall/app/cart/{productId}
                PUT /mall/brands/{id} mall:brand:edit
                PUT /mall/categories/{id} mall:category:edit
                PUT /mall/spus/{id} mall:spu:edit
                PUT /mall/spus/{id}/shelf mall:spu:edit
                """.split("\n")));
        // 文本块会移除行尾空格，空权限与带权限的路由统一去掉行尾空白。
        var normalized = new TreeSet<String>();
        actual.forEach(route -> normalized.add(route.stripTrailing()));
        assertEquals(expected, normalized);
    }
}
