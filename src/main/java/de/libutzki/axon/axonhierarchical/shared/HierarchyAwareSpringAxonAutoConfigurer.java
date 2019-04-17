/*
 * Copyright (c) 2010-2018. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.libutzki.axon.axonhierarchical.shared;

import static org.axonframework.common.ReflectionUtils.methodsOf;
import static org.axonframework.common.annotation.AnnotationUtils.findAnnotationAttributes;
import static org.axonframework.spring.SpringUtils.isQualifierMatch;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.common.AxonConfigurationException;
import org.axonframework.common.annotation.AnnotationUtils;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.lock.LockFactory;
import org.axonframework.common.lock.NullLockFactory;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.AggregateConfigurer;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.ConfigurerModule;
import org.axonframework.config.DefaultConfigurer;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.config.EventProcessingModule;
import org.axonframework.config.ModuleConfiguration;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.eventhandling.ErrorHandler;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.ListenerInvocationErrorHandler;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.messaging.annotation.HandlerDefinition;
import org.axonframework.messaging.annotation.MessageHandler;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.modelling.command.CommandTargetResolver;
import org.axonframework.modelling.command.GenericJpaRepository;
import org.axonframework.modelling.command.Repository;
import org.axonframework.modelling.saga.ResourceInjector;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.upcasting.event.EventUpcaster;
import org.axonframework.spring.config.AxonConfiguration;
import org.axonframework.spring.config.EventHandlerRegistrar;
import org.axonframework.spring.config.QueryHandlerSubscriber;
import org.axonframework.spring.config.RepositoryFactoryBean;
import org.axonframework.spring.config.annotation.SpringContextHandlerDefinitionBuilder;
import org.axonframework.spring.config.annotation.SpringContextParameterResolverFactoryBuilder;
import org.axonframework.spring.eventsourcing.SpringPrototypeAggregateFactory;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.axonframework.spring.saga.SpringResourceInjector;
import org.axonframework.spring.stereotype.Aggregate;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * ImportBeanDefinitionRegistrar implementation that sets up an infrastructure Configuration based on beans available in
 * the application context.
 * <p>
 * This component is backed by a DefaultConfiguration (see {@link DefaultConfigurer#defaultConfiguration()} and
 * registers the following beans if present in the ApplicationContext:
 * <ul>
 * <li>{@link CommandBus}</li>
 * <li>{@link EventStorageEngine} or {@link EventBus}</li>
 * <li>{@link Serializer}</li>
 * <li>{@link TokenStore}</li>
 * <li>{@link PlatformTransactionManager}</li>
 * <li>{@link TransactionManager}</li>
 * <li>{@link SagaStore}</li>
 * <li>{@link ResourceInjector} (which defaults to {@link SpringResourceInjector}</li>
 * </ul>
 * <p>
 * Furthermore, all beans with an {@link Aggregate @Aggregate} or {@link Saga @Saga} annotation are inspected and
 * required components to operate the Aggregate or Saga are registered.
 *
 * @author Allard Buijze
 * @since 3.0
 */
public class HierarchyAwareSpringAxonAutoConfigurer implements ImportBeanDefinitionRegistrar, BeanFactoryAware {

	/**
	 * Name of the {@link AxonConfiguration} bean.
	 */
	@SuppressWarnings( "WeakerAccess" )
	public static final String AXON_CONFIGURATION_BEAN = "org.axonframework.spring.config.AxonConfiguration";

	/**
	 * Name of the {@link Configurer} bean.
	 */
	@SuppressWarnings( "WeakerAccess" )
	public static final String AXON_CONFIGURER_BEAN = "org.axonframework.config.Configurer";

	private static final Logger logger = LoggerFactory.getLogger( HierarchyAwareSpringAxonAutoConfigurer.class );
	private ConfigurableListableBeanFactory beanFactory;

