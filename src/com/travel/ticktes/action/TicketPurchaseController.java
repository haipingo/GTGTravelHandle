package com.travel.ticktes.action;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.travel.ticktes.dao.TicketPurchaseImpl;
import com.travel.util.StationCode;

/**
 * 车票购买流程页面 包括锁票、生成订单 
 * @author Administrator
 *
 */
@SuppressWarnings("rawtypes")
@Controller
@RequestMapping(value="/purchase")
public class TicketPurchaseController  extends BaseController {

		private Log log=LogFactory.getLog(TicketPurchaseController.class);
		
		@Resource(name="ticketPurchaseImpl")
		private TicketPurchaseImpl ticketPurchaseImpl;
		
		
		@Resource(name="querySchemerefreshImpl")
		private QuerySchemerefreshImpl querySchemerefreshImpl;
		
		/**
		 * 锁票 
		 * @param 班次ID
		 * @throws Exception 
		 * @user xiehaiping 
		 * 2016-07-19
		 */
		@RequestMapping(value = "/lockTicket", method = RequestMethod.GET)
		public void lockTicket(HttpServletRequest request) throws Exception{
			PrintWriter out = response.getWriter();  
			
			log.info("该方法已经执行");
			//获取请求的参数ID
			String id=request.getParameter("id");
			
			//票数(乘车人数):<input name="ticketNumbers"  type="text"/>
			//int ticketNumbers=Integer.parseInt(request.getParameter("ticketNumbers"));
			int ticketNumbers=1;
			
			//总金额:<input name="ticketAmount"  type="text"/>
			String ticketAmount=request.getParameter("ticketAmount");
			//String ticketAmount="128";
			//取票人:<input name="ticketHolder"  type="text"/>
			String ticketHolder=request.getParameter("ticketHolder");
			//String ticketHolder="谢海平,431021199210026537,178680543711";
			//乘车人:<input name="riders"  type="text"/>
			String riders=request.getParameter("riders");
			//String riders="谢海平,431021199210026537,178680543711";
			
			
			
			
			System.out.println("请求的班次ID"+id);
			Map<String, String> conditionMaps=new HashMap<String, String>();
			conditionMaps.put("id", id); //班次ID,作为条件传入
			List resultList=querySchemerefreshImpl.querySchemerefresh(conditionMaps);
			Map priceEntity =null;
			for (Object result : resultList) {
				priceEntity = (Map) result;
			}
			
			//座位参数，查询实时班次计划
			StationDataObtain operation=new StationDataObtain();
			//获取班次的所属站
			String areaDepotCode=(String) priceEntity.get("ownerDepot");
			
			Map<String, Object> priceMap=new HashMap<String, Object>();
			//查票参数
			priceMap.put("szDate", 			priceEntity.get("szDate"));
			priceMap.put("szNo", 			priceEntity.get("no"));
			priceMap.put("szStCode", 		priceEntity.get("terminalCode"));
			priceMap.put("szOwnerDepot",	priceEntity.get("ownerDepot"));
			priceMap.put("szDepot", 		priceEntity.get("localDepot"));
			priceMap.put("szArea", 		    priceEntity.get("area"));
			priceMap.put("szLocalDepot", 	priceEntity.get("localDepot"));
			
			Map<String, Object> priceParams=operation.priceInfo(priceMap,areaDepotCode);
			if(ticketNumbers>Integer.parseInt(priceParams.get("iRemain").toString())){
				
				return;
			}
			
			
			
			Map<String, Object> busParams=new HashMap<String, Object>();
			busParams.put("dbFare",  priceParams.get("szFare")+",");
			busParams.put("dbBaf",   priceParams.get("dbBaf")+",");
			
			
			
			
			
			
			//余票
			//String iRemain=(String)priceParams.get("iRemain");
			
			//票价
			//String szFare=(String)priceParams.get("szFare");
			
			//锁票参数
			Map<String, Object> lockMap=new HashMap<String, Object>();
			lockMap.put("szDate",  priceMap.get("szDate"));
			lockMap.put("szNo",    priceMap.get("szNo"));
			lockMap.put("szStCode",priceMap.get("szStCode"));
			
			lockMap.put("szOwnerDepot", priceMap.get("szOwnerDepot"));
			lockMap.put("szDepot", 		priceMap.get("szDepot"));
			lockMap.put("iFrees", "0");
			lockMap.put("szSeat", "");
			lockMap.put("szHostname", "0008.JT00023348@10");
			lockMap.put("szLocalDepot", "0008");//固定值，集团购票
			lockMap.put("szType", "1"); 		
			lockMap.put("iTickets", 1);
			
			Map<String, Object> lockParams=operation.ticketLock(lockMap,"0011");
			
			//判断锁票是否成功
			String lockMapState=(String) lockParams.get("State");
			if(!"true".equals(lockMapState)){
				//锁票失败
				
			}
			
			
			String orderId="123";
			String barCode="";
			String alipayCode="123";
			String dbFare="";
			String dbBaf="";
			String szFareType="";
			try {
				//调用存储过程，生成订单 唯一
				orderId=StationDataObtain.getParame("autoTicketAdd");
				
				//生成支付宝订单，唯一
				alipayCode=StationDataObtain.getParame("autoAlipayAdd");
				
				for (int i = 0; i < ticketNumbers; i++) {
					//生成电子票号
					String code=StationDataObtain.getParame("autoBarCodeAdd");
					barCode+=code+",";
					dbFare+=busParams.get("dbFare");
					dbBaf+=busParams.get("dbBaf");
					szFareType+="1,";
				}
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
			busParams.put("iTickets", ticketNumbers);
			busParams.put("iFrees", "0");
			busParams.put("szSeat", "");
			busParams.put("szType", "1");
			
			//此处用到操作终端编号，默认集团，0008
			
			busParams.put("szHostname", "0008."+orderId+"@10");//此处产生订单ID，调用后台的序列或者(函数)
			busParams.put("ticketNumbers", ticketNumbers);
			busParams.put("ticketAmount", ticketAmount);
			busParams.put("ticketHolder", ticketHolder);
			busParams.put("riders", riders);
			busParams.put("alipayCode", alipayCode);
			busParams.put("barCode", barCode);
			busParams.put("dbBaf",dbBaf);
			busParams.put("dbFare",dbFare);
			busParams.put("szBarCode",barCode);
			busParams.put("szOldBarCode",barCode);
			busParams.put("szFareType",szFareType);
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd HHmmss");//设置日期格式
			busParams.put("szSellTime",df.format(new Date()));
			busParams.put("szOrderid",orderId);
			busParams.put("szAntiCode","ABCDEFG,");
			busParams.put("szIDCard",ticketHolder.split(",")[1]);
			busParams.put("szTelephone",ticketHolder.split(",")[2]);
			busParams.put("szCName",ticketHolder.split(",")[0]);
			busParams.put("szStaffCode","J0001");
			busParams.put("szSeats", lockParams.get("szLockSeat"));
			//busParams.put("szLockSeat", lockMap.get("szLockSeat"));
			
			busParams.put("szData",     priceEntity.get("szDate")); //出发时间
			busParams.put("szTime",     priceEntity.get("time")); //出发时间
			busParams.put("departureTime",     priceEntity.get("departureTime")); //出发时间
			busParams.put("sydepStation",      StationCode.getStation((String)priceEntity.get("localDepot"))); 	 //出发站
			busParams.put("sydesStation",      priceEntity.get("name")); //到达站
			busParams.put("boarLocal",    	   "正佳广场");       // 上车地点
			busParams.put("szAreaDepot",    priceEntity.get("areaDepot")); //上车区域编码
			
			
			
			System.out.println("订单参数参数"+JSONArray.fromObject(busParams));
			
			int addResult=ticketPurchaseImpl.addTicketInfo(busParams);
			if(addResult>0){
				out.print(JSONArray.fromObject(busParams));
			}
			
			
			//创建订单
			
			//拼凑更新票参数(订单参数)
			
			
			
			//取票人电话号码
			
			
			System.out.println(JSONArray.fromObject(lockParams));
		}

		
}


