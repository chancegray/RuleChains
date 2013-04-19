dataSource {
    pooled = true
    driverClassName = "com.mysql.jdbc.Driver"
    dialect = org.hibernate.dialect.MySQL5InnoDBDialect
    username = "myuser"
    password = "mypass"
    properties {
        maxActive = -1
        minEvictableIdleTimeMillis=1800000
        timeBetweenEvictionRunsMillis=1800000
        numTestsPerEvictionRun=3
        testOnBorrow=true
        testWhileIdle=true
        testOnReturn=true
        validationQuery="SELECT 1"
    }    
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:mysql://localhost:3306/RuleChains"
            loggingSql = true            
        }
        dataSource_oasis {
            dialect = org.hibernate.dialect.Oracle10gDialect
            driverClassName = 'oracle.jdbc.driver.OracleDriver'
            username = 'myuser'
            password = 'mypass'
            url = 'jdbc:oracle:thin:@oracle.example.com:1526:DVLP'
        }                
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "jdbc:mysql://localhost:3306/RuleChains?useSSL=true&requireSSL=true&verifyServerCertificate=false"
        }
    }
    production {
        dataSource {
            dbCreate = "update"
            url = "jdbc:mysql://localhost:3306/MessageService?useSSL=true&requireSSL=true&verifyServerCertificate=false"
        }
        dataSource_oasis {
            dialect = org.hibernate.dialect.Oracle10gDialect
            driverClassName = 'oracle.jdbc.driver.OracleDriver'
            username = 'myuser'
            password = 'mypass'
            url = 'jdbc:oracle:thin:@oracle.example.com:1526:PROD'
        }                
    }
}
