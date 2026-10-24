package com.lemnt.common.mybatis.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * long 转 数组
 *
 * @author wangxx
 * @version 1.0.0
 * @date 30/8/2025 09:26
 */
@MappedJdbcTypes(JdbcType.VARCHAR)
@MappedTypes(List.class)
public class LongListTypeHandler extends BaseTypeHandler<List<Long>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<Long> parameter, JdbcType jdbcType) throws SQLException {
        // 存库时转成逗号分隔字符串
        ps.setString(i, parameter.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public List<Long> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public List<Long> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    private List<Long> parse(String value) {
        if (value == null || value.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}
