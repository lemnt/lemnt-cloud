package com.lemnt.system.infrastructure.org.converter;

import com.lemnt.system.infrastructure.org.dataobject.DeptDO;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DeptConverterTest {

    @Test
    void shouldPreserveStoredCreateTime() {
        LocalDateTime time = LocalDateTime.of(2026, 6, 22, 16, 39, 58);

        DeptDO dept = new DeptDO();

        dept.setCreateTime(time);

        assertThat(DeptConverter.toEntity(dept).getCreateTime()).isEqualTo(time);

    }

    @Test
    void shouldKeepMissingCreateTimeNull() {
        assertThat(DeptConverter.toEntity(new DeptDO()).getCreateTime()).isNull();

    }
}
