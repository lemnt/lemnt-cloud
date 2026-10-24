package com.lemnt.common.datascope.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限过滤注解，标注在实际执行 SQL 的 Mapper 方法上，由 MyBatis 拦截器读取。
 *
 * @author wangxx
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 用户表的别名
     */
    String userAlias() default "";

    /**
     * 部门表的别名
     */
    String deptAlias() default "";

    /**
     * 权限字符（用于多个角色匹配符合要求的权限）默认根据权限注解@RequiresPermissions获取，多个权限用逗号分隔开来
     */
    String permission() default "";

    /**
     * 用户表字段
     */
    String userIdColumnName() default "user_id";
}
