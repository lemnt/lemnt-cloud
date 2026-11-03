package com.lemnt.common.core.utils;

import org.apache.commons.lang3.StringUtils;

/** 同名父类的导入桥，保留现有StringUtils继承的静态API，避免全限定类型引用。 */
class CommonsStringUtils extends StringUtils {}
