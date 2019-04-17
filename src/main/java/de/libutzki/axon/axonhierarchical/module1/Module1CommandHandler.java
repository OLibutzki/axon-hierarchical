package de.libutzki.axon.axonhierarchical.module1;

import org.axonframework.commandhandling.CommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.libutzki.axon.axonhierarchical.module2.Module2Command;

@Component
public class Module1CommandHandler {

	private static final Logger log = LoggerFactory.getLogger( Module1CommandHandler.class );

	@CommandHandler
	public void handle( final Module1Command someCommand ) {
		log.info( "Command handled: " + someCommand );
	}

	@CommandHandler
	public void handle( final Module2Command someCommand ) {
		log.info( "Command handled: " + someCommand );
	}

}
