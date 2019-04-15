package de.libutzki.axon.axonhierarchical;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

import de.libutzki.axon.axonhierarchical.axon.AxonConfiguration;
import de.libutzki.axon.axonhierarchical.module1.Module1Configuration;

@SpringBootConfiguration
@Import(AxonConfiguration.class)
public class AxonHierarchicalApplication {

	public static void main(String[] args) {
	    new SpringApplicationBuilder()
	      .sources(AxonHierarchicalApplication.class).web(WebApplicationType.NONE)
	      .child(Module1Configuration.class).web(WebApplicationType.NONE)
	      .run(args);
	}

}