	@Override
	public void registerBeanDefinitions( final AnnotationMetadata importingClassMetadata, final BeanDefinitionRegistry registry ) {
		registry.registerBeanDefinition( "commandHandlerSubscriber",
				genericBeanDefinition( HierarchyAwareCommandHandlerSubscriber.class ).getBeanDefinition( ) );

		registry.registerBeanDefinition( "queryHandlerSubscriber",
				genericBeanDefinition( QueryHandlerSubscriber.class ).getBeanDefinition( ) );

		final Configurer configurer = DefaultConfigurer.defaultConfiguration( false );

		final RuntimeBeanReference parameterResolver = SpringContextParameterResolverFactoryBuilder.getBeanReference( registry );
		configurer.registerComponent( ParameterResolverFactory.class, c -> beanFactory
				.getBean( parameterResolver.getBeanName( ), ParameterResolverFactory.class ) );

		final RuntimeBeanReference handlerDefinition = SpringContextHandlerDefinitionBuilder.getBeanReference( registry );
		configurer.registerHandlerDefinition( ( c, clazz ) -> beanFactory
				.getBean( handlerDefinition.getBeanName( ), HandlerDefinition.class ) );

		findComponent( CommandBus.class )
				.ifPresent( commandBus -> configurer.configureCommandBus( c -> getBean( commandBus, c ) ) );
		findComponent( QueryBus.class )
				.ifPresent( queryBus -> configurer.configureQueryBus( c -> getBean( queryBus, c ) ) );
		findComponent( QueryUpdateEmitter.class )
				.ifPresent( queryUpdateEmitter -> configurer.configureQueryUpdateEmitter( c -> getBean( queryUpdateEmitter, c ) ) );
		findComponent( EventStorageEngine.class )
				.ifPresent( ese -> configurer.configureEmbeddedEventStore( c -> getBean( ese, c ) ) );
		findComponent( EventBus.class ).ifPresent( eventBus -> configurer.configureEventBus( c -> getBean( eventBus, c ) ) );
//		findComponent( Serializer.class )
//				.ifPresent( serializer -> configurer.configureSerializer( c -> getBean( serializer, c ) ) );
		findComponent( Serializer.class, "eventSerializer" )
				.ifPresent( eventSerializer -> configurer.configureEventSerializer( c -> getBean( eventSerializer, c ) ) );
		findComponent( Serializer.class, "messageSerializer" ).ifPresent(
				messageSerializer -> configurer.configureMessageSerializer( c -> getBean( messageSerializer, c ) ) );
		findComponent( TokenStore.class )
				.ifPresent( tokenStore -> configurer.registerComponent( TokenStore.class, c -> getBean( tokenStore, c ) ) );
		try {
			findComponent( PlatformTransactionManager.class ).ifPresent(
					ptm -> configurer.configureTransactionManager( c -> new SpringTransactionManager( getBean( ptm, c ) ) ) );
		} catch ( final NoClassDefFoundError error ) {
			// that's fine...
		}
		findComponent( TransactionManager.class )
				.ifPresent( tm -> configurer.configureTransactionManager( c -> getBean( tm, c ) ) );
		findComponent( SagaStore.class )
				.ifPresent( sagaStore -> configurer.registerComponent( SagaStore.class, c -> getBean( sagaStore, c ) ) );
		findComponent( ListenerInvocationErrorHandler.class ).ifPresent(
				handler -> configurer.registerComponent( ListenerInvocationErrorHandler.class, c -> getBean( handler, c ) ) );
		findComponent( ErrorHandler.class ).ifPresent(
				handler -> configurer.registerComponent( ErrorHandler.class, c -> getBean( handler, c ) ) );

		final String resourceInjector = findComponent( ResourceInjector.class, registry,
				( ) -> genericBeanDefinition( SpringResourceInjector.class )
						.getBeanDefinition( ) );
		configurer.configureResourceInjector( c -> getBean( resourceInjector, c ) );

		findComponent( DeadlineManager.class ).ifPresent( deadlineManager -> configurer
				.registerComponent( DeadlineManager.class, c -> getBean( deadlineManager, c ) ) );

		final EventProcessingModule eventProcessingModule = new EventProcessingModule( );
		final Optional<String> eventProcessingConfigurerOptional = findComponent( EventProcessingConfigurer.class );
		final String eventProcessingConfigurerBeanName = eventProcessingConfigurerOptional
				.orElse( "eventProcessingConfigurer" );
		if ( !eventProcessingConfigurerOptional.isPresent( ) ) {
			registry.registerBeanDefinition( eventProcessingConfigurerBeanName,
					genericBeanDefinition( EventProcessingConfigurer.class,
							( ) -> eventProcessingModule )
									.getBeanDefinition( ) );
		}

		registerModules( configurer );
		registerCorrelationDataProviders( configurer );
		registerEventUpcasters( configurer );
		registerAggregateBeanDefinitions( configurer, registry );

		final String eventProcessingConfigurationName = findComponent( EventProcessingConfiguration.class )
				.orElseThrow( ( ) -> new AxonConfigurationException( "Missing EventProcessingConfiguration bean" ) );

		beanFactory.registerSingleton( AXON_CONFIGURER_BEAN, configurer );
		registry.registerBeanDefinition( AXON_CONFIGURATION_BEAN, genericBeanDefinition( AxonConfiguration.class )
				.addConstructorArgReference( AXON_CONFIGURER_BEAN ).getBeanDefinition( ) );
		try {
			final EventProcessingConfigurer eventProcessingConfigurer = configurer.eventProcessing( );
			registerSagaBeanDefinitions( eventProcessingConfigurer );
			registerEventHandlerRegistrar( eventProcessingConfigurationName,
					eventProcessingConfigurerBeanName,
					registry );
		} catch ( final AxonConfigurationException ace ) {
			logger.warn(
					"There are several EventProcessingConfigurers registered, Axon will not automatically register sagas and event handlers.",
					ace );
		}
	}

