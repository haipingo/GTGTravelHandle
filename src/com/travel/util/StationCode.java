package com.travel.util;
import java.util.HashMap;
import java.util.Map;

/**
 * 站场工具类，用于根据站场编码返回站场
 * @author Administrator
 *
 */
public class StationCode {

	public static String getStation(String code) {
		Map<String, String> map=new HashMap<String, String>();
		map.put("0011", "省客运站");  
		map.put("0013", "天河客运站");
		map.put("0028", "东站客运站");
		map.put("0016", "罗冲围客运站 ");
		map.put("0018", "广园客运站"); 
		map.put("0060", "番禺客运站"); 
		map.put("0015", "越秀南站");
		map.put("0025", "黄埔客运站");
		map.put("0022", "南沙客运站");
		return map.get(code)==null?"未知":map.get(code);
		
	}
	public static void main(String[] args) {
		System.out.println(getStation("0013"));
	}
}
