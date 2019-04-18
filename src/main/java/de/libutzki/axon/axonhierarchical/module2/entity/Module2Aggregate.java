package de.libutzki.axon.axonhierarchical.module2.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.libutzki.axon.axonhierarchical.module2.CreateModule2AggregateCommand;
import de.libutzki.axon.axonhierarchical.module2.Module2AggregatePayloadChangeCommand;
import de.libutzki.axon.axonhierarchical.module2.Module2AggregatePayloadChangedEvent;

@Entity
@Aggregate
public class Module2Aggregate {

	private static final Logger log = LoggerFactory.getLogger( Module2Aggregate.class );

	@Id
	@AggregateIdentifier
	private String id;

	private String payload;

	@SuppressWarnings( "unused" )
	private Module2Aggregate( ) {

	}

	@CommandHandler
	public Module2Aggregate( final CreateModule2AggregateCommand command ) {
		log.info( "Command handled: " + command );
		id = command.getId( );
		payload = command.getPayload( );
	}

	@CommandHandler
	public void handle( final Module2AggregatePayloadChangeCommand command ) {
		log.info( "Command handled: " + command );
		payload = command.getPayload( );
		AggregateLifecycle.apply( new Module2AggregatePayloadChangedEvent( id, payload ) );
	}

	@EventHandler
	public void on( final Module2AggregatePayloadChangedEvent event ) {
		log.info( "Event handled: " + event );
	}

}
