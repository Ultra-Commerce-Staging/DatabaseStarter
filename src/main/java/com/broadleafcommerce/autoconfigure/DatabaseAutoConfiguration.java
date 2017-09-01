/*
 * #%L
 * BroadleafCommerce Database Starter
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package com.broadleafcommerce.autoconfigure;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * @author Jeff Fischer
 */
@Configuration
@EnableConfigurationProperties(DBProperties.class)
public class DatabaseAutoConfiguration {

    private static final Log LOG = LogFactory.getLog(DatabaseAutoConfiguration.class);

    @Autowired
    DBProperties props;

    @ConditionalOnMissingBean(name={"webDS"})
    @Bean
    @Primary
    public DataSource webDS() throws ClassNotFoundException {
        return buildDataSource();
    }

    @ConditionalOnMissingBean(name={"webSecureDS"})
    @Bean
    public DataSource webSecureDS() throws ClassNotFoundException {
        return buildDataSource();
    }

    @ConditionalOnMissingBean(name={"webStorageDS"})
    @Bean
    public DataSource webStorageDS() throws ClassNotFoundException {
        return buildDataSource();
    }

    @ConditionalOnMissingBean(name={"webEventDS"})
    @Bean
    public DataSource webEventDS() throws ClassNotFoundException {
        return buildDataSource();
    }

    @ConditionalOnMissingBean(name={"demoDS"})
    @ConditionalOnClass(name= "com.blcdemo.core.domain.PDSite")
    @Bean
    public DataSource demoDS() throws ClassNotFoundException {
        return buildDataSource();
    }

    protected DataSource buildDataSource() throws ClassNotFoundException {
        DatabaseDriver driver = DatabaseDriver.fromJdbcUrl(props.getUrl());
        org.apache.tomcat.jdbc.pool.DataSource ds = (org.apache.tomcat.jdbc.pool.DataSource) DataSourceBuilder
                .create()
                .username(props.getUser())
                .password(props.getPassword())
                .url(props.getUrl())
                .driverClassName(driver.getDriverClassName())
                .type(org.apache.tomcat.jdbc.pool.DataSource.class)
                .build();
        
        String validationQuery = driver.getValidationQuery();
        if (validationQuery != null) {
            ds.setTestOnBorrow(true);
            ds.setValidationQuery(validationQuery);
        }
        
        return ds;
    }

}
