package de.libutzki.axon.axonhierarchical.module2;

import org.axonframework.commandhandling.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.libutzki.axon.axonhierarchical.module1.Module1Command;

@Component
public class Module2CommandHandler {

	private static final Logger log = LoggerFactory.getLogger( Module2CommandHandler.class );

	@CommandHandler
	public void handle( final Module1Command someCommand ) {
		log.info( "Command handled: " + someCommand );
	}

}
