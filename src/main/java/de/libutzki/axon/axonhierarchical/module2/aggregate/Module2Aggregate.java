package de.libutzki.axon.axonhierarchical.module2.aggregate;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.libutzki.axon.axonhierarchical.module2.CreateModule2AggregateCommand;

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

	public String getPayload( ) {
		return payload;
	}

}
