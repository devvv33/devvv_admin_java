package com.devvv.commons.core.config.datasource.typehandler;

import com.devvv.commons.common.enums.IntIDEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Create by WangSJ on 2023/07/05
 *
 * 枚举转换器
 */
@MappedJdbcTypes(value = JdbcType.INTEGER, includeNullJdbcType = true)
public class IntIDEnumTypeHandler extends BaseTypeHandler<IntIDEnum> {

    private Class<IntIDEnum> type;

    public IntIDEnumTypeHandler() {}
    public IntIDEnumTypeHandler(Class<IntIDEnum> type) {
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, IntIDEnum idEnum, JdbcType jdbcType) throws SQLException {
        if (idEnum == null) {
            ps.setInt(i, 0);
        } else {
            ps.setInt(i, idEnum.getId());
        }
    }

    @Override
    public IntIDEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getValue(rs.getInt(columnName));
    }

    @Override
    public IntIDEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getValue(rs.getInt(columnIndex));
    }

    @Override
    public IntIDEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getValue(cs.getInt(columnIndex));
    }

    private IntIDEnum getValue(Integer value) {
        if (value == null) {
            return null;
        }
        return IntIDEnum.byId(value, type);
    }
}
