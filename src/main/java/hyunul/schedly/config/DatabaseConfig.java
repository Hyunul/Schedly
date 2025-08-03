// package hyunul.schedly.config;

// import java.util.Properties;

// import javax.sql.DataSource;

// import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.orm.jpa.JpaTransactionManager;
// import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
// import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
// import org.springframework.transaction.PlatformTransactionManager;
// import org.springframework.transaction.annotation.EnableTransactionManagement;

// import com.zaxxer.hikari.HikariConfig;
// import com.zaxxer.hikari.HikariDataSource;

// @Configuration
// @EnableTransactionManagement
// public class DatabaseConfig {
    
//     @Bean
//     @ConfigurationProperties("spring.datasource.hikari")
//     public HikariConfig hikariConfig() {
//         return new HikariConfig();
//     }
    
//     @Bean
//     public DataSource dataSource() {
//         return new HikariDataSource(hikariConfig());
//     }
    
//     @Bean
//     public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
//         LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
//         em.setDataSource(dataSource());
//         em.setPackagesToScan("com.example.groupscheduler.entity");
        
//         HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
//         vendorAdapter.setGenerateDdl(true);
//         vendorAdapter.setShowSql(false);
//         em.setJpaVendorAdapter(vendorAdapter);
        
//         Properties properties = new Properties();
//         properties.setProperty("hibernate.hbm2ddl.auto", "update");
//         properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
//         properties.setProperty("hibernate.show_sql", "false");
//         properties.setProperty("hibernate.format_sql", "true");
//         properties.setProperty("hibernate.use_sql_comments", "true");
//         properties.setProperty("hibernate.jdbc.batch_size", "20");
//         properties.setProperty("hibernate.order_inserts", "true");
//         properties.setProperty("hibernate.order_updates", "true");
//         em.setJpaProperties(properties);
        
//         return em;
//     }
    
//     @Bean
//     public PlatformTransactionManager transactionManager() {
//         JpaTransactionManager transactionManager = new JpaTransactionManager();
//         transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
//         return transactionManager;
//     }
// }
