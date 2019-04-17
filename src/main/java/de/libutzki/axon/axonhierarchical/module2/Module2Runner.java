package de.libutzki.axon.axonhierarchical.module2;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Module2Runner implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger( Module2Runner.class );

	private final CommandGateway commandGateway;
	private final EventGateway eventGateway;

	public Module2Runner( final CommandGateway commandGateway, final EventGateway eventGateway ) {
		this.commandGateway = commandGateway;
		this.eventGateway = eventGateway;
	}

	@Override
	public void run( final String... args ) throws Exception {
		final Module2Command command = new Module2Command( "Test Command-Payload" );
		log.info( "Sending command: " + command );
		commandGateway.sendAndWait( command );
		final Module2Event event = new Module2Event( "Test Event-Payload" );
		log.info( "Publishing event: " + event );
		eventGateway.publish( event );
	}

}
