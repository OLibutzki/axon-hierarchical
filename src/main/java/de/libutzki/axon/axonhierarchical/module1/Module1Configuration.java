package de.libutzki.axon.axonhierarchical.module1;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import de.libutzki.axon.axonhierarchical.shared.AxonClientConfiguration;
import de.libutzki.axon.axonhierarchical.shared.ExportedBeanPostProcessor;

@Configuration
@ComponentScan
@EnableTransactionManagement
@PropertySource( "classpath:de/libutzki/axon/axonhierarchical/module1/application.properties" )
@Import( {
		AxonClientConfiguration.class
} )
public class Module1Configuration {
	@Bean
	public ExportedBeanPostProcessor exportedBeanPostProcessorExportedBeanPostProcessor( final ConfigurableListableBeanFactory beanFactory, @Value( "${modulename}" ) final String moduleName, @Value( "${exportedbeannames:}#{T(java.util.Collections).emptyList()}" ) final List<String> exportedBeanNames ) {
		return new ExportedBeanPostProcessor( beanFactory, moduleName, exportedBeanNames );
	}
}
