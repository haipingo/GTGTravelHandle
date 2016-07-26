package com.travel.base;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.mysql.jdbc.CallableStatement;
import com.travel.util.AES;
import com.travel.util.DBHelper;
import com.travel.util.StationClient;

import java.sql.Connection;


public class StationDataObtain {
	  
	private final static String URL = "http://112.74.67.4:7080/GTGAPI/IIB/Station";
	//private  final static String URL = "http://121.33.237.85:8090/GTGAPI/GTGInterface.asmx/StationI";
	   
	private static StationClient stationClient=new StationClient();
	private static Log log=LogFactory.getLog(StationDataObtain.class);
	
	private static Map<String, Object> map = new HashMap<String, Object>();
	private static Map<String,Object> Router=new HashMap<String,Object>();
	private static Map<String,Object> document=new HashMap<String,Object>();
	
	private static String sql = null;
	private static DBHelper dbHelper = null;
	private static ResultSet ret = null;
	private static Connection conn = null;
	private static PreparedStatement ps = null; 
	private static ResultSet rs = null; 
	
	/**
	 * 数据签名
	 * 
	 * @param plain
	 * @return
	 */
	public static String encryption(String plain) {
		String re_md5 = new String();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plain.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			re_md5 = buf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return re_md5;
	}



	/**
	 * 获取票价信息
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Object> priceInfo(Map<String, Object> params,String areaDepotCode) throws Exception{
		params.put("Method", "PriceInfo");
		JSONObject obj=process("PriceInfo",params,areaDepotCode);
		Map<String, Object> resultMap =null;
		if (obj.getString("State").equals("false")) {
			log.info("查询票价异常:"+obj.getString("Errormsg"));
			throw new Exception(obj.getString("Errormsg"));
		} else {
			obj.remove("State");
			obj.remove("Errormsg");
			obj.remove("PageSize");
			obj.remove("TotalPage");
			Iterator it = obj.keys();
			resultMap = new HashMap<String, Object>();
			if (it.hasNext()) {
				String modelName = it.next().toString();
				JSONArray dataJson = obj.getJSONArray(modelName);
				resultMap.put("iRemain", dataJson.getJSONObject(0).get("iRemain"));//剩余座位
				resultMap.put("szDest", dataJson.getJSONObject(0).get("szDest"));//到站中文名
				resultMap.put("szFare", dataJson.getJSONObject(0).get("szFare"));//全票价
				resultMap.put("dbBaf", dataJson.getJSONObject(0).get("szBaf"));//全票价
				resultMap.put("szSeatMap", dataJson.getJSONObject(0).get("szSeatMap"));//座位图
				resultMap.put("szEntry", dataJson.getJSONObject(0).get("szEntry"));//座位图
				//燃油附加费
			}
		}
		log.info("查询票价返回结果:"+net.sf.json.JSONArray.fromObject(resultMap));
		return resultMap;
	}
	
	/**
	 * 取消锁票
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> lockCancel(Map<String, Object> params,String areaDepotCode) throws Exception{
		params.put("Method", "LockCancel");
		//传入参数开始
		JSONObject obj=process("LockCancel",params,areaDepotCode);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if (obj.getString("State").equals("false")) {
			throw new Exception(obj.getString("Errormsg"));
		} else {
			resultMap = new HashMap<String, Object>();
			resultMap.put("State", obj.getString("State"));
			resultMap.put("sumbarcode", obj.getString("LockCancel"));
		}
		System.out.println("取消锁票结果:"+net.sf.json.JSONArray.fromObject(resultMap));
		return resultMap;
	}
	/**
	 * 锁定座票
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Object> ticketLock(Map<String, Object> params,String areaDepotCode) throws Exception{
		//开始传入参数
		params.put("Method", "TicketLock");
		/*
		params.put("szDate", "20151210");
		params.put("szNo", "ASR1R060");
		params.put("iTickets", "1");
		params.put("szOwnerDepot", "0011");
		params.put("szDepot", "0011");
		params.put("szType", "1");
		params.put("szLocalDepot", "0001");
		params.put("iFrees", "0");
		params.put("szSeat", "");
		params.put("szHostname", "0001.J0000218@10");*/
		JSONObject obj=process("TicketLock",params,areaDepotCode);
		Map<String, Object> resultMap=null;
		if (obj.getString("State").equals("false")) {
			log.info("锁票异常:"+obj.getString("Errormsg"));
			throw new Exception(obj.getString("Errormsg"));
		} else {
			obj.remove("Errormsg");
			obj.remove("PageSize");
			obj.remove("TotalPage");
			resultMap = new HashMap<String, Object>();
			resultMap.put("State", obj.getString("State"));
			obj.remove("State");
			Iterator it = obj.keys();
			if (it.hasNext()) {
				String modelName = it.next().toString();
				JSONArray dataJson = obj.getJSONArray(modelName);
				resultMap.put("modelName", modelName);
				resultMap.put("szLockSeat", dataJson.getJSONObject(0).getString("szLockSeat"));
			}
		}
		log.info("锁票返回的结果:"+net.sf.json.JSONArray.fromObject(resultMap));
		return resultMap;
	}
	
	/**
	 * 售票更新
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> ticketUpdate(Map<String, Object> params,String areaDepotCode) throws Exception{
		params.put("Method","TicketUpdate");
		
		
		JSONObject obj=process("TicketUpdate",params,areaDepotCode);
		System.err.println(obj);
		Map<String, Object> resultMap=null;
		if (obj.getString("State").equals("false")) {
			throw new Exception(obj.getString("Errormsg"));
		} else {
			resultMap = new HashMap<String, Object>();
			resultMap.put("State", obj.getString("State"));
			JSONObject jso=obj.getJSONObject("TicketUpdate");
			System.out.println("===AAA:"+jso);
			System.out.println("条形码AAAAAAAAAAAAA："+jso.getString("sumbarcode"));
			resultMap.put("sumbarcode",jso.getString("sumbarcode"));
		}
		
		System.out.println("更新票返回的结果:"+net.sf.json.JSONArray.fromObject(resultMap));
		log.info("更新票返回的结果:"+net.sf.json.JSONArray.fromObject(resultMap));
		return resultMap;
	}
	
	/**
	 * 废票更新
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> ticketBlankout(Map<String, Object> params,String areaDepotCode) throws Exception{
		//开始传入参数
		params.put("Method", "TicketBlankout");

		JSONObject obj=process("TicketBlankout",params,areaDepotCode);
		
		Map<String, Object> resultMap=null;
		if (obj.getString("State").equals("false")) {
			throw new Exception(obj.getString("Errormsg"));
		} else {
			resultMap = new HashMap<String, Object>();
			resultMap.put("State", obj.getString("State"));
			resultMap.put("sumbarcode", obj.getString("TicketBlankout"));
		}
		
		System.out.println("废票更新返回的结果:"+net.sf.json.JSONArray.fromObject(resultMap));
		log.info("废票更新返回的结果:"+net.sf.json.JSONArray.fromObject(resultMap));
		return resultMap;
	}
	
	/**
	 * 取得退费率
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Object> getOffFare() throws Exception{
		StationClient stationClient =new StationClient();
		//参数定义
		String model="GetOffFare";
		String timestamp = String.valueOf(System.currentTimeMillis());
		
		//传入固定参数Document 参数
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Scope", "DATA");
		map.put("Type", "EXECUTE");
		map.put("Domain", "STATION");
		map.put("LocalDepot", "0001");
		map.put("Aes", "1");
		map.put("TimeStamp", timestamp);
		map.put("Sign", encryption("STATIONEXECUTE2*IkzEyB" + model+timestamp));
		
		
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> params=new HashMap<String, String>();
		params.put("Method","GetOffFare");
		params.put("szBarCode", "0100000024,0100000025,");
		params.put("szLocalDepot", "0001");
		params.put("szOrderid","J0000017");
		list.add(params);
		map.put("Datas", list);
		
		Map<String,Object> document=new HashMap<String,Object>();
		document.put("Document",map);
		
		//Router参数
		Map<String,Object> Router=new HashMap<String,Object>();
		Router.put("SerialNo", "2222");
		Router.put("ServiceId", "1111");
		Router.put("ServiceTime", "20150515");
		Router.put("SourceSysId", "001");
		
		document.put("Router", Router);
		String preferences = JSON.toJSONString(document);
		
		
		//开始调用方法
		String jsonData = stationClient.execute(URL,preferences);
		
		//如果不加密map.put("Aes", "1"); 以下段注释掉   begin...
		String data []=jsonData.split("\\[");
		String begin=data[0];
		String middle=data[1].split("\\]")[0];
	    String content=AES.Decrypt(middle, "3fL5oJarKjsFgDGO");
	    jsonData=begin+"["+content+"]}}";
	    //end...
	    
		
	    JSONObject jsonObject = new JSONObject(jsonData);
		JSONObject documentJson = jsonObject.getJSONObject("Document");
		JSONArray datas = documentJson.getJSONArray("Datas");
		
		
		JSONObject obj =(JSONObject) datas.get(0);
		if (obj.getString("State").equals("false")) {
			throw new Exception(obj.getString("Errormsg"));
		} else {
			obj.remove("State");
			obj.remove("Errormsg");
			obj.remove("PageSize");
			obj.remove("TotalPage");
			Iterator it = obj.keys();
			Map<String, Object> resultMap = new HashMap<String, Object>();
			if (it.hasNext()) {
				String modelName = it.next().toString();
				JSONArray dataJson = obj.getJSONArray(modelName);
				resultMap.put("modelName", modelName);
				resultMap.put("datas", dataJson);
			}
			
		}
		
		return null;
		
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Object> ticketOff() throws Exception{
		StationClient stationClient =new StationClient();
		//参数定义
		String model="TicketOff";
		String timestamp = String.valueOf(System.currentTimeMillis());
		
		//传入固定参数Document 参数
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Scope", "DATA");
		map.put("Type", "EXECUTE");
		map.put("Domain", "STATION");
		map.put("LocalDepot", "0001");
		map.put("Aes", "1");
		map.put("TimeStamp", timestamp);
		map.put("Sign", encryption("STATIONEXECUTE2*IkzEyB" + model+timestamp));
		
		
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> params=new HashMap<String, String>();
		params.put("Method","TicketOff");
		/*
		params.put("szOwnerDepot","0011");
		params.put("szDepot","0011");
		params.put("szLocalDepot","0001");
		params.put("szBarCode","0100000008");
		params.put("szFrae","5.00,");
		params.put("szOper","1029901");
		params.put("szStaffCode","J0001");
		params.put("szTime","20151203 114100");
		params.put("szAreaDepot","0011");
		params.put("szOrderid","J0000014");
		*/
		params.put("Method","TicketOff");
		params.put("szBarCode","0100000024,0100000025");
		params.put("szStaffCode","J0001");
		params.put("szNo","ASH1H036");
		params.put("szLocalDepot","0001");
		params.put("szTime","20151205 094300");
		params.put("szAreaDepot","0011");
		params.put("szOwnerDepot","0011");
		params.put("szOrderid","J0000017");
		
		params.put("szDepot","0011");
		params.put("szFare","5.00,5.00,");
		params.put("szOper","1022909012");
		
		
		
		
		
		
		
		
		 
		
		


		
		list.add(params);
		map.put("Datas", list);
		
		Map<String,Object> document=new HashMap<String,Object>();
		document.put("Document",map);
		
		//Router参数
		Map<String,Object> Router=new HashMap<String,Object>();
		Router.put("SerialNo", "2222");
		Router.put("ServiceId", "1111");
		Router.put("ServiceTime", "20150515");
		Router.put("SourceSysId", "001");
		
		document.put("Router", Router);
		String preferences = JSON.toJSONString(document);
		System.out.println("请求参数:"+preferences);
		
		
		//开始调用方法
		String jsonData = stationClient.execute(URL,preferences);
		System.out.println("请求获取最初结果:"+jsonData);
		//System.out.println("取得的加密结果:"+jsonData);
		
		//如果不加密map.put("Aes", "1"); 以下段注释掉   begin...
		String data []=jsonData.split("\\[");
		String begin=data[0];
		String middle=data[1].split("\\]")[0];
	    String content=AES.Decrypt(middle, "3fL5oJarKjsFgDGO");
	    jsonData=begin+"["+content+"]}}";
	    //end...
	    
		
	    JSONObject jsonObject = new JSONObject(jsonData);
	   // System.out.println("我调试到这里了:"+jsonObject);
		JSONObject documentJson = jsonObject.getJSONObject("Document");
		JSONArray datas = documentJson.getJSONArray("Datas");
		
		
		JSONObject obj =(JSONObject) datas.get(0);
		if (obj.getString("State").equals("false")) {
			throw new Exception(obj.getString("Errormsg"));
		} else {
			obj.remove("State");
			obj.remove("Errormsg");
			obj.remove("PageSize");
			obj.remove("TotalPage");
			Iterator it = obj.keys();
			Map<String, Object> resultMap = new HashMap<String, Object>();
			if (it.hasNext()) {
				String modelName = it.next().toString();
				JSONArray dataJson = obj.getJSONArray(modelName);
				resultMap.put("modelName", modelName);
				resultMap.put("datas", dataJson);
			}
			System.out.println(resultMap);
			
		}
		
		return null;
		
	}
	
	/**
	 * 退票确认
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Object> ticketOffConfrim() throws Exception{
		StationClient stationClient =new StationClient();
		//参数定义
		String model="TicketOff";
		String timestamp = String.valueOf(System.currentTimeMillis());
		
		//传入固定参数Document 参数
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Scope", "DATA");
		map.put("Type", "EXECUTE");
		map.put("Domain", "STATION");
		map.put("LocalDepot", "0001");
		map.put("Aes", "1");
		map.put("TimeStamp", timestamp);
		map.put("Sign", encryption("STATIONEXECUTE2*IkzEyB" + model+timestamp));
		
		
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> params=new HashMap<String, String>();
		params.put("Method","TicketOff");
		/*
		params.put("szOwnerDepot","0011");
		params.put("szDepot","0011");
		params.put("szLocalDepot","0001");
		params.put("szBarCode","0100000008");
		params.put("szFrae","5.00,");
		params.put("szOper","1029901");
		params.put("szStaffCode","J0001");
		params.put("szTime","20151203 114100");
		params.put("szAreaDepot","0011");
		params.put("szOrderid","J0000014");
		*/
		params.put("Method","TicketOff");
		params.put("szBarCode","0100000010");
		params.put("szStaffCode","J0001");
		params.put("szNo","ASH1H036");
		params.put("szLocalDepot","0001");
		params.put("szTime","20151203 160700");
		params.put("szAreaDepot","0011");
		params.put("szOwnerDepot","0011");
		params.put("szOrderid","J0000201");
		
		params.put("szDepot","0011");
		params.put("szFare","5.00,");
		params.put("szOper","1022909012");
		
		
		
		
		
		
		
		
		 
		
		


		
		list.add(params);
		map.put("Datas", list);
		
		Map<String,Object> document=new HashMap<String,Object>();
		document.put("Document",map);
		
		//Router参数
		Map<String,Object> Router=new HashMap<String,Object>();
		Router.put("SerialNo", "2222");
		Router.put("ServiceId", "1111");
		Router.put("ServiceTime", "20150515");
		Router.put("SourceSysId", "001");
		
		document.put("Router", Router);
		String preferences = JSON.toJSONString(document);
		System.out.println("请求参数:"+preferences);
		
		
		//开始调用方法
		String jsonData = stationClient.execute(URL,preferences);
		System.out.println("请求获取最初结果:"+jsonData);
		//System.out.println("取得的加密结果:"+jsonData);
		System.out.println("===========AAA"+jsonData);
		
		//如果不加密map.put("Aes", "1"); 以下段注释掉   begin...
		String data []=jsonData.split("\\[");
		String begin=data[0];
		String middle=data[1].split("\\]")[0];
	    String content=AES.Decrypt(middle, "3fL5oJarKjsFgDGO");
	    jsonData=begin+"["+content+"]}}";
	    System.out.println("取的的结果:"+jsonData);
	    //end...
	    
		
	    JSONObject jsonObject = new JSONObject(jsonData);
	   // System.out.println("我调试到这里了:"+jsonObject);
		JSONObject documentJson = jsonObject.getJSONObject("Document");
		JSONArray datas = documentJson.getJSONArray("Datas");
		System.out.println("datas:"+datas);
		
		
		JSONObject obj =(JSONObject) datas.get(0);
		if (obj.getString("State").equals("false")) {
			throw new Exception(obj.getString("Errormsg"));
		} else {
			obj.remove("State");
			obj.remove("Errormsg");
			obj.remove("PageSize");
			obj.remove("TotalPage");
			Iterator it = obj.keys();
			Map<String, Object> resultMap = new HashMap<String, Object>();
			if (it.hasNext()) {
				String modelName = it.next().toString();
				JSONArray dataJson = obj.getJSONArray(modelName);
				resultMap.put("modelName", modelName);
				resultMap.put("datas", dataJson);
			}
			System.out.println(resultMap);
			
		}
		
		return null;
		
	}
	/***
	 * 数据逻辑处理代码
	 * @param model 模块名称
	 * @param params 参数
	 * @return 数据对象
	 * @throws Exception 用于外部捕获异常，返回到跳转页面
	 */
	public static JSONObject process(String model,Map<String, Object> params,String areaDepotCode) throws Exception{
		log.info("执行的操作模块:"+model);
		//初始化固定
		String timestamp = String.valueOf(System.currentTimeMillis());
		//传入固定参数Document 参数
		map.put("Scope", "DATA");
		map.put("Type", "EXECUTE");
		map.put("Domain", "STATION");
		map.put("LocalDepot", "0008");
		map.put("Aes", "1");
		map.put("TimeStamp", timestamp);
		//判断,各个战场的签名不一样
		//判断操作模块
		//省站(0011)
		if("0011".equals(areaDepotCode)){
			map.put("Sign", encryption("STATIONEXECUTE2*IkzEyB" + model+timestamp));
		}
		
		//天河客运站(0013)
		if("0013".equals(areaDepotCode)){
			map.put("Sign", encryption("STATIONEXECUTEmwjr3o4c" + model+timestamp));
		}
		
		//东站客运站(0028)
		if("0028".equals(areaDepotCode)){
			map.put("Sign", encryption("STATIONEXECUTEo4qlc7db" + model+timestamp));
		}
		
		
		//番禺客运站(0060)
		if("0060".equals(areaDepotCode)){
			map.put("Sign", encryption("STATIONEXECUTESd3cvBxJ" + model+timestamp));
		}
		
		//罗冲围客运站(0016)
		if("0016".equals(areaDepotCode)){
			map.put("Sign", encryption("STATIONEXECUTEMJxtFB7x" + model+timestamp));
		}
		
		//越秀南站(0015)
		if("0015".equals(areaDepotCode)){
			map.put("Sign", encryption("STATIONEXECUTEHx8VtH6V" + model+timestamp));
		}
		
		//广园客运站(0018)
		if("0018".equals(areaDepotCode)){
			map.put("Sign", encryption("STATIONEXECUTE00180018" + model+timestamp));
		}
		//初始化参数
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		list.add(params);
		
		map.put("Datas", list);
		document.put("Document",map);
		document.put("Router", Router);
		//Router参数
		Router.put("SerialNo", "2222");
		Router.put("ServiceId", "1111");
		Router.put("ServiceTime", "20150515");
		Router.put("SourceSysId", "001");
		Router.put("areaDepotCode", areaDepotCode);
		//System.out.println("拼接好的参数："+document);
		
		
		String preferences = JSON.toJSONString(document);
		log.info(model+"参数:"+preferences);
		String jsonData = stationClient.execute(URL,preferences);
		
		String data []=jsonData.split("\\[");
		String begin=data[0];
		String middle=data[1].split("\\]")[0];
	    String content="";
		
	    //省站(0011)
		if("0011".equals(areaDepotCode)){
			content=AES.Decrypt(middle, "3fL5oJarKjsFgDGO");
		}
		
		//天河客运站(0013)
		if("0013".equals(areaDepotCode)){
			content=AES.Decrypt(middle, "2wy03jcxxffmzwnl");
		}
		 
		//东站客运站(0028)
		if("0028".equals(areaDepotCode)){
			content=AES.Decrypt(middle, "u4dh8s3ticcehqj1");
		}
	    
		//番禺客运站(0060)
		if("0060".equals(areaDepotCode)){
			content=AES.Decrypt(middle, "02rxz0ay5z5npure");
		}
	    
		//罗冲围客运站(0016)
		if("0016".equals(areaDepotCode)){
			content=AES.Decrypt(middle, "TdYe4BD1RSEZS7Cx");
		}
		
		//越秀南站(0015)
		if("0015".equals(areaDepotCode)){
			 content=AES.Decrypt(middle, "7ACW7dXAiWtSo272");
		}
		
		//广园客运站(0018)
		if("0018".equals(areaDepotCode)){
			content=AES.Decrypt(middle, "2e25b164b72ea1ce");
		}
		
	    jsonData=begin+"["+content+"]}}";
	    JSONObject jsonObject = new JSONObject(jsonData);
		JSONObject documentJson = jsonObject.getJSONObject("Document");
		JSONArray datas = documentJson.getJSONArray("Datas");
		return datas.getJSONObject(0);
	}
	
	/***
	 * 根据班车ID查询班车计划
	 * @param busId 班车计划ID
	 * @return 返回班车信息集合(发车时间,班次日期,班次名称,等信息)
	 * @throws Exception
	 */
	public static Map<String,Object> busSchedule(
			String busId) throws Exception{
		Map<String, Object> params=new HashMap<String, Object>();
		//传入班次ID，获取班次信息，票价
		sql = "select REPLACE(TIME,'-','') time,time time1,no, terminalName AS name,stationData,ownerDepot,localDepot,areaDepot,terminalCode from schemerefresh_view where id='"+busId+"'";//SQL语句
		dbHelper = new DBHelper(sql);//创建DBHelper对象
		log.info("查询班车计划执行的SQL："+sql);
		try {
			ret = dbHelper.pst.executeQuery();
			if(ret.next()){
				//判断可以购票之后，添加参数，之后调用锁票的方法
				params.put("szDate", ret.getString("time").split(" ")[0]); //传入参数，根据班次ID查询 time
				params.put("szNo", ret.getString("no"));//传入参数，班次编号，根据班次ID查询 no
				params.put("szTime",ret.getString("time").split(" ")[1]);//传入参数，班车发车时间，根据班次ID查询 time
				params.put("Time",ret.getString("time1"));//传入参数，班车发车时间，根据班次ID查询 time
				params.put("szOwnerDepot", ret.getString("ownerDepot"));//传入参数，班次所属站代码，根据班次ID查询 
				//params.put("szLocalDepot", ret.getString("localDepot"));//传入参数，操作站编码，根据班次ID查询 localDepot
				params.put("szLocalDepot", "0008");//传入参数，操作站编码，根据班次ID查询 localDepot
				params.put("szArea", ret.getString("areaDepot"));//传入参数，发车区域，根据班次ID查询,areaDepot
				params.put("szStCode",ret.getString("terminalCode"));//传入参数，到站编码，根据班次ID查询terminalCode
				params.put("desStation",ret.getString("name"));//到站名称
				params.put("szAreaDepot",ret.getString("areaDepot"));////传入参数，票源客运站代码，根据班次ID查询
				params.put("name", ret.getString("name"));
				params.put("szDepot",  ret.getString("ownerDepot"));////传入参数，票源客运站代码，根据班次ID查询
				String depStation=ret.getString("localDepot");
				System.out.println("================="+depStation);
				if("0011".equals(depStation)){
					params.put("depStation", "省客运站");
				}
				
				if("0013".equals(depStation)){
					params.put("depStation", "天河客运站");
				}
				
				if("0028".equals(depStation)){
					params.put("depStation", "东站客运站");
				}
				
				if("0060".equals(depStation)){
					params.put("depStation", "番禺客运站");
				}
				
				if("0016".equals(depStation)){
					params.put("depStation", "罗冲围客运站");
				}
				
				if("0015".equals(depStation)){
					params.put("depStation", "越秀南站");
				}
				
				if("0018".equals(depStation)){
					params.put("depStation", "广园客运站");
				}
				
				
			}
		} catch (SQLException e1) {
			return null;
		}
		log.info("查询班车计划执行返回的数据："+net.sf.json.JSONArray.fromObject(params));
		//其他参数可以加入
		return params;
	}
	
	/***
	 * 根据班次ID，修改排班计划
	 * @param busId 班车计划ID
	 * @return 返回班车信息集合(发车时间,班次日期,班次名称,等信息)
	 * @throws Exception
	 */
	public static void busScheduleUpdate(
		Map<String, Object> params,String busId) throws Exception{
		//传入班次ID，获取班次信息，票价
		try {
			String sql= "update schemerefresh set remain=?,seat=? where id=?"; 
			conn=DBHelper.getConnection();
			ps = conn.prepareStatement(sql); 
			ps.setObject(1, params.get("iRemain"));
			ps.setObject(2, params.get("szSeatMap"));
			ps.setString(3, busId);
			int i = ps.executeUpdate();
			log.info("更新班车计划参数：busId："+busId+"结果:"+i+"  params:"+net.sf.json.JSONArray.fromObject(params));
		} catch (SQLException e1) {
			log.info("更新班车计划异常"+e1.getMessage());
			e1.printStackTrace();
		}
	}
	
	/***
	 * 根据支付成功的订单ID查找订单信息，用于更新车票
	 * @param orderId
	 * @return 返回集合(发车时间，班次ID，购票人等)
	 * @throws Exception
	 */
	public static Map<String,Object> ticketOrderInfo(
			String orderId) throws Exception{
		Map<String, Object> ticketInfo=new HashMap<String, Object>();
		//传入班次ID，获取班次信息，票价
		sql = "select *from Bus_Schedule";//SQL语句
		dbHelper = new DBHelper(sql);//创建DBHelper对象
		//在此处执行查询的方法
		try {
			ret = dbHelper.pst.executeQuery();
			if(ret.next()){
				//判断可以购票之后，添加参数，之后调用锁票的方法
				ticketInfo.put("szDate", "20151208"); //传入参数，根据班次ID查询
				ticketInfo.put("szNo", "ASH1H036");//传入参数，班次编号，根据班次ID查询
				ticketInfo.put("szStCode","8011");//传入参数，到站编码，根据班次ID查询
				ticketInfo.put("szTime","18:50");//传入参数，班车发车时间，根据班次ID查询
				ticketInfo.put("szStCode","18:50");//传入参数，到站代码，根据班次ID查询
				ticketInfo.put("szDepot", "0001");////传入参数，票源客运站代码，根据班次ID查询
				ticketInfo.put("szOwnerDepot", "0001");//传入参数，班次所属站代码，根据班次ID查询
				ticketInfo.put("szLocalDepot", "0001");//传入参数，操作站编码，根据班次ID查询
				ticketInfo.put("szArea", "0001");//传入参数，发车区域，根据班次ID查询
			}
		} catch (SQLException e1) {
			return null;
		}
		//其他参数可以加入
		return ticketInfo;
	}
	
	public static boolean ticketOrderUpdate(String orderId,String sumbarcode){
		//传入班次ID，获取班次信息，票价
		sql = "select *from Bus_Schedule";//SQL语句
		dbHelper = new DBHelper(sql);//创建DBHelper对象
		//在此处执行查询的方法
		return false;
	}
	
	
	
	/***
	 * 创建车票订单
	 * @param orderId
	 * @return 返回集合(发车时间，班次ID，购票人等)
	 * @throws Exception
	 */
	public static int createTicketOrder(Map<String, Object> orderInfo) throws Exception{
		PreparedStatement pstmt=null;
		String sql_01="INSERT INTO  alipayorder("
				+"dbBaf,"
				+"dbFare,"
				+"szAntiCode,"
				+"szAreaDepot,"
				+"szBarCode,"
				+"szCName,"
				+"szDate,"
				+"szFareType,"
				+"szIDCard,"
				+"szLocalDepot,"
				+"szNo,"
				+"szOldBarCode,"
				+"szOrderid,"
				+"szOwnerDepot,"
				+"szSeats,"
				+"szStCode,"
				+"szStaffCode,"
				+"szTelephone,"
				+"szTime,"
				+"szType,"
				+"syUserID,"
				+"syUsername,"
				+"syaliAccount,"
				+"syorderId,"
				+"syOdAddress,"
				+"syBusRide,"
				+"sydepStation,"
				+"sydesStation,"
				+"syumbers,"
				+"syAmount,state,collecttime,szEntry,createtime) VALUES(?,?,?,?,?,?,STR_TO_DATE(?,'%Y%m%d'),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,DATE_FORMAT(NOW(),'%Y%m%d %H:%i:%s'),?,NOW())";
	    conn=DBHelper.getConnection();
	    int result=0;
	    try {
	        pstmt = (PreparedStatement) conn.prepareStatement(sql_01);
	        pstmt.setString(1, orderInfo.get("dbBaf")==null?"未知":orderInfo.get("dbBaf").toString()); 
	        pstmt.setString(2, orderInfo.get("dbFare")==null?"未知":orderInfo.get("dbFare").toString()); 
	        pstmt.setString(3, orderInfo.get("szAntiCode")==null?"未知":orderInfo.get("szAntiCode").toString()); 
	        pstmt.setString(4, orderInfo.get("szAreaDepot")==null?"未知":orderInfo.get("szAreaDepot").toString()); 
	        pstmt.setString(5, orderInfo.get("szBarCode")==null?"未知":orderInfo.get("szBarCode").toString()); 
	        pstmt.setString(6, orderInfo.get("szCName")==null?"未知":orderInfo.get("szCName").toString()); 
	        pstmt.setString(7, orderInfo.get("szDate")==null?"未知":orderInfo.get("szDate").toString()); 
	        pstmt.setString(8, orderInfo.get("szFareType")==null?"未知":orderInfo.get("szFareType").toString()); 
	        pstmt.setString(9, orderInfo.get("szIDCard")==null?"未知":orderInfo.get("szIDCard").toString()); 
	        pstmt.setString(10, orderInfo.get("szLocalDepot")==null?"未知":orderInfo.get("szLocalDepot").toString()); 
	        pstmt.setString(11, orderInfo.get("szNo")==null?"未知":orderInfo.get("szNo").toString()); 
	        pstmt.setString(12, orderInfo.get("szOldBarCode")==null?"未知":orderInfo.get("szOldBarCode").toString());
	        pstmt.setString(13, orderInfo.get("szOrderid")==null?"未知":orderInfo.get("szOrderid").toString()); 
	        pstmt.setString(14, orderInfo.get("szOwnerDepot")==null?"未知":orderInfo.get("szOwnerDepot").toString()); 
	        //判断是否是番禺站
	        
	        pstmt.setString(15, orderInfo.get("szSeats")==null?"未知":orderInfo.get("szSeats").toString()); 
	        pstmt.setString(16, orderInfo.get("szStCode")==null?"未知":orderInfo.get("szStCode").toString()); 
	        pstmt.setString(17, orderInfo.get("szStaffCode")==null?"未知":orderInfo.get("szStaffCode").toString()); 
	        pstmt.setString(18, orderInfo.get("szTelephone")==null?"未知":orderInfo.get("szTelephone").toString()); 
	        pstmt.setString(19, orderInfo.get("szTime")==null?"未知":orderInfo.get("szTime").toString()); 
	        pstmt.setString(20, orderInfo.get("szType")==null?"未知":orderInfo.get("szType").toString()); 
	        pstmt.setString(21, orderInfo.get("userId")==null?"未知":orderInfo.get("userId").toString()); 
	        pstmt.setString(22, orderInfo.get("syUsername")==null?"未知":orderInfo.get("syUsername").toString()); 
	        pstmt.setString(23, orderInfo.get("syaliAccount")==null?"未知":orderInfo.get("syaliAccount").toString()); 
	        pstmt.setString(24, orderInfo.get("alipayCode")==null?"未知":orderInfo.get("alipayCode").toString());
	        pstmt.setString(25, orderInfo.get("syOdAddress")==null?"未知":orderInfo.get("syOdAddress").toString());
	        pstmt.setString(26, orderInfo.get("riders")==null?"未知":orderInfo.get("riders").toString());
	        pstmt.setString(27, orderInfo.get("depStation")==null?"未知":orderInfo.get("depStation").toString());
	        pstmt.setString(28, orderInfo.get("name")==null?"未知":orderInfo.get("name").toString());
	        pstmt.setString(29, orderInfo.get("iTickets")==null?"未知":orderInfo.get("iTickets").toString());
	        pstmt.setString(30, orderInfo.get("ticketAmount")==null?"未知":orderInfo.get("ticketAmount").toString());
	        pstmt.setString(31, "未付款");
	        pstmt.setString(32, orderInfo.get("szEntry")==null?"未知":orderInfo.get("szEntry").toString());
	        result = pstmt.executeUpdate();
	        pstmt.close();
	        conn.close();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }finally{
	    	 pstmt.close();
		     conn.close();
	    	
	    }
	    return result;
	}
	
	public static String getParame(String proname) throws SQLException{
		  try {
		      conn =  DBHelper.getConnection();
		      CallableStatement proc = null;
		      proc = (CallableStatement) conn.prepareCall("{ call "+proname+"(?) }");
		      proc.registerOutParameter(1,Types.VARCHAR);
		      proc.execute();
		      return proc.getString(1);
		   }catch (SQLException ex2) {
		       ex2.printStackTrace();
		       return "";
		   }finally{
			   conn.close();
		   }
	}
	
	@SuppressWarnings("null")
	public static String updateOrderInfo(String orderId) throws SQLException{
	  try {
	      conn =  DBHelper.getConnection();
	      CallableStatement proc = null;
	      //proc = (CallableStatement) conn.prepareCall("{ call "+proname+"(?) }");
	      proc.registerOutParameter(1,Types.VARCHAR);
	      proc.execute();
	      return proc.getString(1);
	   }catch (SQLException ex2) {
	       ex2.printStackTrace();
	       return "";
	   }finally{
		   conn.close();
	   }
	}
	
	public static Map<String, Object> queryOrderInfo(String orderId) throws SQLException{
	 
		Map<String, Object> result=new HashMap<String, Object>();
			conn =  DBHelper.getConnection();
			String sql="SELECT * FROM alipayorder WHERE syOrderId=?";
			ps=conn.prepareStatement(sql);
			ps.setString(1, orderId);
		    rs=ps.executeQuery();
			if(rs.next()){
				result.put("createtime", rs.getObject("createtime"));
				result.put("szCName", rs.getObject("szCName"));
				result.put("szIDCard", rs.getObject("szIDCard"));
  				result.put("szTelephone", rs.getObject("szTelephone"));
				result.put("state", rs.getObject("state"));
				result.put("dbBaf", rs.getObject("dbBaf"));
				result.put("dbFare", rs.getObject("dbFare"));
				result.put("szAntiCode", rs.getObject("szAntiCode"));
				result.put("szAreaDepot", rs.getObject("szAreaDepot"));
  				result.put("szBarCode", rs.getObject("szBarCode"));
  				result.put("Date", rs.getString("szDate"));
  				String szDate=rs.getString("szDate").replaceAll("-", "");
  				result.put("szDate", szDate);
  				result.put("szFareType", rs.getObject("szFareType"));
  				result.put("szLocalDepot", rs.getObject("szLocalDepot"));
  				result.put("szNo", rs.getObject("szNo"));
  				result.put("szOldBarCode", rs.getObject("szOldBarCode"));
  				result.put("szOrderid", rs.getObject("szOrderid"));
  				result.put("szOwnerDepot", rs.getObject("szOwnerDepot"));
  				
  				//判断座位号
  				String szSeats=rs.getString("szSeats");
  				if(szSeats.indexOf(",")>0){
  					
  					result.put("szSeats", rs.getObject("szSeats"));
  				}else{
  					result.put("szSeats", rs.getObject("szSeats")+",");
  				}
  				
  				result.put("szSeats", rs.getObject("szSeats"));
  				result.put("szStCode", rs.getObject("szStCode"));
  				result.put("szStaffCode", rs.getObject("szStaffCode"));

  				result.put("szTime", rs.getObject("szTime"));
  				result.put("szType", rs.getObject("szType"));
  				
  				
  				result.put("syUserID", rs.getObject("syUserID"));
  				result.put("syUsername", rs.getObject("syUsername"));
  				result.put("syaliAccount", rs.getObject("syaliAccount"));
  				result.put("syorderId", rs.getObject("syorderId"));
  				result.put("syOdAddress", rs.getObject("syOdAddress"));
  				result.put("syBusRide", rs.getObject("syBusRide"));
  				result.put("sydepStation", rs.getObject("sydepStation"));
  				result.put("sydesStation", rs.getObject("sydesStation"));
  				result.put("syumbers", rs.getObject("syumbers"));
  				result.put("syAmount", rs.getObject("syAmount"));
  				result.put("szEntry", rs.getObject("szEntry"));
  				
  				//覆盖条形码
	      }
		  ps.close();
		  conn.close();
	  return result;
	}
	
	
	
	/***
	 * 根据订单ID修改订单状态
	 * @param orderId 订单ID
	 * @return 
	 * @throws Exception
	 */
	public static int orderUpdate(String orderId,String trade_no,String state,String confirmBarCode) throws Exception{
		int i = 0;
		//传入班次ID，获取班次信息，票价
		try {
			String sql= "UPDATE alipayorder SET state=? ,trade_no=?, confirmBarCode=? WHERE syorderId=?"; 
			conn=DBHelper.getConnection();
			ps = conn.prepareStatement(sql); 
			ps.setString(1, state);
			ps.setString(2, trade_no);
			ps.setString(3, confirmBarCode);
			ps.setString(4, orderId);
			i = ps.executeUpdate();
			return i;
		} catch (SQLException e1) {
			e1.printStackTrace();
		}finally{
			ps.close();
			conn.close();
		}
		return i;
	}
	
	public static void main(String[] args) throws Exception {
		Map<String, Object> params=new HashMap<String, Object>();
		//废票操作
		params.put("szBarCode", "0800001286,");
		params.put("szOrderid", "JT00001122");
		params.put("szLocalDepot", "0008");
		
		params.put("szAreaDepot",  "0060");
		params.put("szOwnerDepot", "0060");
		params.put("szStaffCode", "J0001");
		params.put("szTime", "20160120 092000");
		params.put("szOper", "1234567890");
		new StationDataObtain().ticketBlankout(params, "0060");
		
		/*
		params.put("szSeat", "");
		params.put("szType", "1");
		params.put("szLocalDepot", "0008");
		params.put("szHostname", "0001.JT00001043@10");
		new ProvinceStationDataSync().lockCancel(params, "0060");
		*/
	}
}
