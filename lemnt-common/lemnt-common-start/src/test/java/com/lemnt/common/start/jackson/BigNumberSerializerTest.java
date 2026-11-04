package com.lemnt.common.start.jackson;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 大整数不能先截为long再判断JS安全范围；已有边界输出格式保持兼容。
 */
class BigNumberSerializerTest {
    final JsonMapper json =
        JsonMapper.builder()
            .addModule(
                new SimpleModule()
                    .addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE)
                    .addSerializer(Long.class, BigNumberSerializer.INSTANCE))
            .build();

    @Test
    void integersBeyondLongRangeMustRemainExactStrings() {
        for (String value :
            new String[]{
                "18446744073709551616",
                "18446744073709551617",
                "-18446744073709551616",
                "999999999999999999999999999999999999"
            }) {
            var integer = new BigInteger(value);
            var node = json.readTree(json.writeValueAsString(integer));
            assertTrue(node.isString(), value);
            assertEquals(value, node.asString());
        }
    }

    @Test
    void normalNumbersAndExistingBoundaryStringsStayCompatible() {
        assertEquals("42", json.writeValueAsString(42L));
        assertEquals("42", json.writeValueAsString(BigInteger.valueOf(42)));
        assertEquals("\"9007199254740991\"", json.writeValueAsString(9007199254740991L));
        assertEquals("\"-9007199254740991\"", json.writeValueAsString(-9007199254740991L));
        assertEquals("\"9223372036854775807\"", json.writeValueAsString(Long.MAX_VALUE));
    }
}
