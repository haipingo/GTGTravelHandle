package com.travel.util;

import com.ibatis.common.resources.Resources;
import com.ibatis.common.xml.Nodelet;
import com.ibatis.common.xml.NodeletParser;
import com.ibatis.common.xml.NodeletUtils;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapException;
import com.ibatis.sqlmap.engine.builder.xml.SqlMapClasspathEntityResolver;
import com.ibatis.sqlmap.engine.builder.xml.SqlMapParser;
import com.ibatis.sqlmap.engine.builder.xml.XmlParserState;
import com.ibatis.sqlmap.engine.config.SqlMapConfiguration;
import com.ibatis.sqlmap.engine.datasource.DataSourceFactory;
import com.ibatis.sqlmap.engine.mapping.result.ResultObjectFactory;
import com.ibatis.sqlmap.engine.transaction.TransactionConfig;
import com.ibatis.sqlmap.engine.transaction.TransactionManager;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Node;

public class SolarSqlMapConfigParser
{
  protected final NodeletParser parser = new NodeletParser();
  private XmlParserState state = new XmlParserState();

  private boolean usingStreams = false;

  public SolarSqlMapConfigParser() {
    this.parser.setValidation(true);
    this.parser.setEntityResolver(new SqlMapClasspathEntityResolver());

    addSqlMapConfigNodelets();
    addGlobalPropNodelets();
    addSettingsNodelets();
    addTypeAliasNodelets();
    addTypeHandlerNodelets();
    addTransactionManagerNodelets();
    addSqlMapNodelets();
    addResultObjectFactoryNodelets();
    addTypeHandler();
  }

  protected void addTypeHandler()
  {
    this.state.getConfig().getTypeHandlerFactory().register(Object.class, new SolarObjectTypeHandler());
    this.state.getConfig().getTypeHandlerFactory().register(Object.class, "OBJECT", new SolarObjectTypeHandler());
  }

  public SqlMapClient parse(Reader reader, Properties props) {
    if (props != null) this.state.setGlobalProps(props);
    return parse(reader);
  }

  public SqlMapClient parse(Reader reader) {
    try {
      this.usingStreams = false;

      this.parser.parse(reader);
      return this.state.getConfig().getClient(); } catch (Exception e) {
    	  throw new RuntimeException("Error occurred.  Cause: " + e, e);
    }
    
  }

  public SqlMapClient parse(InputStream inputStream, Properties props)
  {
    if (props != null) this.state.setGlobalProps(props);
    return parse(inputStream);
  }

  public SqlMapClient parse(InputStream inputStream) {
    try {
      this.usingStreams = true;

      this.parser.parse(inputStream);
      return this.state.getConfig().getClient(); } catch (Exception e) {
    	  throw new RuntimeException("Error occurred.  Cause: " + e, e);
    }
   
  }

