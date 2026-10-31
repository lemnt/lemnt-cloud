package com.lemnt.system.adapter.web.config;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.system.adapter.web.config.converter.DictDataResConverter;
import com.lemnt.system.application.config.converter.DictDataConverter;
import com.lemnt.system.domain.config.model.DictData;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/** 从领域到 HTTP 响应完整保留备注，不伪造缺失备注。 */
class DictDataRemarkContractTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "字典数据备注")
    void preservesRemarkThroughBothConverters(String remark) {
        DictData entity = new DictData();

        entity.setRemark(remark);

        var dto = DictDataConverter.toDTO(entity);

        assertThat(dto).extracting("remark").isEqualTo(remark);

        var response = DictDataResConverter.toRes(dto);

        assertThat(response).extracting("remark").isEqualTo(remark);

        if (remark != null) {
            assertThat(JsonUtils.toJsonStr(response)).contains("\"remark\":\"" + remark + "\"");

        }
    }
}
