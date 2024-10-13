package com.kloudnuk.webserver.configurations;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Import({ServiceConfig.class})
@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {"com.kloudnuk.webserver"})
public class TransactionConfig {

    final Logger log = LoggerFactory.getLogger(TransactionConfig.class);

    @Autowired
    DataSource dataSource;

    @Bean
    public PlatformTransactionManager transactionMgr() throws SQLException {
        JdbcTransactionManager transactionMgr = new JdbcTransactionManager();
        transactionMgr.setDataSource(dataSource);
        transactionMgr.setDefaultTimeout(300);
        return transactionMgr;
    }
}
