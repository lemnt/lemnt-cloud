package com.lemnt.system.adapter.mq.log;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.common.log.event.OperLogEvent;
import com.lemnt.common.mq.constant.MQConstants;
import com.lemnt.system.adapter.web.log.converter.OperLogResConverter;
import com.lemnt.system.application.log.converter.OperLogConverter;
import com.lemnt.system.application.log.executor.OperLogAddCmdExe;
import com.lemnt.system.domain.log.gateway.OperLogGateway;
import com.lemnt.system.domain.log.model.OperLog;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class OperLogConsumerTest {

    private final OperLogGateway gateway = mock(OperLogGateway.class);

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ConsumerConfiguration.class)
            .withBean(OperLogAddCmdExe.class, () -> new OperLogAddCmdExe(gateway));

    @Test
    void registersListenerForOperationAuditTopic() {
        contextRunner.run(context -> {
                assertThat(context).hasSingleBean(RocketMQListener.class);

                RocketMQMessageListener listener = context.getBean(RocketMQListener.class).getClass()
                .getAnnotation(RocketMQMessageListener.class);

                assertThat(listener.topic()).isEqualTo(MQConstants.Topic.OPER_LOG_EVENT);

                assertThat(listener.consumerGroup()).isEqualTo("system-operlogevent-consumer-group");

                });

    }

    @Test
    void persistsProducerEventIncludingOriginalOperationTime() {
        when(gateway.insert(any())).thenReturn(true);

        OperLogEvent event = new OperLogEvent();

        event.setTitle("岗位管理");

        event.setBusinessType(2);

        event.setMethod("PostController.update()");

        event.setRequestMethod("PUT");

        event.setOperatorType(1);

        event.setOperName("audit-test");

        event.setDeptName("测试部门");

        event.setOperUrl("/post");

        event.setOperIp("127.0.0.1");

        event.setOperParam("{\"postId\":6}");

        event.setJsonResult("{\"code\":200,\"data\":true}");

        event.setStatus(0);

        event.setErrorMsg("");

        event.setOperTime(LocalDateTime.of(2026, 10, 11, 9, 35, 44));

        event.setCostTime(37L);

        contextRunner.run(context -> {
                assertThat(context).hasSingleBean(RocketMQListener.class);

                context.getBean(RocketMQListener.class).onMessage(JsonUtils.toJsonStr(event));

                ArgumentCaptor<OperLog> saved = ArgumentCaptor.forClass(OperLog.class);

                verify(gateway).insert(saved.capture());

                assertThat(saved.getValue()).extracting(
                    OperLog::getTitle, OperLog::getBusinessType, OperLog::getMethod,
                    OperLog::getRequestMethod, OperLog::getOperatorType, OperLog::getOperName,
                    OperLog::getDeptName, OperLog::getOperUrl, OperLog::getOperIp,
                    OperLog::getOperParam, OperLog::getJsonResult, OperLog::getStatus,
                    OperLog::getErrorMsg, OperLog::getOperTime, OperLog::getCostTime)
                .containsExactly(event.getTitle(), event.getBusinessType(), event.getMethod(),
                    event.getRequestMethod(), event.getOperatorType(), event.getOperName(),
                    event.getDeptName(), event.getOperUrl(), event.getOperIp(),
                    event.getOperParam(), event.getJsonResult(), event.getStatus(),
                    event.getErrorMsg(), event.getOperTime(), event.getCostTime());

                assertThat(OperLogResConverter.toRes(OperLogConverter.toDTO(saved.getValue())).costTime())
                .isEqualTo(event.getCostTime());

                });

    }

    @Test
    void failedInsertSignalsBrokerToRetry() {
        when(gateway.insert(any())).thenReturn(false);

        contextRunner.run(context -> {
                assertThat(context).hasSingleBean(RocketMQListener.class);

                assertThatThrownBy(() -> context.getBean(RocketMQListener.class).onMessage("{}"))
                .isInstanceOf(IllegalStateException.class);

                });

    }

    @Test
    void databaseFailureIsNotSwallowed() {
        IllegalStateException failure = new IllegalStateException("测试数据库不可用");

        when(gateway.insert(any())).thenThrow(failure);

        contextRunner.run(context -> {
                assertThat(context).hasSingleBean(RocketMQListener.class);

                assertThatThrownBy(() -> context.getBean(RocketMQListener.class).onMessage("{}"))
                .isSameAs(failure);

                });

    }

    @Test
    void invalidPayloadDoesNotWriteAnEmptyAuditRecord() {
        contextRunner.run(context -> {
                assertThat(context).hasSingleBean(RocketMQListener.class);

                assertThatThrownBy(() -> context.getBean(RocketMQListener.class).onMessage("null"))
                .isInstanceOf(IllegalArgumentException.class);

                assertThatThrownBy(() -> context.getBean(RocketMQListener.class).onMessage("invalid-json"))
                .isInstanceOf(RuntimeException.class);

                verifyNoInteractions(gateway);

                });

    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan("com.lemnt.system.adapter.mq")
    static class ConsumerConfiguration {
    }
}
