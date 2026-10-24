package com.lemnt.common.core.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.util.AntPathMatcher;
import com.lemnt.common.core.constant.Constants;

/**
 * 字符串工具类
 * <p>
 * 仅保留 commons-lang3 未覆盖的扩展方法。
 * 标准判空/判空请直接使用 {@link org.apache.commons.lang3.StringUtils}。
 * </p>
 *
 * @author wangxx
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    private static final String NULLSTR = "";
    private static final char SEPARATOR = '_';

    /**
     * 判断 Collection 是否为空（commons-lang3 未覆盖此重载）
     */
    public static boolean isEmpty(Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    /**
     * 判断 Map 是否为空（commons-lang3 未覆盖此重载）
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * 获取参数不为空值
     */
    public static <T> T nvl(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * 判断是否为空，并且不是空白字符
     */
    public static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    private static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 替换指定字符串的指定区间内字符为"*"
     */
    public static String hide(CharSequence str, int startInclude, int endExclude) {
        if (isEmpty(str)) {
            return NULLSTR;
        }
        final int strLength = str.length();
        if (startInclude > strLength) {
            return NULLSTR;
        }
        if (endExclude > strLength) {
            endExclude = strLength;
        }
        if (startInclude > endExclude) {
            return NULLSTR;
        }
        final char[] chars = new char[strLength];
        for (int i = 0; i < strLength; i++) {
            if (i >= startInclude && i < endExclude) {
                chars[i] = '*';
            } else {
                chars[i] = str.charAt(i);
            }
        }
        return new String(chars);
    }

    /**
     * 在字符串中查找第一个出现的 open 和最后一个出现的 close 之间的子字符串
     */
    public static String substringBetweenLast(final String str, final String open, final String close) {
        if (isEmpty(str) || isEmpty(open) || isEmpty(close)) {
            return NULLSTR;
        }
        final int start = str.indexOf(open);
        if (start != INDEX_NOT_FOUND) {
            final int end = str.lastIndexOf(close);
            if (end != INDEX_NOT_FOUND) {
                return str.substring(start + open.length(), end);
            }
        }
        return NULLSTR;
    }

    /**
     * 是否为http(s)://开头
     */
    public static boolean ishttp(String link) {
        return StringUtils.startsWithAny(link, Constants.HTTP, Constants.HTTPS);
    }

    /**
     * 判断给定的collection列表中是否包含数组array
     */
    public static boolean containsAny(Collection<String> collection, String... array) {
        if (isEmpty(collection) || array == null || array.length == 0) {
            return false;
        } else {
            for (String str : array) {
                if (collection.contains(str)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 驼峰转下划线命名
     */
    public static String toUnderScoreCase(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean preCharIsUpperCase = true;
        boolean curreCharIsUpperCase = true;
        boolean nexteCharIsUpperCase = true;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i > 0) {
                preCharIsUpperCase = Character.isUpperCase(str.charAt(i - 1));
            } else {
                preCharIsUpperCase = false;
            }

            curreCharIsUpperCase = Character.isUpperCase(c);

            if (i < (str.length() - 1)) {
                nexteCharIsUpperCase = Character.isUpperCase(str.charAt(i + 1));
            }

            if (preCharIsUpperCase && curreCharIsUpperCase && !nexteCharIsUpperCase) {
                sb.append(SEPARATOR);
            } else if ((i != 0 && !preCharIsUpperCase) && curreCharIsUpperCase) {
                sb.append(SEPARATOR);
            }
            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    /**
     * 是否包含字符串（忽略大小写）
     */
    public static boolean inStringIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            for (String s : strs) {
                if (str.equalsIgnoreCase(trim(s))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 将下划线大写方式命名的字符串转换为驼峰式
     * 例如：HELLO_WORLD -> HelloWorld
     */
    public static String convertToCamelCase(String name) {
        StringBuilder result = new StringBuilder();
        if (name == null || name.isEmpty()) {
            return "";
        } else if (!name.contains("_")) {
            return name.substring(0, 1).toUpperCase() + name.substring(1);
        }
        String[] camels = name.split("_");
        for (String camel : camels) {
            if (camel.isEmpty()) {
                continue;
            }
            result.append(camel.substring(0, 1).toUpperCase());
            result.append(camel.substring(1).toLowerCase());
        }
        return result.toString();
    }

    /**
     * 驼峰式命名法，例如：user_name -> userName
     */
    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }
        if (s.indexOf(SEPARATOR) == -1) {
            return s;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 查找指定字符串是否匹配指定字符串列表中的任意一个
     */
    public static boolean matches(String str, List<String> strs) {
        if (isEmpty(str) || isEmpty(strs)) {
            return false;
        }
        for (String pattern : strs) {
            if (isMatch(pattern, str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断url是否与规则配置:
     * ? 表示单个字符;
     * * 表示一层路径内的任意字符串，不可跨层级;
     * ** 表示任意层路径;
     */
    public static boolean isMatch(String pattern, String url) {
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    /**
     * 数字左边补齐0，使之达到指定长度
     */
    public static final String padl(final Number num, final int size) {
        return padl(num.toString(), size, '0');
    }

    /**
     * 字符串左补齐
     */
    public static final String padl(final String s, final int size, final char c) {
        final StringBuilder sb = new StringBuilder(size);
        if (s != null) {
            final int len = s.length();
            if (s.length() <= size) {
                for (int i = size - len; i > 0; i--) {
                    sb.append(c);
                }
                sb.append(s);
            } else {
                return s.substring(len - size, len);
            }
        } else {
            for (int i = size; i > 0; i--) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
