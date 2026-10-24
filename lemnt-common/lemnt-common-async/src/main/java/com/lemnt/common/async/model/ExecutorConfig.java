package com.lemnt.common.async.model;

import lombok.Data;

/**
 * 线程池配置
 *
 * @author wangxx
 */
@Data
public class ExecutorConfig {

    /**
     * 核心线程数
     */
    private int corePoolSize = 8;

    /**
     * 最大线程数
     */
    private int maxPoolSize = 16;

    /**
     * 队列容量
     */
    private int queueCapacity = 1000;

    /**
     * 线程名前缀
     */
    private String threadNamePrefix = "async-task-";
}
