package de.libutzki.axon.axonhierarchical.module1;

import javax.transaction.Transactional;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.libutzki.axon.axonhierarchical.module2.CreateModule2AggregateCommand;

@Component
public class Module1CommandSender {

	private static final Logger log = LoggerFactory.getLogger( Module1CommandSender.class );

	private final CommandGateway commandGateway;

	public Module1CommandSender( final CommandGateway commandGateway ) {
		this.commandGateway = commandGateway;
	}

	@Transactional
	public void sendCommand( ) {
		final CreateModule2AggregateCommand createModule2AggregateCommand = new CreateModule2AggregateCommand( "1", "Initial Payload" );
		log.info( "Sending command: " + createModule2AggregateCommand );
		commandGateway.sendAndWait( createModule2AggregateCommand );
	}

}
