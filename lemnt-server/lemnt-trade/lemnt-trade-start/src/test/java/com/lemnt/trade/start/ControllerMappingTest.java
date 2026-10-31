package com.lemnt.trade.start;

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
        for (var definition : scanner.findCandidateComponents("com.lemnt.trade.adapter")) {
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
                GET /trade/admin/config trade:config:query
                PUT /trade/admin/config trade:config:update
                POST /trade/admin/delivery/expresses trade:delivery-express:create
                PUT /trade/admin/delivery/expresses/{id} trade:delivery-express:update
                DELETE /trade/admin/delivery/expresses/{id} trade:delivery-express:delete
                GET /trade/admin/delivery/expresses/{id} trade:delivery-express:query
                GET /trade/admin/delivery/expresses trade:delivery-express:list
                GET /trade/admin/delivery/expresses/simple trade:delivery-express:query
                POST /trade/admin/delivery/templates trade:delivery-template:create
                PUT /trade/admin/delivery/templates/{id} trade:delivery-template:update
                DELETE /trade/admin/delivery/templates/{id} trade:delivery-template:delete
                GET /trade/admin/delivery/templates/{id} trade:delivery-template:query
                GET /trade/admin/delivery/templates trade:delivery-template:list
                GET /trade/admin/delivery/templates/simple trade:delivery-template:query
                GET /trade/admin/payment-channels trade:payment-channel:list
                GET /trade/admin/payments trade:payment:list
                GET /trade/admin/payments/{no} trade:payment:query
                GET /trade/admin/payment-refunds trade:payment-refund:list
                GET /trade/admin/payment-refunds/{no} trade:payment-refund:query
                GET /trade/admin/shipments trade:shipment:list
                GET /trade/admin/shipments/{orderNo} trade:shipment:query
                GET /trade/admin/shipments/{orderNo}/tracks trade:shipment:track
                GET /trade/admin/orders/{orderNo}/after-sale-items trade:after-sale:query
                POST /trade/admin/orders/{orderNo}/after-sales trade:after-sale:create
                GET /trade/admin/after-sales trade:after-sale:list
                GET /trade/admin/after-sales/{no} trade:after-sale:query
                GET /trade/admin/after-sales/{no}/logs trade:after-sale:query
                POST /trade/app/after-sales/{no}/cancel
                POST /trade/admin/after-sales/{no}/agree trade:after-sale:agree
                POST /trade/admin/after-sales/{no}/reject trade:after-sale:reject
                POST /trade/admin/after-sales/{no}/return trade:after-sale:return
                POST /trade/admin/after-sales/{no}/receive trade:after-sale:receive
                POST /trade/admin/after-sales/{no}/refuse trade:after-sale:refuse
                POST /trade/admin/after-sales/{no}/refund trade:after-sale:refund
                GET /trade/admin/orders/summary trade:order:summary
                GET /trade/admin/orders/{orderNo}/remark trade:order:query
                PUT /trade/admin/orders/{orderNo}/remark trade:order:remark
                GET /trade/admin/orders trade:fulfillment:list
                GET /trade/admin/orders/{orderNo} trade:fulfillment:query
                GET /trade/admin/orders/{orderNo}/logs trade:fulfillment:query
                POST /trade/app/orders/{orderNo}/cancel
                POST /trade/app/orders/{orderNo}/receive
                GET /trade/app/orders/{orderNo}/payment
                GET /trade/app/orders
                GET /trade/app/orders/status-counts
                GET /trade/app/orders/{orderNo}
                GET /trade/app/orders/{orderNo}/tracks
                POST /trade/app/orders/{orderNo}/after-sales
                GET /trade/app/orders/{orderNo}/after-sale-items
                GET /trade/app/after-sales
                GET /trade/app/after-sales/{no}
                POST /trade/app/after-sales/{no}/return
                POST /trade/admin/orders/{orderNo}/shipment trade:fulfillment:ship
                POST /trade/admin/pay-apps trade:pay-app:create
                PUT /trade/admin/pay-apps/{id} trade:pay-app:update
                PUT /trade/admin/pay-apps/{id}/status trade:pay-app:update
                DELETE /trade/admin/pay-apps/{id} trade:pay-app:remove
                GET /trade/admin/pay-apps/{id} trade:pay-app:query
                GET /trade/admin/pay-apps trade:pay-app:list
                POST /trade/admin/pay-channels trade:pay-channel:create
                PUT /trade/admin/pay-channels/{id} trade:pay-channel:update
                DELETE /trade/admin/pay-channels/{id} trade:pay-channel:remove
                GET /trade/admin/pay-channels/{id} trade:pay-channel:query
                GET /trade/admin/pay-channels trade:pay-channel:list
                GET /trade/admin/pay-channels/by-app/{appId} trade:pay-channel:query
                GET /trade/admin/pay-notify-tasks trade:pay-notify:list
                GET /trade/admin/pay-notify-tasks/{id} trade:pay-notify:query
                POST /trade/admin/pay-notify-tasks/{id}/retry trade:pay-notify:retry
                GET /trade/admin/wallets trade:wallet:list
                GET /trade/admin/wallets/{memberId} trade:wallet:query
                GET /trade/admin/wallets/{memberId}/transactions trade:wallet:query
                POST /trade/admin/wallets/{memberId}/adjust trade:wallet:adjust
                GET /trade/app/wallet
                GET /trade/app/wallet/transactions
                GET /trade/admin/recharge-packages trade:recharge-package:list
                GET /trade/admin/recharge-packages/{id} trade:recharge-package:query
                POST /trade/admin/recharge-packages trade:recharge-package:create
                PUT /trade/admin/recharge-packages/{id} trade:recharge-package:update
                DELETE /trade/admin/recharge-packages/{id} trade:recharge-package:remove
                GET /trade/admin/wallet-recharges trade:wallet-recharge:list
                GET /trade/admin/wallet-recharges/{rechargeNo} trade:wallet-recharge:query
                POST /trade/admin/wallet-recharges/{rechargeNo}/confirm trade:wallet-recharge:confirm
                POST /trade/admin/wallet-recharges/{rechargeNo}/refund trade:wallet-recharge:refund
                GET /trade/app/recharge-packages
                POST /trade/app/wallet/recharges
                GET /trade/app/wallet/recharges
                GET /trade/admin/wallet-transfers trade:wallet-transfer:list
                GET /trade/admin/wallet-transfers/{transferNo} trade:wallet-transfer:query
                POST /trade/app/wallet/transfers
                GET /trade/app/wallet/transfers

                """.split("\n")));
        // 文本块会移除行尾空格，空权限与带权限的路由统一去掉行尾空白。
        var normalized = new TreeSet<String>();
        actual.forEach(route -> normalized.add(route.stripTrailing()));
        assertEquals(expected, normalized);
    }
}
