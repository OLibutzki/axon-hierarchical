package de.libutzki.axon.axonhierarchical.shared;

import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Simple configuration which can be imported / used in order to declare a {@link DelegatingCommandBus}.
 *
 * @author Oliver Libutzki
 *
 */
@Configuration
public class AxonDelegatingCommandBusConfiguration {

	/**
	 * The {@link DelegatingCommandBus} has to be annotated with {@link Primary} as the exported bean would be found twice
	 * otherwise.
	 */
	@Bean
	@Primary
	public DelegatingCommandBus delegatingCommandBus( final AxonConfiguration axonConfiguration ) {
		return new DelegatingCommandBus( axonConfiguration.messageMonitor( DelegatingCommandBus.class, "delegatingCommandBus" ) );
	}
}
