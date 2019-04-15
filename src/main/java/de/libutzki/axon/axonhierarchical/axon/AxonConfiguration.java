package de.libutzki.axon.axonhierarchical.axon;

import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
import org.axonframework.springboot.autoconfig.AxonServerAutoConfiguration;
import org.axonframework.springboot.autoconfig.EventProcessingAutoConfiguration;
import org.axonframework.springboot.autoconfig.InfraConfiguration;
import org.axonframework.springboot.autoconfig.JdbcAutoConfiguration;
import org.axonframework.springboot.autoconfig.JpaAutoConfiguration;
import org.axonframework.springboot.autoconfig.JpaEventStoreAutoConfiguration;
import org.axonframework.springboot.autoconfig.MetricsAutoConfiguration;
import org.axonframework.springboot.autoconfig.MicrometerMetricsAutoConfiguration;
import org.axonframework.springboot.autoconfig.NoOpTransactionAutoConfiguration;
import org.axonframework.springboot.autoconfig.ObjectMapperAutoConfiguration;
import org.axonframework.springboot.autoconfig.TransactionAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
	  MetricsAutoConfiguration.class,
	  MicrometerMetricsAutoConfiguration.class,
	  EventProcessingAutoConfiguration.class,
	  AxonAutoConfiguration.class,
	  JpaAutoConfiguration.class,
	  JpaEventStoreAutoConfiguration.class,
	  JdbcAutoConfiguration.class,
	  TransactionAutoConfiguration.class,
	  NoOpTransactionAutoConfiguration.class,
	  InfraConfiguration.class,
	  ObjectMapperAutoConfiguration.class,
	  AxonServerAutoConfiguration.class
})
public class AxonConfiguration {

}
