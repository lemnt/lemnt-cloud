package com.lemnt.common.excel.constant;

import lombok.Getter;

/**
 * 任务执行状态
 *
 * @author wangxx
 */
@Getter
public enum TaskStatusEnum {

    WAITING(0), DOING(1), SUCCESS(2), FAIL(3), STOP(4), PART_FAIL(5);

    private int status;

    TaskStatusEnum(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

}