  private void addSqlMapConfigNodelets()
  {
    this.parser.addNodelet("/sqlMapConfig/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        SolarSqlMapConfigParser.this.state.getConfig().finalizeSqlMapConfig();
      } } );
  }

  private void addGlobalPropNodelets() {
    this.parser.addNodelet("/sqlMapConfig/properties", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, SolarSqlMapConfigParser.this.state.getGlobalProps());
        String resource = attributes.getProperty("resource");
        String url = attributes.getProperty("url");
        SolarSqlMapConfigParser.this.state.setGlobalProperties(resource, url);
      } } );
  }

  private void addSettingsNodelets() {
    this.parser.addNodelet("/sqlMapConfig/settings", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, SolarSqlMapConfigParser.this.state.getGlobalProps());
        SqlMapConfiguration config = SolarSqlMapConfigParser.this.state.getConfig();

        String classInfoCacheEnabledAttr = attributes.getProperty("classInfoCacheEnabled");
        boolean classInfoCacheEnabled = (classInfoCacheEnabledAttr == null) || ("true".equals(classInfoCacheEnabledAttr));
        config.setClassInfoCacheEnabled(classInfoCacheEnabled);

        String lazyLoadingEnabledAttr = attributes.getProperty("lazyLoadingEnabled");
        boolean lazyLoadingEnabled = (lazyLoadingEnabledAttr == null) || ("true".equals(lazyLoadingEnabledAttr));
        config.setLazyLoadingEnabled(lazyLoadingEnabled);

        String statementCachingEnabledAttr = attributes.getProperty("statementCachingEnabled");
        boolean statementCachingEnabled = (statementCachingEnabledAttr == null) || ("true".equals(statementCachingEnabledAttr));
        config.setStatementCachingEnabled(statementCachingEnabled);

        String cacheModelsEnabledAttr = attributes.getProperty("cacheModelsEnabled");
        boolean cacheModelsEnabled = (cacheModelsEnabledAttr == null) || ("true".equals(cacheModelsEnabledAttr));
        config.setCacheModelsEnabled(cacheModelsEnabled);

        String enhancementEnabledAttr = attributes.getProperty("enhancementEnabled");
        boolean enhancementEnabled = (enhancementEnabledAttr == null) || ("true".equals(enhancementEnabledAttr));
        config.setEnhancementEnabled(enhancementEnabled);

        String useColumnLabelAttr = attributes.getProperty("useColumnLabel");
        boolean useColumnLabel = (useColumnLabelAttr == null) || ("true".equals(useColumnLabelAttr));
        config.setUseColumnLabel(useColumnLabel);

        String forceMultipleResultSetSupportAttr = attributes.getProperty("forceMultipleResultSetSupport");
        boolean forceMultipleResultSetSupport = "true".equals(forceMultipleResultSetSupportAttr);
        config.setForceMultipleResultSetSupport(forceMultipleResultSetSupport);

        String defaultTimeoutAttr = attributes.getProperty("defaultStatementTimeout");
        Integer defaultTimeout = defaultTimeoutAttr == null ? null : Integer.valueOf(defaultTimeoutAttr);
        config.setDefaultStatementTimeout(defaultTimeout);

        String useStatementNamespacesAttr = attributes.getProperty("useStatementNamespaces");
        boolean useStatementNamespaces = "true".equals(useStatementNamespacesAttr);
        SolarSqlMapConfigParser.this.state.setUseStatementNamespaces(useStatementNamespaces);
      } } );
  }

  private void addTypeAliasNodelets() {
    this.parser.addNodelet("/sqlMapConfig/typeAlias", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties prop = NodeletUtils.parseAttributes(node, SolarSqlMapConfigParser.this.state.getGlobalProps());
        String alias = prop.getProperty("alias");
        String type = prop.getProperty("type");
        SolarSqlMapConfigParser.this.state.getConfig().getTypeHandlerFactory().putTypeAlias(alias, type);
      } } );
  }

  private void addTypeHandlerNodelets() {
    this.parser.addNodelet("/sqlMapConfig/typeHandler", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties prop = NodeletUtils.parseAttributes(node, SolarSqlMapConfigParser.this.state.getGlobalProps());
        String jdbcType = prop.getProperty("jdbcType");
        String javaType = prop.getProperty("javaType");
        String callback = prop.getProperty("callback");

        javaType = SolarSqlMapConfigParser.this.state.getConfig().getTypeHandlerFactory().resolveAlias(javaType);
        callback = SolarSqlMapConfigParser.this.state.getConfig().getTypeHandlerFactory().resolveAlias(callback);

        SolarSqlMapConfigParser.this.state.getConfig().newTypeHandler(Resources.classForName(javaType), jdbcType, Resources.instantiate(callback));
      } } );
  }

  private void addTransactionManagerNodelets() {
    this.parser.addNodelet("/sqlMapConfig/transactionManager/property", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, SolarSqlMapConfigParser.this.state.getGlobalProps());
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), SolarSqlMapConfigParser.this.state.getGlobalProps());
        SolarSqlMapConfigParser.this.state.getTxProps().setProperty(name, value);
      }
    });
    this.parser.addNodelet("/sqlMapConfig/transactionManager/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, SolarSqlMapConfigParser.this.state.getGlobalProps());
        String type = attributes.getProperty("type");
        boolean commitRequired = "true".equals(attributes.getProperty("commitRequired"));

        SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setActivity("configuring the transaction manager");
        type = SolarSqlMapConfigParser.this.state.getConfig().getTypeHandlerFactory().resolveAlias(type);
        TransactionManager txManager;
        try
        {
          SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setMoreInfo("Check the transaction manager type or class.");
          TransactionConfig config = (TransactionConfig)Resources.instantiate(type);
          config.setDataSource(SolarSqlMapConfigParser.this.state.getDataSource());
          SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setMoreInfo("Check the transactio nmanager properties or configuration.");
          config.setProperties(SolarSqlMapConfigParser.this.state.getTxProps());
          config.setForceCommit(commitRequired);
          config.setDataSource(SolarSqlMapConfigParser.this.state.getDataSource());
          SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setMoreInfo(null);
          txManager = new TransactionManager(config);
        }
        catch (Exception e)
        {
          if ((e instanceof SqlMapException)) {
            throw ((SqlMapException)e);
          }
          throw new SqlMapException("Error initializing TransactionManager.  Could not instantiate TransactionConfig.  Cause: " + e, e);
        }
        
        SolarSqlMapConfigParser.this.state.getConfig().setTransactionManager(txManager);
      }
    });
    this.parser.addNodelet("/sqlMapConfig/transactionManager/dataSource/property", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, SolarSqlMapConfigParser.this.state.getGlobalProps());
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), SolarSqlMapConfigParser.this.state.getGlobalProps());
        SolarSqlMapConfigParser.this.state.getDsProps().setProperty(name, value);
      }
    });
    this.parser.addNodelet("/sqlMapConfig/transactionManager/dataSource/end()", new Nodelet() {
      public void process(Node node) throws Exception {
        SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setActivity("configuring the data source");

        Properties attributes = NodeletUtils.parseAttributes(node, SolarSqlMapConfigParser.this.state.getGlobalProps());

        String type = attributes.getProperty("type");
        Properties props = SolarSqlMapConfigParser.this.state.getDsProps();

        type = SolarSqlMapConfigParser.this.state.getConfig().getTypeHandlerFactory().resolveAlias(type);
        try {
          SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setMoreInfo("Check the data source type or class.");
          DataSourceFactory dsFactory = (DataSourceFactory)Resources.instantiate(type);
          SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setMoreInfo("Check the data source properties or configuration.");
          dsFactory.initialize(props);
          SolarSqlMapConfigParser.this.state.setDataSource(dsFactory.getDataSource());
          SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setMoreInfo(null);
        } catch (Exception e) {
          if ((e instanceof SqlMapException)) {
            throw ((SqlMapException)e);
          }
          throw new SqlMapException("Error initializing DataSource.  Could not instantiate DataSourceFactory.  Cause: " + e, e);
        }
      }
    });
  }

  protected void addSqlMapNodelets()
  {
    this.parser.addNodelet("/sqlMapConfig/sqlMap", new Nodelet() {
      public void process(Node node) throws Exception {
        SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setActivity("loading the SQL Map resource");

        Properties attributes = NodeletUtils.parseAttributes(node, SolarSqlMapConfigParser.this.state.getGlobalProps());

        String resource = attributes.getProperty("resource");
        String url = attributes.getProperty("url");

        if (SolarSqlMapConfigParser.this.usingStreams) {
          InputStream inputStream = null;
          if (resource != null) {
            SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setResource(resource);
            PathMatchingResourcePatternResolver prr = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
            Resource[] resources = prr.getResources(resource);
            for (int i = 0; i < resources.length; i++)
              new SqlMapParser(SolarSqlMapConfigParser.this.state).parse(resources[i].getInputStream());
          }
          else if (url != null) {
            SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setResource(url);
            inputStream = Resources.getUrlAsStream(url);
            new SqlMapParser(SolarSqlMapConfigParser.this.state).parse(inputStream);
          } else {
            throw new SqlMapException("The <sqlMap> element requires either a resource or a url attribute.");
          }
        } else {
          Reader reader = null;
          if (resource != null) {
            SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setResource(resource);
            reader = Resources.getResourceAsReader(resource);
          } else if (url != null) {
            SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setResource(url);
            reader = Resources.getUrlAsReader(url);
          } else {
            throw new SqlMapException("The <sqlMap> element requires either a resource or a url attribute.");
          }

          new SqlMapParser(SolarSqlMapConfigParser.this.state).parse(reader);
        }
      } } );
  }

  private void addResultObjectFactoryNodelets() {
    this.parser.addNodelet("/sqlMapConfig/resultObjectFactory", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, SolarSqlMapConfigParser.this.state.getGlobalProps());
        String type = attributes.getProperty("type");

        SolarSqlMapConfigParser.this.state.getConfig().getErrorContext().setActivity("configuring the Result Object Factory");
        try
        {
          ResultObjectFactory rof = (ResultObjectFactory)Resources.instantiate(type);
          SolarSqlMapConfigParser.this.state.getConfig().setResultObjectFactory(rof);
        } catch (Exception e) {
          throw new SqlMapException("Error instantiating resultObjectFactory: " + type, e);
        }
      }
    });
    this.parser.addNodelet("/sqlMapConfig/resultObjectFactory/property", new Nodelet() {
      public void process(Node node) throws Exception {
        Properties attributes = NodeletUtils.parseAttributes(node, SolarSqlMapConfigParser.this.state.getGlobalProps());
        String name = attributes.getProperty("name");
        String value = NodeletUtils.parsePropertyTokens(attributes.getProperty("value"), SolarSqlMapConfigParser.this.state.getGlobalProps());
        SolarSqlMapConfigParser.this.state.getConfig().getDelegate().getResultObjectFactory().setProperty(name, value);
      }
    });
  }
}