	private void registerCorrelationDataProviders( final Configurer configurer ) {
		configurer.configureCorrelationDataProviders(
				c -> {
					final String[] correlationDataProviderBeans = beanFactory.getBeanNamesForType( CorrelationDataProvider.class );
					return Arrays.stream( correlationDataProviderBeans )
							.map( n -> ( CorrelationDataProvider ) getBean( n, c ) )
							.collect( Collectors.toList( ) );
				} );
	}

	private void registerEventUpcasters( final Configurer configurer ) {
		Arrays.stream( beanFactory.getBeanNamesForType( EventUpcaster.class ) )
				.forEach( name -> configurer.registerEventUpcaster( c -> getBean( name, c ) ) );
	}

	@SuppressWarnings( "unchecked" )
	private <T> T getBean( final String beanName, final Configuration configuration ) {
		return ( T ) configuration.getComponent( ApplicationContext.class ).getBean( beanName );
	}

	private void registerEventHandlerRegistrar( final String epConfigurationBeanName, final String epConfigurerBeanName,
			final BeanDefinitionRegistry registry ) {
		final List<RuntimeBeanReference> beans = new ManagedList<>( );
		beanFactory.getBeanNamesIterator( ).forEachRemaining( bean -> {
			if ( !beanFactory.isFactoryBean( bean ) ) {
				final Class<?> beanType = beanFactory.getType( bean );
				if ( beanType != null && beanFactory.containsBeanDefinition( bean ) &&
						beanFactory.getBeanDefinition( bean ).isSingleton( ) ) {
					final boolean hasHandler = StreamSupport.stream( methodsOf( beanType ).spliterator( ), false )
							.map( m -> findAnnotationAttributes( m, MessageHandler.class ).orElse( null ) )
							.filter( Objects::nonNull )
							.anyMatch( attr -> EventMessage.class
									.isAssignableFrom( ( Class ) attr.get( "messageType" ) ) );
					if ( hasHandler ) {
						beans.add( new RuntimeBeanReference( bean ) );
					}
				}
			}
		} );
		registry.registerBeanDefinition( "eventHandlerRegistrar", genericBeanDefinition( EventHandlerRegistrar.class )
				.addConstructorArgReference( AXON_CONFIGURATION_BEAN )
				.addConstructorArgReference( epConfigurationBeanName )
				.addConstructorArgReference( epConfigurerBeanName )
				.addPropertyValue( "eventHandlers", beans ).getBeanDefinition( ) );
	}

	private void registerModules( final Configurer configurer ) {
		registerConfigurerModules( configurer );
		registerModuleConfigurations( configurer );
	}

	private void registerConfigurerModules( final Configurer configurer ) {
		final String[] configurerModules = beanFactory.getBeanNamesForType( ConfigurerModule.class );
		for ( final String configurerModuleBeanName : configurerModules ) {
			final ConfigurerModule configurerModule = beanFactory.getBean( configurerModuleBeanName, ConfigurerModule.class );
			configurerModule.configureModule( configurer );
		}
	}

	private void registerModuleConfigurations( final Configurer configurer ) {
		final String[] moduleConfigurations = beanFactory.getBeanNamesForType( ModuleConfiguration.class );
		for ( final String moduleConfiguration : moduleConfigurations ) {
			configurer.registerModule( new LazyRetrievedModuleConfiguration(
					( ) -> beanFactory.getBean( moduleConfiguration, ModuleConfiguration.class ),
					beanFactory.getType( moduleConfiguration ) ) );
		}
	}

