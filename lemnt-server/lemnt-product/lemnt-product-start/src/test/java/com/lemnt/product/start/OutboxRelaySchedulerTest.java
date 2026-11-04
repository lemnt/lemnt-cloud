package com.lemnt.product.start;

import com.lemnt.product.application.event.service.OutboxRelayService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Relay默认停用，切流审核通过后才显式发送原Outbox记录。 */
class OutboxRelaySchedulerTest {
    @Test
    void relayRequiresExplicitCutoverActivation() {
        var runner =
                new ApplicationContextRunner()
                        .withUserConfiguration(OutboxRelayScheduler.class)
                        .withBean(OutboxRelayService.class, () -> mock(OutboxRelayService.class));
        runner.run(context -> assertThat(context).doesNotHaveBean(OutboxRelayScheduler.class));
        runner.withPropertyValues("commerce.outbox.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(OutboxRelayScheduler.class));
        runner.withPropertyValues("commerce.outbox.enabled=true")
                .run(context -> assertThat(context).hasSingleBean(OutboxRelayScheduler.class));
    }
}
