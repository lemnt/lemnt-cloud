package com.lemnt.common.mq.consumer;

import com.lemnt.common.core.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 基础MQ 消费者
 *
 * @author wangxx
 * @version 1.0
 * @date 2025-11-10 08:55
 */
@Slf4j
public abstract class BaseConsumer<T> implements RocketMQListener<String> {

    /**
     * 元素类型
     */
    protected final Class<T> elementClass;

    @SuppressWarnings("unchecked")
    public BaseConsumer() {
        Type superClass = getClass().getGenericSuperclass();
        if (!(superClass instanceof ParameterizedType pt)) {
            throw new IllegalStateException("父类不是泛型类型，请检查继承关系！");
        }
        Type actualType = pt.getActualTypeArguments()[0];

        if (actualType instanceof Class<?> clazz) {
            elementClass = (Class<T>) clazz;
        } else if (actualType instanceof ParameterizedType ptActual) {
            Type rawType = ptActual.getRawType();
            if (!(rawType instanceof Class)) {
                throw new IllegalStateException("无法解析集合类型: " + rawType);
            }
            Type[] args = ptActual.getActualTypeArguments();
            if (args.length != 1) {
                throw new IllegalStateException("只支持单泛型集合: " + actualType);
            }
            if (!(args[0] instanceof Class)) {
                throw new IllegalStateException("无法解析集合元素类型: " + args[0]);
            }
            elementClass = (Class<T>) args[0];
        } else {
            throw new IllegalStateException("无法解析泛型类型: " + actualType);
        }
    }

    @Override
    public void onMessage(String message) {
        try {
            log.debug("MQ 消息反序列化, message={}", message);
            if (message.trim().startsWith("[")) {
                List<T> list = JsonUtils.toList(message, elementClass);
                this.handleList(list);
            } else {
                this.handle(JsonUtils.toBean(message, elementClass));
            }
        } catch (Exception e) {
            log.error("MQ 消息消费失败失败, message={}", message, e);
        }
    }

    /**
     * 处理消息
     */
    protected void handle(T message) {
    }

    /**
     * 处理批量消息
     */
    protected void handleList(List<T> messages) {
    }
}