	@SuppressWarnings( "unchecked" )
	private void registerSagaBeanDefinitions( final EventProcessingConfigurer configurer ) {
		final String[] sagas = beanFactory.getBeanNamesForAnnotation( Saga.class );
		for ( final String saga : sagas ) {
			final Saga sagaAnnotation = beanFactory.findAnnotationOnBean( saga, Saga.class );
			final Class sagaType = beanFactory.getType( saga );
			final ProcessingGroup processingGroupAnnotation = beanFactory.findAnnotationOnBean( saga, ProcessingGroup.class );
			if ( processingGroupAnnotation != null && !"".equals( processingGroupAnnotation.value( ) ) ) {
				configurer.assignHandlerTypesMatching( processingGroupAnnotation.value( ), sagaType::equals );
			}
			configurer.registerSaga( sagaType, sagaConfigurer -> {
				if ( sagaAnnotation != null && !"".equals( sagaAnnotation.sagaStore( ) ) ) {
					sagaConfigurer.configureSagaStore( c -> beanFactory.getBean( sagaAnnotation.sagaStore( ), SagaStore.class ) );
				}
			} );
		}
	}

	/**
	 * @param <A>
	 *            generic specifying the Aggregate type being registered
	 */
	@SuppressWarnings( "unchecked" )
	private <A> void registerAggregateBeanDefinitions( final Configurer configurer, final BeanDefinitionRegistry registry ) {
		final String[] aggregates = beanFactory.getBeanNamesForAnnotation( Aggregate.class );
		for ( final String aggregate : aggregates ) {
			final Aggregate aggregateAnnotation = beanFactory.findAnnotationOnBean( aggregate, Aggregate.class );
			final Class<A> aggregateType = ( Class<A> ) beanFactory.getType( aggregate );
			final AggregateConfigurer<A> aggregateConf = AggregateConfigurer.defaultConfiguration( aggregateType );
			if ( "".equals( aggregateAnnotation.repository( ) ) ) {
				final String repositoryName = lcFirst( aggregateType.getSimpleName( ) ) + "Repository";
				final String factoryName = aggregate.substring( 0, 1 ).toLowerCase( ) + aggregate.substring( 1 ) + "AggregateFactory";
				if ( beanFactory.containsBean( repositoryName ) ) {
					aggregateConf.configureRepository( c -> beanFactory.getBean( repositoryName, Repository.class ) );
				} else {
					registry.registerBeanDefinition( repositoryName,
							genericBeanDefinition( RepositoryFactoryBean.class )
									.addConstructorArgValue( aggregateConf )
									.getBeanDefinition( ) );

					if ( !registry.isBeanNameInUse( factoryName ) ) {
						registry.registerBeanDefinition( factoryName,
								genericBeanDefinition( SpringPrototypeAggregateFactory.class )
										.addConstructorArgValue( aggregate )
										.getBeanDefinition( ) );
					}
					aggregateConf
							.configureAggregateFactory( c -> beanFactory.getBean( factoryName, AggregateFactory.class ) );
					final String triggerDefinition = aggregateAnnotation.snapshotTriggerDefinition( );
					if ( !"".equals( triggerDefinition ) ) {
						aggregateConf.configureSnapshotTrigger(
								c -> beanFactory.getBean( triggerDefinition, SnapshotTriggerDefinition.class ) );
					}
					if ( AnnotationUtils.isAnnotationPresent( aggregateType, "javax.persistence.Entity" ) ) {
						aggregateConf.configureRepository(
								c -> GenericJpaRepository.builder( aggregateType )
										.parameterResolverFactory( c.parameterResolverFactory( ) )
										.handlerDefinition( c.handlerDefinition( aggregateType ) )
										.lockFactory( c.getComponent(
												LockFactory.class, ( ) -> NullLockFactory.INSTANCE ) )
										.entityManagerProvider( c.getComponent(
												EntityManagerProvider.class,
												( ) -> beanFactory.getBean( EntityManagerProvider.class ) ) )
										.eventBus( c.eventBus( ) )
										.repositoryProvider( c::repository )
										.build( ) );
					}
				}
			} else {
				aggregateConf.configureRepository(
						c -> beanFactory.getBean( aggregateAnnotation.repository( ), Repository.class ) );
			}

			if ( !"".equals( aggregateAnnotation.commandTargetResolver( ) ) ) {
				aggregateConf.configureCommandTargetResolver( c -> getBean( aggregateAnnotation.commandTargetResolver( ),
						c ) );
			} else {
				findComponent( CommandTargetResolver.class ).ifPresent( commandTargetResolver -> aggregateConf
						.configureCommandTargetResolver( c -> getBean( commandTargetResolver, c ) ) );
			}

			configurer.configureAggregate( aggregateConf );
		}
	}

