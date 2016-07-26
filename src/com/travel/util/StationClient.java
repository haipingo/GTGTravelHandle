package com.travel.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;




/**
 * 
 * @ClassName: StationClient
 * @Description: 站场 httpClient post 请求
 * @author wwd
 * @date 2015年9月15日 下午2:07:12 © Copyright 续日科技
 *
 */
@SuppressWarnings("deprecation")
public class StationClient {

	/**
	 * 根据传入的参数执行post 请求 返回执行的结果数据
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public String execute(String host,String params) throws Exception {
		HttpResponse response = null;
		
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(host);
			post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
		    post.setHeader("Content-Type", "text/json;charset=UTF-8");
			post.setHeader("Accept-Encoding","gzip,deflate");
			post.setEntity(new StringEntity(params, "text/html", "UTF-8"));
			response = client.execute(post);
			StatusLine sl = response.getStatusLine();
			int code = sl.getStatusCode();
			if (code != HttpURLConnection.HTTP_OK) {
				throw new IOException(sl.toString());
			}
			Header[] headers = response.getHeaders("Content-Encoding");
			boolean isGzip = false;
			for(Header h:headers){
			    if(h.getValue().equals("gzip")){
			       isGzip = true;
			    }
			}
			HttpEntity entity = null;
			if(isGzip){
				entity=new GzipDecompressingEntity(response.getEntity());
			}else{
				entity=response.getEntity();
			}
			InputStream in = entity.getContent();
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[10240];
			int len;
			while (-1 != (len = in.read(buf))) {
				bos.write(buf, 0, len);
			}
			in.close();
			String json = bos.toString("utf-8");
			bos.close();
			return json;
			
		} finally {
			if (null != response) {
				try {
					EntityUtils.consume(response.getEntity());
				} catch (Exception e) {
				}
			}
		}
	}

}
