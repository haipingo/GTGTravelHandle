package com.travel.ticktes.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;


import org.springframework.stereotype.Repository;

import com.ibatis.sqlmap.client.SqlMapClient;

@SuppressWarnings("rawtypes")
@Repository("querySchemerefreshImpl")
public class QuerySchemerefreshImpl{
	
	@Resource(name = "sqlMapClient")
	private SqlMapClient sqlMapClient;

	/**
	 * 根据参数查询 班次计划
	 * @param map
	 * @return 班线计划集合
	 */
	
	public List querySchemerefresh(Map map) {
		List list=null;
		try {
			list=sqlMapClient.queryForList("ticktes.querySchemerefresh",map);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}
	
	
	/**
	 * 获取条形码
	 * @param map
	 * @return 班线计划集合
	 */
	
	public String autoBarCode(String pname) {
		String code="";
		HashMap<String,Object> map = new HashMap<String,Object>();  
		map.put("code", null);
		try {
			System.out.println(sqlMapClient.queryForObject(pname,map));
			System.out.println("调用存储过程返回的参数值："+map.get("code"));
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return code;
	}
	
}
