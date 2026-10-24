package com.lemnt.common.datasource.annotation;

import com.baomidou.dynamic.datasource.annotation.DS;

import java.lang.annotation.*;

/**
 * doris 数据源
 *
 * @author wangxx
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DS("doris")
public @interface Doris {

}