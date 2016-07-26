package com.travel.util;


import com.ibatis.sqlmap.client.SqlMapClient;
import java.io.IOException;
import java.util.Properties;
import org.springframework.core.io.Resource;
import org.springframework.orm.ibatis.SqlMapClientFactoryBean;
@SuppressWarnings("deprecation")
public class SolarSqlMapClientFactoryBean extends SqlMapClientFactoryBean {
	public SolarSqlMapClientFactoryBean() {
	}

	protected SqlMapClient buildSqlMapClient(Resource configLocation,
			Properties properties) throws IOException {
		java.io.InputStream is = configLocation.getInputStream();
		if (properties != null)
			return SolarSqlMapClientBuilder.buildSqlMapClient(is, properties);
		else
			return SolarSqlMapClientBuilder.buildSqlMapClient(is);
	}
}
