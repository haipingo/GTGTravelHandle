package com.travel.base;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.travel.util.UUIDUtil;



/**
 * BaseController 类
 * @author 谢海平
 *
 */

public class BaseController {
	
	private static Log log=LogFactory.getLog(BaseController.class);

	@SuppressWarnings("unused")
	private static final long serialVersionUID = 6357869213649815390L;
	
	
	
	/**得到request对象
	 * @return 
	 */
	public HttpServletRequest getRequest() {
		HttpServletRequest request = ((ServletRequestAttributes)
				RequestContextHolder.getRequestAttributes()).getRequest();
		
		log.info("获取请求路径,URL地址为："+request.getRequestURI());
		return request;
	}

	/**得到32位的uuid
	 * @return
	 */
	public String get32UUID(){
		return UUIDUtil.get32UUID();
	}
	
	
	protected HttpServletRequest request;
    protected HttpServletResponse response;  
    protected HttpSession session;  
      
    @ModelAttribute  
    public void setReqAndRes(HttpServletRequest request, HttpServletResponse response){  
        this.request = request;  
        this.response = response;
        this.response.setContentType("text/html; charset=UTF-8");  
        this.session = request.getSession();  
    }
	
}
