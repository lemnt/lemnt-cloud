package com.lemnt.system.infrastructure.org.converter;

import com.lemnt.system.domain.org.model.Post;
import com.lemnt.system.infrastructure.org.dataobject.PostDO;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证岗位备注在持久化与查询转换中原样传递。
 */
class PostConverterTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "岗位备注验收")
    void shouldPreserveRemarkWhenSaving(String remark) {
        Post post = new Post();

        post.setRemark(remark);

        assertThat(PostConverter.toDO(post).getRemark()).isEqualTo(remark);

    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "岗位备注验收")
    void shouldPreserveRemarkWhenReading(String remark) {
        PostDO post = new PostDO();

        post.setRemark(remark);

        assertThat(PostConverter.toEntity(post).getRemark()).isEqualTo(remark);

    }
}
