package com.lemnt.common.kafka.model;

import lombok.Data;

import java.io.Serializable;

/**
 * kafka消息
 *
 * @author wangxx
 */
@Data
public class KafkaMessage<T> implements Serializable {

    private static final long serialVersionUID = 7298559778267166984L;

    private String topic;

    private String key;

    private T data;
}
