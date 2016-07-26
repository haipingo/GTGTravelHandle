package com.travel.ticktes.dao;

import java.sql.SQLException;
import java.util.Map;

import javax.annotation.Resource;


import org.springframework.stereotype.Repository;

import com.ibatis.sqlmap.client.SqlMapClient;

@SuppressWarnings("rawtypes")
@Repository("ticketPurchaseImpl")
public class TicketPurchaseImpl{
	
	@Resource(name = "sqlMapClient")
	private SqlMapClient sqlMapClient;

	/**
	 * 新增订单数据
	 * @param map
	 * @return 新增购票订单
	 */
	
	public int addTicketInfo(Map map) {
		int addResult=0;
		try {
			addResult=sqlMapClient.update("purchase.addTicketInfo",map);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return addResult;
	}
}
