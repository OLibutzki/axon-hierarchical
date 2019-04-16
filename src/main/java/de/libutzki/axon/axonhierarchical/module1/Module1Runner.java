package de.libutzki.axon.axonhierarchical.module1;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Module1Runner implements CommandLineRunner {

	private final CommandGateway commandGateway;

	public Module1Runner( final CommandGateway commandGateway ) {
		this.commandGateway = commandGateway;
	}

	@Override
	public void run( final String... args ) throws Exception {
		System.out.println( "Sending command!" );
		commandGateway.sendAndWait( new SomeCommand( "Test-Payload" ) );
	}

}
