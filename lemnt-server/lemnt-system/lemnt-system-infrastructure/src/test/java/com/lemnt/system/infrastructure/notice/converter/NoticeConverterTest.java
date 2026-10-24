package com.lemnt.system.infrastructure.notice.converter;

import com.lemnt.system.domain.notice.model.Notice;
import com.lemnt.system.infrastructure.notice.dataobject.NoticeDO;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证公告备注在写入和读取转换中原样传递，包含清空值。 */
class NoticeConverterTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "公告备注验收")
    void shouldPreserveRemarkWhenSaving(String remark) {
        Notice notice = new Notice();
        notice.setRemark(remark);

        assertThat(NoticeConverter.toDO(notice).getRemark()).isEqualTo(remark);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "公告备注验收")
    void shouldPreserveRemarkWhenReading(String remark) {
        NoticeDO notice = new NoticeDO();
        notice.setRemark(remark);

        assertThat(NoticeConverter.toEntity(notice).getRemark()).isEqualTo(remark);
    }
}
