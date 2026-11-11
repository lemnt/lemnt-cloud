package com.lemnt.order.start;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.ClassPathResource;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * 配置源及切流开关的本地装配验证，不连接Nacos、数据库或消息中间件。
 */
class CommerceConfigurationTest {
    @Test
    void serviceTemplatesRequireIndependentCredentialsAndResolveSharedMqAndRegistry() {
        var urls = new HashSet<String>();
        var ports = new HashSet<Integer>();
        for (var owner : List.of("product", "promotion", "order", "pay")) {
            var sources = new MutablePropertySources();
            sources.addLast(new PropertiesPropertySource("service", properties("lemnt-" + owner)));
            sources.addLast(new PropertiesPropertySource("mq", properties("commerce-mq")));
            var resolver = new PropertySourcesPropertyResolver(sources);
            String prefix = "spring.datasource.dynamic.datasource.master.";
            // 数据库连接没有可误连的默认库或默认凭据。
            for (var field : List.of("url", "username", "password")) {
                assertThrows(
                    IllegalArgumentException.class,
                    () -> resolver.getRequiredProperty(prefix + field));
            }
            String env = owner.toUpperCase();
            sources.addFirst(
                new MapPropertySource(
                    "deployment",
                    Map.of(
                        env + "_DB_URL",
                        "jdbc:mysql://localhost/lemnt_" + owner,
                        env + "_DB_USERNAME",
                        owner + "-test-user",
                        env + "_DB_PASSWORD",
                        "fixture-password",
                        "spring.application.name",
                        "lemnt-" + owner,
                        "NACOS_NAMESPACE",
                        "migration-test",
                        "NACOS_GROUP",
                        "commerce-test",
                        "ROCKETMQ_NAME_SERVER",
                        "localhost:9876")));
            assertThat(urls.add(resolver.getRequiredProperty(prefix + "url"))).isTrue();
            assertThat(resolver.getRequiredProperty(prefix + "username"))
                .isEqualTo(owner + "-test-user");
            assertThat(resolver.getProperty("spring.datasource.dynamic.strict", Boolean.class))
                .isTrue();
            assertThat(ports.add(resolver.getProperty("dubbo.protocol.port", Integer.class)))
                .isTrue();
            assertThat(resolver.getProperty("dubbo.registry.parameters.namespace"))
                .isEqualTo("migration-test");
            assertThat(resolver.getProperty("dubbo.registry.parameters.group"))
                .isEqualTo("commerce-test");
            assertThat(resolver.getProperty("dubbo.consumer.retries", Integer.class)).isZero();
            assertThat(resolver.getProperty("rocketmq.producer.group"))
                .isEqualTo("lemnt-" + owner + "-commerce-producer-v1");
            assertThat(resolver.getProperty("commerce.mq.enabled", Boolean.class)).isFalse();
            if ("product".equals(owner)) {
                // product 出站改为RocketMQ事务消息，模板不再携带Outbox中继开关
                assertThat(resolver.getProperty("commerce.outbox.enabled", Boolean.class)).isNull();
            } else if (!"promotion".equals(owner)) {
                assertThat(resolver.getProperty("commerce.outbox.enabled", Boolean.class))
                    .isFalse();
            }
            sources.addFirst(
                new MapPropertySource("cutover", Map.of("COMMERCE_MQ_ENABLED", "true")));
            assertThat(resolver.getProperty("commerce.mq.enabled", Boolean.class)).isTrue();
        }
    }

    /**
     * 默认和显式停用均不装配副作用组件；审核后必须显式开启。
     */
    @Test
    void consumersAndOutboxRelaysRequireExplicitActivation() throws Exception {
        for (var owner : List.of("product", "promotion", "order", "pay")) {
            List<String> consumers =
                switch (owner) {
                    case "product" -> List.of(
                        "OrderPaidConsumer",
                        "OrderClosedConsumer",
                        "AfterSaleCompletedConsumer");
                    case "promotion" -> List.of(
                        "OrderPaidConsumer",
                        "OrderClosedConsumer",
                        "AfterSaleCompletedConsumer",
                        "OrderCompletedConsumer",
                        "StockPreReservedConsumer");
                    case "order" -> List.of("PaySuccessConsumer", "PayRefundSuccessConsumer");
                    default -> List.of("OrderClosedConsumer");
                };
            for (var consumer : consumers) {
                checkActivation(
                    Class.forName("com.lemnt." + owner + ".adapter.mq." + consumer),
                    "commerce.mq.enabled");
            }
            if ("order".equals(owner)) {
                checkActivation(
                    Class.forName("com.lemnt." + owner + ".start.OutboxRelayScheduler"),
                    "commerce.outbox.enabled");
            }
        }
    }

    private void checkActivation(Class<?> component, String key) {
        var runner = new ApplicationContextRunner().withUserConfiguration(component);
        for (var dependency : component.getConstructors()[0].getParameterTypes()) {
            runner = withMock(runner, dependency);
        }
        runner.run(context -> assertThat(context).doesNotHaveBean(component));
        runner.withPropertyValues(key + "=false")
            .run(context -> assertThat(context).doesNotHaveBean(component));
        runner.withPropertyValues(key + "=true")
            .run(context -> assertThat(context).hasSingleBean(component));
    }

    private <T> ApplicationContextRunner withMock(ApplicationContextRunner runner, Class<T> type) {
        return runner.withBean(type, () -> mock(type));
    }

    private Properties properties(String name) {
        var loader = new YamlPropertiesFactoryBean();
        loader.setResources(new ClassPathResource("config-sources/" + name + ".yaml"));
        return loader.getObject();
    }
}
