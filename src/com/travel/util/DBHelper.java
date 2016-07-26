package com.travel.util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class DBHelper{
	//public static final String url="jdbc:mysql://112.74.67.4:3306/gtgdb?useUnicode=true&characterEncoding=utf8";
	//public static final String name = "com.mysql.jdbc.Driver";
	//public static final String user = "root";
	//public static final String password = "GZgtgdb@123!@#*2016";
	
	
	public static final String url = "jdbc:mysql://localhost:3306/gtgdb";
	public static final String name = "com.mysql.jdbc.Driver";
	public static final String user = "root";
	public static final String password = "123456";
	//日志记录
	private static Log log=LogFactory.getLog(DBHelper.class);
	
	public static Connection conn = null;
	public PreparedStatement pst = null;
	public DBHelper(String sql) {
		try {
			Class.forName(name);//指定连接类型
			System.out.println(url);
			conn = DriverManager.getConnection(url, user, password);//获取连接
			pst = conn.prepareStatement(sql);//准备执行语句
		} catch (Exception e) {
			log.info("数据库连接异常："+e.getMessage());
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			conn.close();
			this.pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static Connection getConnection() {
		try {	
			System.out.println(url);
			Class.forName(name);//指定连接类型
			conn = DriverManager.getConnection(url, user, password);//获取连接
		} catch (SQLException e) {
			System.out.println("AAA");
			log.info("数据库连接异常："+e.getMessage());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			log.info("数据库连接异常："+e.getMessage());
			e.printStackTrace();
		}
		return conn;
	}
}
