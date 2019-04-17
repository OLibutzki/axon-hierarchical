package de.libutzki.axon.axonhierarchical.module1;

import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.libutzki.axon.axonhierarchical.module2.Module2Event;

@Component
public class Module1EventHandler {

	private static final Logger log = LoggerFactory.getLogger( Module1EventHandler.class );

	@EventHandler
	public void on( final Module1Event someEvent ) {
		log.info( "Event handled: " + someEvent );
	}

	@EventHandler
	public void on( final Module2Event someEvent ) {
		log.info( "Event handled: " + someEvent );
	}
}
