package com.lemnt.common.sensitive.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import tools.jackson.databind.annotation.JsonSerialize;
import com.lemnt.common.sensitive.config.SensitiveJsonSerializer;
import com.lemnt.common.sensitive.enums.DesensitizedType;

/**
 * 数据脱敏注解
 *
 * @author wangxx
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveJsonSerializer.class)
public @interface Sensitive {
    DesensitizedType desensitizedType();
}
