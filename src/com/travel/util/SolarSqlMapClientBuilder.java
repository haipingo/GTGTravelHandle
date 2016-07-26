package com.travel.util;

import com.ibatis.sqlmap.client.SqlMapClient;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

// Referenced classes of package gzkit.common.spring.orm.ibatis:
//            KitSqlMapConfigParser

public class SolarSqlMapClientBuilder {

	protected SolarSqlMapClientBuilder() {
	}

	public static SqlMapClient buildSqlMapClient(Reader reader) {
		return (new SolarSqlMapConfigParser()).parse(reader);
	}

	public static SqlMapClient buildSqlMapClient(Reader reader, Properties props) {
		return (new SolarSqlMapConfigParser()).parse(reader, props);
	}

	public static SqlMapClient buildSqlMapClient(InputStream inputStream) {
		return (new SolarSqlMapConfigParser()).parse(inputStream);
	}

	public static SqlMapClient buildSqlMapClient(InputStream inputStream,
			Properties props) {
		return (new SolarSqlMapConfigParser()).parse(inputStream, props);
	}
}


/*
	DECOMPILATION REPORT

	Decompiled from: D:\MyEclipse10\Workspaces\odsms\WebRoot\WEB-INF\lib\kitFramework_Release_V1.1.1.jar
	Total time: 47 ms
	Jad reported messages/errors:
The class file version is 49.0 (only 45.3, 46.0 and 47.0 are supported)
	Exit status: 0
	Caught exceptions:
*/