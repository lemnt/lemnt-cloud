package com.lemnt.system.adapter.web.org;

import com.lemnt.system.adapter.web.org.converter.DeptResConverter;
import com.lemnt.system.adapter.web.org.converter.PostResConverter;
import com.lemnt.system.application.org.dto.DeptDTO;
import com.lemnt.system.application.org.dto.PostDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrgTimeContractTest {

    @Test
    void shouldExposeSourceCreateTimes() {
        LocalDateTime time = LocalDateTime.of(2026, 6, 22, 16, 39, 58);
        DeptDTO dept = new DeptDTO();
        dept.setCreateTime(time);
        PostDTO post = new PostDTO();
        post.setCreateTime(time);

        assertThat(DeptResConverter.toRes(dept).createTime()).isEqualTo(time);
        assertThat(PostResConverter.toRes(post).createTime()).isEqualTo(time);
    }

    @Test
    void shouldKeepMissingCreateTimesNull() {
        assertThat(DeptResConverter.toRes(new DeptDTO()).createTime()).isNull();
        assertThat(PostResConverter.toRes(new PostDTO()).createTime()).isNull();
    }
}