	/**
	 * Return the given {@code string}, with its first character lowercase
	 *
	 * @param string
	 *               The input string
	 * @return The input string, with first character lowercase
	 */
	private String lcFirst( final String string ) {
		return string.substring( 0, 1 ).toLowerCase( ) + string.substring( 1 );
	}

	private <T> String findComponent( final Class<T> componentType, final BeanDefinitionRegistry registry,
			final Supplier<BeanDefinition> defaultBean ) {
		return findComponent( componentType ).orElseGet( ( ) -> {
			final BeanDefinition beanDefinition = defaultBean.get( );
			final String beanName = BeanDefinitionReaderUtils.generateBeanName( beanDefinition, registry );
			registry.registerBeanDefinition( beanName, beanDefinition );
			return beanName;
		} );
	}

	private <T> Optional<String> findComponent( final Class<T> componentType, final String componentQualifier ) {
		return Stream.of( BeanFactoryUtils.beanNamesForTypeIncludingAncestors( beanFactory, componentType ) )
				.filter( bean -> isQualifierMatch( bean, beanFactory, componentQualifier ) )
				.findFirst( );
	}

	private <T> Optional<String> findComponent( final Class<T> componentType ) {
		final String[] beans = BeanFactoryUtils.beanNamesForTypeIncludingAncestors( beanFactory, componentType );
		if ( beans.length == 1 ) {
			return Optional.of( beans[0] );
		} else if ( beans.length > 1 ) {
			for ( final String bean : beans ) {
				final BeanDefinition beanDef = beanFactory.getMergedBeanDefinition( bean );
				if ( beanDef.isPrimary( ) ) {
					return Optional.of( bean );
				}
			}
			logger.warn( "Multiple beans of type {} found in application context: {}. Chose {}",
					componentType.getSimpleName( ), beans, beans[0] );
			return Optional.of( beans[0] );
		}
		return Optional.empty( );
	}

	@Override
	public void setBeanFactory( final BeanFactory beanFactory ) throws BeansException {
		this.beanFactory = ( ConfigurableListableBeanFactory ) beanFactory;
	}

	/**
	 * Implementation of an {@link ImportSelector} that enables the import of the
	 * {@link HierarchyAwareSpringAxonAutoConfigurer} after all {@code @Configuration} beans have been processed.
	 */
	public static class ImportSelector implements DeferredImportSelector {

		@Override
		public String[] selectImports( final AnnotationMetadata importingClassMetadata ) {
			return new String[] { HierarchyAwareSpringAxonAutoConfigurer.class.getName( ) };
		}
	}

	private static class LazyRetrievedModuleConfiguration implements ModuleConfiguration {

		private final Supplier<ModuleConfiguration> delegateSupplier;
		private final Class<?> moduleType;
		private ModuleConfiguration delegate;

		LazyRetrievedModuleConfiguration( final Supplier<ModuleConfiguration> delegateSupplier, final Class<?> moduleType ) {
			this.delegateSupplier = delegateSupplier;
			this.moduleType = moduleType;
		}

		@Override
		public void initialize( final Configuration config ) {
			getDelegate( ).initialize( config );
		}

		@Override
		public void start( ) {
			getDelegate( ).start( );
		}

		@Override
		public void shutdown( ) {
			getDelegate( ).shutdown( );
		}

		@Override
		public int phase( ) {
			return getDelegate( ).phase( );
		}

		@Override
		public ModuleConfiguration unwrap( ) {
			return getDelegate( );
		}

		@Override
		public boolean isType( final Class<?> type ) {
			return type.isAssignableFrom( moduleType );
		}

		private ModuleConfiguration getDelegate( ) {
			if ( delegate == null ) {
				delegate = delegateSupplier.get( );
			}
			return delegate;
		}
	}
}