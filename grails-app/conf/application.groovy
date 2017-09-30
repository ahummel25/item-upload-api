import com.wwt.customapps.CloudUtilities
import org.grails.orm.hibernate.cfg.GrailsAnnotationConfiguration



dataSource {
  
  url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=devr12-scan.wwt.com)(PORT=1525))(CONNECT_DATA=(SERVICE_NAME=DEVERP_GENERAL.WWT.COM)))"
  username = "wwt_cf_item_upload_api"
  password = "wwt_inn0vate"
  logSql = true
  driverClassName = 'oracle.jdbc.OracleDriver'
  configClass = GrailsAnnotationConfiguration.class
}


environments {
  development {
    appDefault.loginName = System.properties['user.name']
    wwt.security.permissions.optional = true
  }

  production {
    if (CloudUtilities.isCloudDeployed()) {
      
      
      def oracleSource = CloudUtilities.getServiceCredentials('oracle-erp-sb')
        dataSource {
          url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${oracleSource.host})(PORT=${oracleSource.port}))(CONNECT_DATA=(SERVICE_NAME=${oracleSource.service})))"
          username = oracleSource.username
          password = oracleSource.password
          driverClassName = 'oracle.jdbc.OracleDriver'
          dialect = 'org.hibernate.dialect.Oracle10gDialect'
          logSql = false
          pooled = true
          properties {
            maxActive   = oracleSource.maxActive
            maxIdle     = oracleSource.maxIdle
            minIdle     = oracleSource.minIdle
            initialSize = oracleSource.initialSize

            maxWait = 60 * 1000;                                 //... Timeout for borrowConnection()

            minEvictableIdleTimeMillis    = 30 * 60 * 1000       //... Evict Connections idle for 30 mins
            timeBetweenEvictionRunsMillis = 15 * 60 * 1000       //... Run Evictor every 15 mins
            numTestsPerEvictionRun        = 3                    //... Test 3 connections each time the evictor runs

            testOnBorrow  = true
            testOnReturn  = false
            testWhileIdle = true

            validationQuery = oracleSource.validationQuery
          }
        }
    }
  }
}
