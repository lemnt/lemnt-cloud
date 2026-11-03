package com.lemnt.common.start.jackson;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.annotation.JacksonStdImpl;
import tools.jackson.databind.ser.std.StdSerializer;

import java.math.BigInteger;

/**
 * 超出 JS 最大最小值 处理
 *
 * @author Lion Li
 */
@JacksonStdImpl
public class BigNumberSerializer extends StdSerializer<Number> {

    /**
     * 根据 JS Number.MAX_SAFE_INTEGER 与 Number.MIN_SAFE_INTEGER 得来
     */
    private static final BigInteger MAX_SAFE_INTEGER = BigInteger.valueOf(9007199254740991L);

    private static final BigInteger MIN_SAFE_INTEGER = MAX_SAFE_INTEGER.negate();

    /**
     * 提供实例
     */
    public static final BigNumberSerializer INSTANCE = new BigNumberSerializer(Number.class);

    public BigNumberSerializer(Class<? extends Number> rawType) {
        super(rawType);
    }

    @Override
    public void serialize(Number value, JsonGenerator gen, SerializationContext provider) {
        // BigInteger先转long会截断，必须用原整数比较；边界输出沿用既有字符串契约。
        BigInteger integer =
                value instanceof BigInteger big ? big : BigInteger.valueOf(value.longValue());
        if (integer.compareTo(MIN_SAFE_INTEGER) > 0 && integer.compareTo(MAX_SAFE_INTEGER) < 0) {
            gen.writeNumber(value.toString());
        } else {
            gen.writeString(value.toString());
        }
    }
}
