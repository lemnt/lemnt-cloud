package com.lemnt.system.infrastructure.log.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLDataException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 验证实际 Mapper XML 使用数值处理器读取毫秒耗时，覆盖零、长整型及空值。 */
class OperLogCostTimeMapperTest {

    @ParameterizedTest
    @ValueSource(longs = {0L, 37L, 2147483648L})
    void readsBigintAsMilliseconds(long milliseconds) throws Exception {
        ResultSet resultSet = numericResultSet();
        when(resultSet.getLong("cost_time")).thenReturn(milliseconds);
        assertThat(costTimeMapping().getTypeHandler().getResult(resultSet, "cost_time"))
                .isEqualTo(milliseconds);
    }

    @Test
    void preservesNullDuration() throws Exception {
        ResultSet resultSet = numericResultSet();
        when(resultSet.wasNull()).thenReturn(true);
        assertThat(costTimeMapping().getTypeHandler().getResult(resultSet, "cost_time"))
                .isNull();
    }

    private ResultSet numericResultSet() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        // 与 MySQL 对 BIGINT 强制日期转换的失败行为一致。
        when(resultSet.getObject("cost_time", LocalDateTime.class))
                .thenThrow(new SQLDataException("Unsupported conversion from LONG to java.time.LocalDateTime"));
        return resultSet;
    }

    private ResultMapping costTimeMapping() throws Exception {
        Configuration configuration = new Configuration();
        String resource = "mapper/OperLogMapper.xml";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        return configuration.getResultMap(OperLogMapper.class.getName() + ".SysOperLogResult")
                .getResultMappings().stream()
                .filter(mapping -> "costTime".equals(mapping.getProperty()))
                .findFirst().orElseThrow();
    }
}
