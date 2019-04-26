package de.libutzki.axon.axonhierarchical.shared;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.common.Registration;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.MessageHandler;
import org.axonframework.messaging.MessageHandlerInterceptor;

/**
 * The {@code DelegationCandidateCommandBusWrapper} wraps a {@link CommandBus} and additionally implements
 * {@link DelegationCandidateCommandBus}. Therefore it is able to answer if the underlying {@link CommandBus} is able to
 * handle a certain {@link CommandMessage}.
 * 
 * @author Oliver Libutzki
 *
 */
public final class DelegationCandidateCommandBusWrapper implements DelegationCandidateCommandBus {

	private final CommandBus commandBus;
	private final ConcurrentMap<String, MessageHandler<? super CommandMessage<?>>> subscriptions = new ConcurrentHashMap<>( );

	public DelegationCandidateCommandBusWrapper( final CommandBus commandBus ) {
		this.commandBus = commandBus;
	}

	@Override
	public Registration registerHandlerInterceptor( final MessageHandlerInterceptor<? super CommandMessage<?>> handlerInterceptor ) {
		return commandBus.registerHandlerInterceptor( handlerInterceptor );
	}

	@Override
	public Registration registerDispatchInterceptor( final MessageDispatchInterceptor<? super CommandMessage<?>> dispatchInterceptor ) {
		return commandBus.registerDispatchInterceptor( dispatchInterceptor );
	}

	@Override
	public <C, R> void dispatch( final CommandMessage<C> command, final CommandCallback<? super C, ? super R> callback ) {
		commandBus.dispatch( command, callback );
	}

	@Override
	public Registration subscribe( final String commandName, final MessageHandler<? super CommandMessage<?>> handler ) {
		final Registration delegateRegistration = commandBus.subscribe( commandName, handler );
		subscriptions.put( commandName, handler );
		return ( ) -> {

			final boolean delegateCancelationSuccessful = delegateRegistration.cancel( );
			final boolean wrapperCancelationSuccessful = subscriptions.remove( commandName, handler );
			return delegateCancelationSuccessful && wrapperCancelationSuccessful;
		};
	}

	@Override
	public boolean canHandle( final CommandMessage<?> command ) {
		return subscriptions.containsKey( command.getCommandName( ) );
	}

}
