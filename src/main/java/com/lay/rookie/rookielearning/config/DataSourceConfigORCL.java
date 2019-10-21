package com.lay.rookie.rookielearning.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@MapperScan(basePackages = "com.keda.mapper", sqlSessionFactoryRef = "orclSqlSessionFactory", sqlSessionTemplateRef = "orclSqlSessionTemplate")
public class DataSourceConfigORCL {

//    # ==============more datasource config===================================
//    spring.datasource.orcl.jdbcUrl=jdbc:oracle:thin:@172.16.234.168:1521:ORCL
//    spring.datasource.orcl.username=ezview
//    spring.datasource.orcl.password=ezview
//    spring.datasource.orcl.driverClassName = oracle.jdbc.OracleDriver
//    #-------------------------------------------------------------------------
//        spring.datasource.gxdw2.jdbcUrl=jdbc:oracle:thin:@10.148.76.122:1521:GXDW2
//        spring.datasource.gxdw2.username=VISITOR
//        spring.datasource.gxdw2.password=nbkgxapk110
//        spring.datasource.gxdw2.driverClassName = oracle.jdbc.OracleDriver
//    #==========================================================================

    // 将这个对象放入Spring容器中
    @Bean(name = "orclDataSource")
    // 表示这个数据源是默认数据源
//    @Primary
    // 读取application.properties中的配置参数映射成为一个对象
    // prefix表示参数的前缀
    @ConfigurationProperties(prefix = "spring.datasource.orcl")
    public DataSource getOrclDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "orclSqlSessionFactory")
    // 表示这个数据源是默认数据源
//    @Primary
    // @Qualifier表示查找Spring容器中名字为orclDataSource的对象
    public SqlSessionFactory orclSqlSessionFactory(@Qualifier("orclDataSource") DataSource datasource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(datasource);
        //注解和xml不可同时使用
//        bean.setMapperLocations(
//                // 设置mybatis的xml所在位置
//                new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        return bean.getObject();
    }

    @Bean("orclSqlSessionTemplate")
    // 表示这个数据源是默认数据源
//    @Primary
    public SqlSessionTemplate orclSqlSessionTemplate(
            @Qualifier("orclSqlSessionFactory") SqlSessionFactory sessionFactory) {
        return new SqlSessionTemplate(sessionFactory);
    }

}



