package com.lemnt.system.application.org.converter;

import com.lemnt.system.domain.org.model.Dept;
import com.lemnt.system.domain.org.model.Post;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrgTimeConverterTest {

    @Test
    void shouldPreserveDeptAndChildCreateTimes() {
        LocalDateTime time = LocalDateTime.of(2026, 6, 22, 16, 39, 58);

        Dept child = new Dept().setCreateTime(time.plusSeconds(1));

        Dept dept = new Dept().setCreateTime(time).setChildren(List.of(child));

        var result = DeptConverter.toDTO(dept);

        assertThat(result.getCreateTime()).isEqualTo(time);

        assertThat(result.getChildren().get(0).getCreateTime()).isEqualTo(child.getCreateTime());

    }

    @Test
    void shouldPreservePostCreateTime() {
        LocalDateTime time = LocalDateTime.of(2026, 6, 22, 16, 39, 58);

        Post post = new Post();

        post.setCreateTime(time);

        assertThat(PostConverter.toDTO(post).getCreateTime()).isEqualTo(time);

    }

    @Test
    void shouldKeepMissingCreateTimesNull() {
        assertThat(DeptConverter.toDTO(new Dept()).getCreateTime()).isNull();

        assertThat(PostConverter.toDTO(new Post()).getCreateTime()).isNull();

    }
}
