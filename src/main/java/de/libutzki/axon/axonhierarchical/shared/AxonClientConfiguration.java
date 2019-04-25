package de.libutzki.axon.axonhierarchical.shared;

import org.axonframework.spring.config.AnnotationDriven;
import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
import org.axonframework.springboot.autoconfig.AxonServerAutoConfiguration;
import org.axonframework.springboot.autoconfig.EventProcessingAutoConfiguration;
import org.axonframework.springboot.autoconfig.JdbcAutoConfiguration;
import org.axonframework.springboot.autoconfig.JpaAutoConfiguration;
import org.axonframework.springboot.autoconfig.JpaEventStoreAutoConfiguration;
import org.axonframework.springboot.autoconfig.MetricsAutoConfiguration;
import org.axonframework.springboot.autoconfig.MicrometerMetricsAutoConfiguration;
import org.axonframework.springboot.autoconfig.NoOpTransactionAutoConfiguration;
import org.axonframework.springboot.autoconfig.ObjectMapperAutoConfiguration;
import org.axonframework.springboot.autoconfig.TransactionAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
@AnnotationDriven
@EntityScan( { "org.axonframework.eventhandling.tokenstore", "org.axonframework.modelling.saga.repository.jpa" } )
@Import( {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		MetricsAutoConfiguration.class,
		MicrometerMetricsAutoConfiguration.class,
		EventProcessingAutoConfiguration.class,
		JpaAutoConfiguration.class,
		JpaEventStoreAutoConfiguration.class,
		JdbcAutoConfiguration.class,
		TransactionAutoConfiguration.class,
		NoOpTransactionAutoConfiguration.class,
		AxonAutoConfiguration.class,
		ObjectMapperAutoConfiguration.class,
		AxonServerAutoConfiguration.class,
		HierarchyAwareSpringAxonAutoConfigurer.ImportSelector.class,
		PropertyPlaceholderAutoConfiguration.class
} )
public class AxonClientConfiguration {

}