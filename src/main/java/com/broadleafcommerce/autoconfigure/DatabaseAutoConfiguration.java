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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * @author Jeff Fischer
 */
@Configuration
@EnableConfigurationProperties(DBProperties.class)
public class DatabaseAutoConfiguration {

    private static final Log LOG = LogFactory.getLog(DatabaseAutoConfiguration.class);

    private final String OLD_MYSQL = "com.mysql.jdbc.Driver";
    private final String NEW_MYSQL = "com.mysql.cj.jdbc.Driver";

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
        String driverClassName = props.getDriver();
        DatabaseDriver driver = DatabaseDriver.fromJdbcUrl(props.getUrl());
        String suggestedDriverClassName = driver.getDriverClassName();
        logDriverInconsistencies(driverClassName, suggestedDriverClassName);
        if (StringUtils.isEmpty(driverClassName)) {
            driverClassName = suggestedDriverClassName;
        }
        org.apache.tomcat.jdbc.pool.DataSource ds = DataSourceBuilder
                .create()
                .username(props.getUser())
                .password(props.getPassword())
                .url(props.getUrl())
                .driverClassName(driverClassName)
                .type(org.apache.tomcat.jdbc.pool.DataSource.class)
                .build();

        String validationQuery = driver.getValidationQuery();
        if (validationQuery != null) {
            ds.setTestOnBorrow(true);
            ds.setValidationQuery(validationQuery);
        }

        return ds;
    }

    protected void logDriverInconsistencies(String explicitDriverClassName, String springDriverClassName) {
        if (springDriverClassName == null && explicitDriverClassName == null) {
            LOG.error("Application did not set property database.driver and Spring wasn't able to derive driver class from url. Set the database.driver property and/or review property database.url to make sure a valid url is set.");
        }

        if (NEW_MYSQL.equals(explicitDriverClassName)) {
            LOG.warn("Application explicitly set the driver class to " + NEW_MYSQL + ". Broadleaf recommends using driver class " + OLD_MYSQL + " as it performs better. Set \"database.driver=" + OLD_MYSQL + "\" to use the more performant driver.");
        }

        if (explicitDriverClassName == null && NEW_MYSQL.equals(springDriverClassName)) {
            LOG.warn("Application did not set property database.driver when using MySQL therefore driver class " + NEW_MYSQL + " will be used per Spring's recommendation. Broadleaf suggests using driver class " + OLD_MYSQL + " as it performs better than " + NEW_MYSQL + ".");
        }

        if (explicitDriverClassName != null && !explicitDriverClassName.equals(springDriverClassName) && !OLD_MYSQL.equals(explicitDriverClassName)) {
            LOG.warn("Application explicitly set the driver class to " + explicitDriverClassName + " which does not equal Spring's recommended driver class of " + springDriverClassName + ". If this is not on purpose please set the property database.driver to \"database.driver=\" to use Spring's recommended driver.");
        }
    }

}
