package de.libutzki.axon.axonhierarchical;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import de.libutzki.axon.axonhierarchical.axon.AxonConfiguration;
import de.libutzki.axon.axonhierarchical.module1.Module1CommandSender;
import de.libutzki.axon.axonhierarchical.module1.Module1Configuration;
import de.libutzki.axon.axonhierarchical.module2.Module2Configuration;

@SpringBootConfiguration
public class AxonHierarchicalApplication {

	public static void main( final String[] args ) {
		final ApplicationContext applicationContext = new SpringApplicationBuilder( )
				.sources( AxonHierarchicalApplication.class ).web( WebApplicationType.NONE )
				.child( AxonConfiguration.class ).web( WebApplicationType.NONE )
				.sibling( Module1Configuration.class ).web( WebApplicationType.NONE )
				.sibling( Module2Configuration.class ).web( WebApplicationType.NONE )
				.run( args );
		final Module1CommandSender module1CommandSender = applicationContext.getBean( Module1CommandSender.class );
		module1CommandSender.sendCommand( );
	}

}
