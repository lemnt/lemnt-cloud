package com.lemnt.common.datasource.annotation;

import com.baomidou.dynamic.datasource.annotation.DS;

import java.lang.annotation.*;

/**
 * doris 读数据源
 *
 * @author wangxx
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DS("doris-read")
public @interface DorisRead {

}