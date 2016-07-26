package com.travel.ticktes.action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.travel.base.BaseController;
import com.travel.base.StationDataObtain;
import com.travel.ticktes.dao.QuerySchemerefreshImpl;
import com.travel.util.StationCode;
/**
 * 班线计划查询，包括查询班次计划、查询实时车票信息
 * @author xiehaiping
 *
 */
@SuppressWarnings("rawtypes")
@Controller
@RequestMapping(value="/ticktes")
public class QuerySchemerefreshController extends BaseController {
	private Log log=LogFactory.getLog(QuerySchemerefreshController.class);
	
	@Resource(name="querySchemerefreshImpl")
	private QuerySchemerefreshImpl querySchemerefreshImpl;
	
	
	/**
	 * 查询班线计划
	 * @param 始发站编码、到达站编码 、出发时间
	 * @throws IOException 
	 * @user xiehaiping 
	 * 2016-07-19
	 */
	@RequestMapping(value = "/querySchemerefresh", method = RequestMethod.POST)
	public void querySchemerefresh(HttpServletRequest request) throws IOException{
		PrintWriter out = response.getWriter();  
		log.info("日志记录");
		//此处获取前端请求参数
		String departureStation=request.getParameter("departureStation");
		String destinationStation=request.getParameter("destinationStation");
		String departureDate=request.getParameter("departureDate");
		
		System.out.println("departureStation:"+departureStation);
		System.out.println("destinationStation:"+destinationStation);
		System.out.println("departureDate:"+departureDate);
		
		Map<String, String> conditionMap=new HashMap<String, String>();
		conditionMap.put("departureStation", "0011");
		conditionMap.put("destinationStation", "1010");
		conditionMap.put("departureDate", "2016-07-27");
		
		List result=querySchemerefreshImpl.querySchemerefresh(conditionMap);
		
		
		
		System.out.println(JSONArray.fromObject(result));
	    out.println(JSONArray.fromObject(result));
	}
	
	
	/**
	 * 根据班次ID查询 班线计划
	 * @param 班次ID
	 * @throws Exception 
	 * @user xiehaiping 
	 * 2016-07-19
	 */
	@RequestMapping(value = "/queryTickteInfo", method = RequestMethod.GET)
	public void queryTickteInfo(HttpServletRequest request) throws Exception{
		PrintWriter out = response.getWriter();  
		
		//获取请求的参数ID
		String id=request.getParameter("id");
		System.out.println("请求的班次ID"+id);
		Map<String, String> conditionMaps=new HashMap<String, String>();
		conditionMaps.put("id", id); //班次ID,作为条件传入
		List resultList=querySchemerefreshImpl.querySchemerefresh(conditionMaps);
		Map entity =null;
		for (Object result : resultList) {
			entity = (Map) result;
		}
		//座位参数，查询实时班次计划
		StationDataObtain operation=new StationDataObtain();
		//获取班次的所属站
		String areaDepotCode=(String) entity.get("ownerDepot");
		Map<String, Object> priceMap=new HashMap<String, Object>();
		
		
		//查票
        //params.put("szDate", 		entity.get("szDate"));
		
		priceMap.put("szDate", 			"20160727");
		priceMap.put("szNo", 			entity.get("no"));
		priceMap.put("szStCode", 		entity.get("terminalCode"));
		priceMap.put("szOwnerDepot",	entity.get("ownerDepot"));
		priceMap.put("szDepot", 		entity.get("localDepot"));
		priceMap.put("szArea", 		    entity.get("area"));
		priceMap.put("szLocalDepot", 	entity.get("localDepot"));
		Map<String, Object> priceParams=operation.priceInfo(priceMap,areaDepotCode);
		
		//将查询到的结果响应给请求端
		priceParams.put("id", 			   id);
		priceParams.put("departureTime",   entity.get("departureTime")); //出发时间
		priceParams.put("localStation",    StationCode.getStation((String)entity.get("localDepot"))); 	 //到达站
		priceParams.put("desStation",      entity.get("name")); //出发时间
		priceParams.put("boarLocal",    "正佳广场");       // 上车地点
		out.println(JSONArray.fromObject(priceParams));
	}
	
	
	
	
	
	/**
	 * 添加订单信息
	 * @param 班次ID、票数、票价、等信息
	 * @throws Exception 
	 * @user xiehaiping 
	 * 2016-07-21
	 */
	@RequestMapping(value = "/addTickteInfo", method = RequestMethod.GET)
	public void addTickteInfo(HttpServletRequest request) throws Exception{
		PrintWriter out = response.getWriter();  
		
		
		//获取请求的参数ID
		String id=request.getParameter("id");
		
		//拼接乘车人
		
		//生成订单
		
		//返回订单信息
		
		System.out.println("请求的班次ID"+id);
		Map<String, String> conditionMaps=new HashMap<String, String>();
		conditionMaps.put("id", id); //班次ID,作为条件传入
		List resultList=querySchemerefreshImpl.querySchemerefresh(conditionMaps);
		Map entity =null;
		for (Object result : resultList) {
			entity = (Map) result;
		}
		
		
		//座位参数，查询实时班次计划
		StationDataObtain operation=new StationDataObtain();
		
		//获取班次的所属站
		String areaDepotCode=(String) entity.get("ownerDepot");
		Map<String, Object> priceMap=new HashMap<String, Object>();
		
		//查票
        //params.put("szDate", 		entity.get("szDate"));
		priceMap.put("szDate", 			"20160722");
		priceMap.put("szNo", 			entity.get("no"));
		priceMap.put("szStCode", 		entity.get("terminalCode"));
		priceMap.put("szOwnerDepot",	entity.get("ownerDepot"));
		priceMap.put("szDepot", 		entity.get("localDepot"));
		priceMap.put("szArea", 		entity.get("area"));
		priceMap.put("szLocalDepot", 	entity.get("localDepot"));
		Map<String, Object> priceParams=operation.priceInfo(priceMap,areaDepotCode);
		
		//将查询到的结果响应给请求端
		priceParams.put("departureTime",   entity.get("departureTime")); //出发时间
		priceParams.put("localStation", "省汽车客运站"); 	 //到达站
		priceParams.put("desStation",   "汕尾(汕尾路口)");      //到达站
		priceParams.put("boarLocal",    "正佳广场");       // 上车地点
		out.println(JSONArray.fromObject(priceParams));
	} 
	
	
	
	
	
	
	
	
	public static void main(String[] args) throws Exception {
		Map<String, Object> params=new HashMap<String, Object>();
		//查票
        params.put("szDate", "20160721");
		params.put("szNo", "CPS38977");
		params.put("szStCode", "9113");
		params.put("szOwnerDepot", "0013");
		params.put("szDepot", "0013");
		params.put("szArea", "广州天河客运站");
		params.put("szLocalDepot", "0011");
		System.out.println("执行...");
		new StationDataObtain().priceInfo(params, "0013");
		
		
	}
	
}
