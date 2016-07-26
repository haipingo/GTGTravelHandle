package com.travel.util;

import com.ibatis.sqlmap.engine.type.*;
import java.sql.*;

public class SolarObjectTypeHandler extends BaseTypeHandler implements
		TypeHandler {

	public SolarObjectTypeHandler() {
		clobTypeHandler = new CustomTypeHandler(new ClobTypeHandlerCallback());
	}

	public void setParameter(PreparedStatement ps, int i, Object parameter,
			String jdbcType) throws SQLException {
		ps.setObject(i, parameter);
	}

	public Object getResult(ResultSet rs, String columnName)
			throws SQLException {
		Object object = rs.getObject(columnName);
		if (rs.wasNull())
			return null;
		if (object instanceof Clob)
			return clobTypeHandler.getResult(rs, columnName);
		else
			return object;
	}

	public Object getResult(ResultSet rs, int columnIndex) throws SQLException {
		Object object = rs.getObject(columnIndex);
		if (rs.wasNull())
			return null;
		if (object instanceof Clob)
			return clobTypeHandler.getResult(rs, columnIndex);
		else
			return object;
	}

	public Object getResult(CallableStatement cs, int columnIndex)
			throws SQLException {
		Object object = cs.getObject(columnIndex);
		if (cs.wasNull())
			return null;
		if (object instanceof Clob)
			return clobTypeHandler.getResult(cs, columnIndex);
		else
			return object;
	}

	public Object valueOf(String s) {
		return s;
	}

	private TypeHandler clobTypeHandler;
}
