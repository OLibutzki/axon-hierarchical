package de.libutzki.axon.axonhierarchical.shared;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.messaging.interceptors.CorrelationDataInterceptor;
import org.axonframework.spring.config.AnnotationDriven;
import org.axonframework.spring.config.AxonConfiguration;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Configuration
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

	@Bean
	@ConditionalOnBean( DelegatingCommandBus.class )
	@Primary
	public DelegationCandidateCommandBus delegationCandidateCommandBus( final DelegatingCommandBus delegatingCommandBus, @Qualifier( "localSegment" ) final CommandBus commandBus ) {
		final DelegationCandidateCommandBusWrapper delegationCandidateCommandBusWrapper = new DelegationCandidateCommandBusWrapper( commandBus );
		delegatingCommandBus.registerCommandBus( delegationCandidateCommandBusWrapper );
		return delegationCandidateCommandBusWrapper;

	}

	@ConditionalOnBean( DelegatingCommandBus.class )
	@ConditionalOnMissingBean
	@Qualifier( "localSegment" )
	@Bean
	public SimpleCommandBus commandBus( final TransactionManager txManager, final AxonConfiguration axonConfiguration ) {
		final SimpleCommandBus commandBus = SimpleCommandBus.builder( )
				.transactionManager( txManager )
				.messageMonitor( axonConfiguration.messageMonitor( CommandBus.class, "commandBus" ) )
				.build( );
		commandBus.registerHandlerInterceptor(
				new CorrelationDataInterceptor<>( axonConfiguration.correlationDataProviders( ) ) );
		return commandBus;
	}
}