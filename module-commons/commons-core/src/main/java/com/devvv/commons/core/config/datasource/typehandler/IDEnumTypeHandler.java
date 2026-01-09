package com.devvv.commons.core.config.datasource.typehandler;

import com.devvv.commons.common.enums.IDEnum;
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
@MappedJdbcTypes(value = JdbcType.CHAR, includeNullJdbcType = true)
public class IDEnumTypeHandler extends BaseTypeHandler<IDEnum> {

    private Class<IDEnum> type;

    public IDEnumTypeHandler() {}
    public IDEnumTypeHandler(Class<IDEnum> type) {
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, IDEnum idEnum, JdbcType jdbcType) throws SQLException {
        if (idEnum == null) {
            ps.setString(i, null);
        } else {
            ps.setString(i, idEnum.getId());
        }
    }

    @Override
    public IDEnum getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return getValue(rs.getString(columnName));
    }

    @Override
    public IDEnum getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return getValue(rs.getString(columnIndex));
    }

    @Override
    public IDEnum getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return getValue(cs.getString(columnIndex));
    }

    private IDEnum getValue(String value) {
        if (value == null) {
            return null;
        }
        return IDEnum.byId(value, type);
    }
}
