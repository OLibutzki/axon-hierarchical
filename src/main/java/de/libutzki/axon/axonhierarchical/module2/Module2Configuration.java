package de.libutzki.axon.axonhierarchical.module2;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import de.libutzki.axon.axonhierarchical.shared.AxonClientConfiguration;

@Configuration
@ComponentScan
@EnableTransactionManagement
@EntityScan( {
		"de.libutzki.axon.axonhierarchical.module2.aggregate"
} )
@PropertySource( "classpath:de/libutzki/axon/axonhierarchical/module2/application.properties" )
@Import( {
		AxonClientConfiguration.class
} )
public class Module2Configuration {
}
