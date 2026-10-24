package com.lemnt.system.infrastructure.org.mapper;

import com.lemnt.system.infrastructure.org.dataobject.PostDO;
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

/** 验证重复编码查询绑定编码，修改排除自身且不跳过停用记录。 */
class PostCodeUniqueMapperTest {

    @BeforeAll
    static void initializeTableMetadata() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        assistant.setCurrentNamespace("test");
        TableInfoHelper.initTableInfo(assistant, PostDO.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldCheckCodeAndExcludeOnlyCurrentPost(boolean editing) {
        PostMapper mapper = mock(PostMapper.class, CALLS_REAL_METHODS);
        doReturn(null).when(mapper).selectOne(any());

        if (editing) {
            mapper.checkPostCodeUnique("test", 5L);
        } else {
            mapper.checkPostCodeUnique("test");
        }

        ArgumentCaptor<LambdaQueryWrapper<PostDO>> captor = ArgumentCaptor.captor();
        verify(mapper).selectOne(captor.capture());
        LambdaQueryWrapper<PostDO> query = captor.getValue();
        assertThat(query.getSqlSegment()).contains("post_code =").doesNotContain("status");
        if (editing) {
            assertThat(query.getSqlSegment()).contains("post_id <>");
            assertThat(query.getParamNameValuePairs().values()).containsExactlyInAnyOrder("test", 5L);
        } else {
            assertThat(query.getSqlSegment()).doesNotContain("post_id <>");
            assertThat(query.getParamNameValuePairs().values()).containsExactly("test");
        }
    }
}
