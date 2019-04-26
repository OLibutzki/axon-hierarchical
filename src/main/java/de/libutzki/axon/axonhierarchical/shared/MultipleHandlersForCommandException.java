package de.libutzki.axon.axonhierarchical.shared;

import static java.lang.String.format;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.common.AxonNonTransientException;

public class MultipleHandlersForCommandException extends AxonNonTransientException {

	private static final long serialVersionUID = -7242262641697288852L;

	/**
	 * Initialize a MultipleHandlersForCommandException with the given {@code message}.
	 *
	 * @param message
	 *                The message describing the cause of the exception
	 */
	public MultipleHandlersForCommandException( final String message ) {
		super( message );
	}

	/**
	 * Initialize a MultipleHandlersForCommandException with a message describing the given {@code CommandMessage}.
	 *
	 * @param commandMessage
	 *                       The command for which multiple handlers were found
	 */
	public MultipleHandlersForCommandException( final CommandMessage<?> commandMessage ) {
		this( format( "Multiple handlers available to handle command [%s]", commandMessage.getCommandName( ) ) );
	}
}