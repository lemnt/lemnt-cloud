package com.lemnt.common.start.jackson;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.math.BigDecimal;

public class BigDecimalSerializer extends ValueSerializer<BigDecimal> {

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializationContext provider) {
        // 序列化为数字，不是字符串
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeNumber(value);
        }
    }
}
