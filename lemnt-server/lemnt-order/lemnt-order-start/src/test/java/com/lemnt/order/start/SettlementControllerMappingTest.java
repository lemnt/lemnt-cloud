package com.lemnt.order.start;

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

class SettlementControllerMappingTest {

    /**
     * 全量注册拆分后的接口，核对原有路径、HTTP 方法和权限，并检测映射冲突。
     */
    @Test
    void businessControllersPreserveHttpRoutesAndPermissions() throws Exception {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        var controllers = new ArrayList<Object>();
        for (var owner : Arrays.asList("order", "product", "promotion"))
            for (var definition :
                scanner.findCandidateComponents("com.lemnt." + owner + ".adapter")) {
                var type = Class.forName(definition.getBeanClassName());
                var constructor = type.getConstructors()[0];
                var dependencies =
                    Arrays.stream(constructor.getParameterTypes()).map(t -> mock(t)).toArray();
                controllers.add(constructor.newInstance(dependencies));
            }
        var mvc = MockMvcBuilders.standaloneSetup(controllers.toArray()).build();
        var mappings =
            mvc.getDispatcherServlet()
                .getWebApplicationContext()
                .getBean(RequestMappingHandlerMapping.class);
        var actual = new TreeSet<String>();
        mappings.getHandlerMethods()
            .forEach(
                (mapping, handler) -> {
                    var permission = handler.getMethodAnnotation(RequiresPermissions.class);
                    var permissions =
                        permission == null ? "" : String.join(",", permission.value());
                    for (var method : mapping.getMethodsCondition().getMethods()) {
                        for (var path : mapping.getPatternValues()) {
                            if (!path.startsWith("/mall/")
                                && !handler.getBeanType().getName().contains(".product.adapter"))
                                continue;
                            actual.add(method.name() + " " + path + " " + permissions);
                        }
                    }
                });
        var expected =
            new TreeSet<>(
                Arrays.asList(
                    """
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
                        GET /mall/promotion/banners mall:promotion:list
                        GET /mall/promotion/banners/{id} mall:promotion:query
                        POST /mall/promotion/banners mall:promotion:create
                        PUT /mall/promotion/banners/{id} mall:promotion:update
                        DELETE /mall/promotion/banners/{id} mall:promotion:delete
                        GET /mall/app/banners
                        PUT /mall/app/banners/{id}/browse
                        GET /mall/promotion/kefu-conversations mall:promotion:list
                        GET /mall/promotion/kefu-conversations/{id} mall:promotion:query
                        GET /mall/promotion/kefu-conversations/{id}/messages mall:promotion:query
                        POST /mall/promotion/kefu-conversations/{id}/messages mall:promotion:create
                        PUT /mall/promotion/kefu-conversations/{id}/read mall:promotion:update
                        PUT /mall/promotion/kefu-conversations/{id}/pinned mall:promotion:update
                        DELETE /mall/promotion/kefu-conversations/{id} mall:promotion:delete
                        GET /mall/app/kefu/conversation
                        GET /mall/app/kefu/messages
                        POST /mall/app/kefu/messages
                        PUT /mall/app/kefu/read
                        GET /mall/promotion/diy-templates mall:promotion:list
                        GET /mall/promotion/diy-templates/{id} mall:promotion:query
                        POST /mall/promotion/diy-templates mall:promotion:create
                        PUT /mall/promotion/diy-templates/{id} mall:promotion:update
                        PUT /mall/promotion/diy-templates/{id}/use mall:promotion:update
                        PUT /mall/promotion/diy-templates/{id}/property mall:promotion:update
                        DELETE /mall/promotion/diy-templates/{id} mall:promotion:delete
                        GET /mall/promotion/diy-pages mall:promotion:list
                        GET /mall/promotion/diy-pages/{id} mall:promotion:query
                        POST /mall/promotion/diy-pages mall:promotion:create
                        PUT /mall/promotion/diy-pages/{id} mall:promotion:update
                        PUT /mall/promotion/diy-pages/{id}/property mall:promotion:update
                        DELETE /mall/promotion/diy-pages/{id} mall:promotion:delete
                        GET /mall/app/diy/template/used
                        GET /mall/app/diy/template/{id}
                        GET /mall/app/diy/page/{id}
                        GET /mall/app/promotion-hub/sections
                        GET /mall/app/promotion-hub/discount/{id}
                        GET /mall/app/promotion-hub/reward/{id}
                        DELETE /mall/app/cart/{productId}
                        DELETE /brands/{id} product:brand:remove
                        DELETE /categories/{id} product:category:remove
                        GET /mall/admin/coupons mall:coupon:list
                        GET /mall/admin/coupon-templates mall:coupon-template:list
                        GET /mall/admin/coupon-templates/public-claim mall:coupon-template:list
                        GET /mall/admin/members/{memberId}/coupons mall:coupon:list
                        GET /mall/app/cart
                        GET /app/favorites
                        GET /app/favorites/exists
                        GET /app/favorites/count
                        POST /app/favorites
                        DELETE /app/favorites/{spuId:[1-9][0-9]*}
                        GET /app/browse-historys
                        POST /app/browse-historys
                        DELETE /app/browse-historys
                        DELETE /app/browse-historys/clear
                        GET /favorites product:favorite:list
                        GET /browse-historys product:browse-history:list
                        GET /app/categories
                        GET /mall/app/checkout/requests/{requestNo}
                        GET /mall/app/coupon-center
                        GET /mall/app/coupon-wallet
                        GET /mall/app/coupons
                        GET /mall/app/orders/{orderNo}
                        GET /app/products
                        GET /app/products/{id}
                        GET /brands product:brand:list
                        GET /brands/{id} product:brand:query
                        GET /categories product:category:list
                        GET /categories/{id} product:category:query
                        DELETE /specs/{id} product:spec:remove
                        DELETE /spec-values/{id} product:spec-value:remove
                        DELETE /spus/{id} product:spu:remove
                        GET /specs product:spec:list
                        GET /specs/simple product:spec:query
                        GET /specs/{id} product:spec:query
                        GET /spec-values product:spec-value:list
                        GET /spec-values/simple product:spec-value:query
                        GET /spec-values/{id} product:spec-value:query
                        GET /spus/count product:spu:list
                        GET /spus/export product:spu:export
                        GET /spus/simple product:spu:query
                        GET /spus/batch product:spu:query
                        GET /brands/page product:brand:list
                        GET /brands/simple product:brand:query
                        GET /categories/tree product:category:list
                        POST /comments product:comment:create
                        GET /comments product:comment:list
                        GET /comments/{id} product:comment:query
                        POST /specs product:spec:add
                        POST /spec-values product:spec-value:add
                        POST /spus/{id}/recycle product:spu:edit
                        POST /spus/{id}/restore product:spu:edit
                        GET /app/comments
                        POST /mall/app/comments
                        PUT /specs/{id} product:spec:edit
                        PUT /spec-values/{id} product:spec-value:edit
                        PUT /comments/{id}/reply product:comment:reply
                        PUT /comments/{id}/visible product:comment:edit
                        GET /spus product:spu:list
                        GET /spus/{id} product:spu:query
                        POST /mall/admin/coupon-templates mall:coupon-template:add
                        POST /mall/admin/members/{memberId}/coupons/{templateId} mall:coupon:issue
                        POST /mall/app/checkout
                        POST /mall/app/checkout/abandon
                        POST /mall/app/checkout/quote
                        POST /mall/app/coupon-center/{templateId}/claim
                        POST /brands product:brand:add
                        POST /categories product:category:add
                        POST /spus product:spu:add
                        PUT /mall/admin/coupon-templates/{id} mall:coupon-template:edit
                        DELETE /mall/admin/coupon-templates/{id} mall:coupon-template:remove
                        DELETE /mall/admin/members/{memberId}/coupons/{couponId} mall:coupon:remove
                        PUT /mall/admin/coupon-templates/{id}/enabled mall:coupon-template:edit
                        PUT /mall/admin/coupon-templates/{id}/public-claim mall:coupon-template:edit
                        PUT /mall/app/cart/{productId}
                        PUT /brands/{id} product:brand:edit
                        PUT /categories/{id} product:category:edit
                        PUT /spus/{id} product:spu:edit
                        PUT /spus/{id}/shelf product:spu:edit
                        """
                        .split("\n")));
        // 文本块会移除行尾空格，空权限与带权限的路由统一去掉行尾空白。
        var normalized = new TreeSet<String>();
        actual.forEach(route -> normalized.add(route.stripTrailing()));
        assertEquals(expected, normalized);
    }
}
