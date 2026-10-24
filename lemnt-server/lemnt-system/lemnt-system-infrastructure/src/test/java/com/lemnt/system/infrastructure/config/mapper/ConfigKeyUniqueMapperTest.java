package com.lemnt.system.infrastructure.config.mapper;

import com.lemnt.system.infrastructure.config.dataobject.ConfigDO;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** 修改查询排除自身，不按内置标识跳过冲突参数。 */
class ConfigKeyUniqueMapperTest {
    @BeforeAll
    static void initializeTable() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        assistant.setCurrentNamespace("test");
        TableInfoHelper.initTableInfo(assistant, ConfigDO.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void checksKeyAndExcludesOnlySelf(boolean editing) {
        ConfigMapper mapper = mock(ConfigMapper.class, CALLS_REAL_METHODS);
        doReturn(null).when(mapper).selectOne(any());
        if (editing) mapper.checkConfigKeyUnique("test", 2L);
        else mapper.checkConfigKeyUnique("test");
        ArgumentCaptor<LambdaQueryWrapper<ConfigDO>> captor = ArgumentCaptor.captor();
        verify(mapper).selectOne(captor.capture());
        var query = captor.getValue();
        assertThat(query.getSqlSegment()).contains("config_key =").doesNotContain("config_type");
        if (editing) {
            assertThat(query.getSqlSegment()).contains("config_id <>");
            assertThat(query.getParamNameValuePairs().values()).containsExactlyInAnyOrder("test", 2L);
        } else {
            assertThat(query.getSqlSegment()).doesNotContain("config_id <>");
            assertThat(query.getParamNameValuePairs().values()).containsExactly("test");
        }
    }
}
