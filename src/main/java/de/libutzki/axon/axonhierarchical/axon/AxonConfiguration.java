package de.libutzki.axon.axonhierarchical.axon;

import java.util.List;

import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.gateway.DefaultEventGateway;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
import org.axonframework.springboot.autoconfig.AxonServerAutoConfiguration;
import org.axonframework.springboot.autoconfig.EventProcessingAutoConfiguration;
import org.axonframework.springboot.autoconfig.InfraConfiguration;
import org.axonframework.springboot.autoconfig.JdbcAutoConfiguration;
import org.axonframework.springboot.autoconfig.JpaEventStoreAutoConfiguration;
import org.axonframework.springboot.autoconfig.MetricsAutoConfiguration;
import org.axonframework.springboot.autoconfig.MicrometerMetricsAutoConfiguration;
import org.axonframework.springboot.autoconfig.NoOpTransactionAutoConfiguration;
import org.axonframework.springboot.autoconfig.ObjectMapperAutoConfiguration;
import org.axonframework.springboot.autoconfig.TransactionAutoConfiguration;
import org.axonframework.springboot.util.jpa.ContainerManagedEntityManagerProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import de.libutzki.axon.axonhierarchical.shared.ExportedBeanPostProcessor;

@Configuration
@Import( {
		DataSourceAutoConfiguration.class,
		HibernateJpaAutoConfiguration.class,
		MetricsAutoConfiguration.class,
		MicrometerMetricsAutoConfiguration.class,
		EventProcessingAutoConfiguration.class,
		// JpaAutoConfiguration.class,
		JpaEventStoreAutoConfiguration.class,
		JdbcAutoConfiguration.class,
		TransactionAutoConfiguration.class,
		NoOpTransactionAutoConfiguration.class,
		AxonAutoConfiguration.class,
		InfraConfiguration.class,
		ObjectMapperAutoConfiguration.class,
		AxonServerAutoConfiguration.class,
		PropertyPlaceholderAutoConfiguration.class,
} )
@ComponentScan
@PropertySource( "classpath:de/libutzki/axon/axonhierarchical/axon/application.properties" )
public class AxonConfiguration {

	@Bean
	public EventGateway eventGateway( final EventBus eventBus ) {
		return DefaultEventGateway.builder( ).eventBus( eventBus ).build( );
	}

	@Bean
	public ExportedBeanPostProcessor exportedBeanPostProcessorExportedBeanPostProcessor( final ConfigurableListableBeanFactory beanFactory, @Value( "${modulename}" ) final String moduleName, @Value( "${exportedbeannames:}#{T(java.util.Collections).emptyList()}" ) final List<String> exportedBeanNames ) {
		return new ExportedBeanPostProcessor( beanFactory, moduleName, exportedBeanNames );
	}

	@ConditionalOnMissingBean
	@Bean
	public EntityManagerProvider entityManagerProvider( ) {
		return new ContainerManagedEntityManagerProvider( );
	}
}
