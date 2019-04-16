package de.libutzki.axon.axonhierarchical;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;

import de.libutzki.axon.axonhierarchical.axon.AxonConfiguration;
import de.libutzki.axon.axonhierarchical.module1.Module1Configuration;

@SpringBootConfiguration
public class AxonHierarchicalApplication {

	public static void main( final String[] args ) {
		new SpringApplicationBuilder( )
				.sources( AxonConfiguration.class ).web( WebApplicationType.NONE )
				.child( Module1Configuration.class ).web( WebApplicationType.NONE )
				.run( args );
	}

}
