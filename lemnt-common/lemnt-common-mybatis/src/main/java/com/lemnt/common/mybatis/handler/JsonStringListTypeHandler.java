package com.lemnt.common.mybatis.handler;

import cn.hutool.core.collection.CollUtil;
import com.lemnt.common.core.utils.JsonUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * 字符串 转数组
 *
 * @author wangxx
 * @version 1.0.0
 * @date 30/8/2025 09:26
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(List.class)
public class JsonStringListTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        if (CollUtil.isEmpty(parameter)) {
            ps.setString(i, "[]");
        } else {
            ps.setString(i, JsonUtils.toJsonStr(parameter));
        }
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private List<String> parse(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return JsonUtils.toList(value, String.class);
    }
}
