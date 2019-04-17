package de.libutzki.axon.axonhierarchical.module2;

import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.libutzki.axon.axonhierarchical.module1.Module1Event;

@Component
public class Module2EventHandler {

	private static final Logger log = LoggerFactory.getLogger( Module2EventHandler.class );

	@EventHandler
	public void on( final Module1Event someEvent ) {
		log.info( "Event handled: " + someEvent );
	}

	@EventHandler
	public void on( final Module2Event someEvent ) {
		log.info( "Event handled: " + someEvent );
	}
}
