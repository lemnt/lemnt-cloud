package com.lemnt.system.infrastructure.config.mapper;

import com.lemnt.system.domain.config.model.Config;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** 实际 XML 对日期范围只做空值检查，支持单边、双边以及无日期查询。 */
class ConfigDateFilterTest {
    @ParameterizedTest
    @CsvSource({
            "false,false", "true,false", "false,true", "true,true"
            })
    void bindsLocalDateTimeWithoutComparingStrings(boolean start, boolean end) throws Exception {
        Configuration configuration = new Configuration();

        String resource = "mapper/ConfigMapper.xml";

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();

        }
        Config query = new Config();

        query.setConfigName("联调测试");

        if (start) query.setBeginTime(LocalDateTime.of(2026, 10, 10, 0, 0));

        if (end) query.setEndTime(LocalDateTime.of(2026, 10, 10, 23, 59, 59));

        var bound = configuration.getMappedStatement(ConfigMapper.class.getName() + ".selectPage")
                .getBoundSql(Map.of("qry", query));

        String sql = bound.getSql().replaceAll("\\s+", " ");

        assertThat(sql).contains("config_name LIKE");

        assertThat(sql.contains("create_time >=")).isEqualTo(start);

        assertThat(sql.contains("create_time <=")).isEqualTo(end);

        assertThat(bound.getParameterMappings()).extracting("property")
                .hasSize(1 + (start ? 1 : 0) + (end ? 1 : 0));

    }
}
