package com.lemnt.member.start;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 迁移前不能提前消费订单权益；显式启用后才装配会员业务消费者。
 */
class CommerceActivationTest {
    @Test
    void orderBenefitsConsumersRequireExplicitCutoverActivation() throws Exception {
        for (var name :
            List.of(
                "OrderPaidConsumer",
                "OrderClosedConsumer",
                "OrderShippedConsumer",
                "AfterSaleCompletedConsumer")) {
            var component = Class.forName("com.lemnt.member.adapter.mq." + name);
            var runner = new ApplicationContextRunner().withUserConfiguration(component);
            for (var type : component.getConstructors()[0].getParameterTypes()) {
                runner = withMock(runner, type);
            }
            runner.run(context -> assertThat(context).doesNotHaveBean(component));
            runner.withPropertyValues("commerce.mq.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(component));
            runner.withPropertyValues("commerce.mq.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(component));
        }
    }

    private <T> ApplicationContextRunner withMock(ApplicationContextRunner runner, Class<T> type) {
        return runner.withBean(type, () -> mock(type));
    }
}
