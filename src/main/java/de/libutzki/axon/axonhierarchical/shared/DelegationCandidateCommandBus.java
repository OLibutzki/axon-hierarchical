package de.libutzki.axon.axonhierarchical.shared;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandMessage;

/**
 * Extension of the {@link CommandBus} which offers the possibility to ask the {@link CommandBus} if it could handle a
 * certain {@link CommandMessage}.
 *
 * @author Oliver Libutzki
 *
 */
public interface DelegationCandidateCommandBus extends CommandBus {

	/**
	 * Return true, if the given {@link CommandMessage} can be handled, otherwise false
	 * 
	 * @param command
	 *                the command message
	 * @return true, if the given {@link CommandMessage} can be handled, otherwise false
	 */
	boolean canHandle( CommandMessage<?> command );

}
