dataSource {
    pooled = true
    driverClassName = "com.mysql.jdbc.Driver"
    dialect = org.hibernate.dialect.MySQL5InnoDBDialect
    // username = "user"
    username = "user"
    password = "password"
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
            // url = "jdbc:mysql://131.247.100.35:3306/RuleChains?useSSL=true&requireSSL=true&verifyServerCertificate=false"
            url = "jdbc:mysql://localhost:3306/RuleChains?useUnicode=yes&characterEncoding=UTF-8&autoReconnect=true"
            loggingSql = true            
        }
//        dataSource_oasis {
//            dialect = org.hibernate.dialect.Oracle10gDialect
//            driverClassName = 'oracle.jdbc.driver.OracleDriver'
//            username = 'user'
//            password = 'password'
//            url = 'jdbc:oracle:thin:@cims-ora-dvlp.it.usf.edu:1526:VIPDVLP'
//        }        
//        dataSource_oasis {
//            dialect = org.hibernate.dialect.Oracle10gDialect
//            driverClassName = 'oracle.jdbc.driver.OracleDriver'
//            username = 'user'
//            password = 'password'
//            url = 'jdbc:oracle:thin:@trusty.cfr.usf.edu:1526:DVLP'
//        }        
//        dataSource_alumni {
//            dialect = org.hibernate.dialect.Oracle10gDialect
//            driverClassName = 'oracle.jdbc.driver.OracleDriver'
//            username = 'user'
//            password = 'password'
//            url = 'jdbc:oracle:thin:@131.247.89.35:1526:ALUM'
//        }        
        dataSource_datawarehouse {
            dialect = org.hibernate.dialect.Oracle10gDialect
            driverClassName = 'oracle.jdbc.driver.OracleDriver'
            username = 'user'
            password = 'password'
            url = 'jdbc:oracle:thin:@truly.cfr.usf.edu:1526:DWUPG'
        }        
//        dataSource_idcards {
//            dialect = org.hibernate.dialect.Oracle10gDialect
//            driverClassName = 'oracle.jdbc.driver.OracleDriver'
//            username = 'user'
//            password = 'password'
//            url = 'jdbc:oracle:thin:@diamond.fastmail.usf.edu:1526:IDCD'
//        }        
        dataSource_nams {
            dialect = org.hibernate.dialect.MySQLDialect
            driverClassName = 'com.mysql.jdbc.Driver'
            username = 'user'
            password = 'password'
            url = 'jdbc:mysql://dev.it.usf.edu:3306/nams?useUnicode=yes&characterEncoding=UTF-8&autoReconnect=true'
        }      
//        dataSource_medBanner {
//            dialect = org.hibernate.dialect.Oracle10gDialect
//            driverClassName = 'oracle.jdbc.driver.OracleDriver'
//            username = 'user'
//            password = 'password'
//            url = 'jdbc:oracle:thin:@131.247.66.17:1521:DWPROD'
//        }
//        dataSource_medAltBanner {
//            dialect = org.hibernate.dialect.Oracle10gDialect
//            driverClassName = 'oracle.jdbc.driver.OracleDriver'
//            username = 'user'
//            password = 'password'
//            url = 'jdbc:oracle:thin:@hscdb3.hsc.usf.edu:1527:DBAFRZN'
//        }
        dataSource_namsDev {
            dialect = org.hibernate.dialect.MySQLDialect
            driverClassName = 'com.mysql.jdbc.Driver'
            username = 'user'
            password = 'password'
            url = 'jdbc:mysql://dev.it.usf.edu:3306/nams?useUnicode=yes&characterEncoding=UTF-8&autoReconnect=true'
        }      
        dataSource_baggageclaim {
            dialect = org.hibernate.dialect.MySQLInnoDBDialect
            driverClassName = 'com.mysql.jdbc.Driver'
            username = 'user'
            password = 'password'
            url = 'jdbc:mysql://localhost:3306/baggage_claim?useUnicode=yes&characterEncoding=UTF-8&autoReconnect=true'
        }      
        dataSource_gems {
            dialect = org.hibernate.dialect.Oracle10gDialect
            driverClassName = 'oracle.jdbc.driver.OracleDriver'
            username = 'user'
            password = 'password'
            url = 'jdbc:oracle:thin:@trusty.cfr.usf.edu:1526:GEMSDVLP'
        }        
        dataSource_peopleadmin {
            dialect = org.hibernate.dialect.MySQLDialect
            driverClassName = 'com.mysql.jdbc.Driver'
            username = 'user'
            password = 'password'
            url = 'jdbc:mysql://dev.it.usf.edu:3306/nams?useUnicode=yes&characterEncoding=UTF-8&autoReconnect=true'
        }      
        dataSource_staging {
            dialect = org.hibernate.dialect.MySQLInnoDBDialect
            driverClassName = 'com.mysql.jdbc.Driver'
            username = 'user'
            password = 'password'
            url = 'jdbc:mysql://localhost:3306/staging?useUnicode=yes&characterEncoding=UTF-8&autoReconnect=true'
        }      
        
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "jdbc:mysql://131.247.100.35:3306/RuleChains?useUnicode=yes&characterEncoding=UTF-8&useSSL=true&requireSSL=true&verifyServerCertificate=false&autoReconnect=true"
        }
    }
    production {
        dataSource {
            dbCreate = "update"
            url = "jdbc:mysql://131.247.100.35:3306/MessageService?useUnicode=yes&characterEncoding=UTF-8&useSSL=true&requireSSL=true&verifyServerCertificate=false&autoReconnect=true"
        }
    }
}
