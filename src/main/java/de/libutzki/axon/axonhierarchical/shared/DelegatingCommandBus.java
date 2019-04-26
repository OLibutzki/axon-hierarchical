package de.libutzki.axon.axonhierarchical.shared;

import static org.axonframework.commandhandling.GenericCommandResultMessage.asCommandResultMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.NoHandlerForCommandException;
import org.axonframework.common.Registration;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.MessageHandler;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.monitoring.MessageMonitor;

/**
 * The {@code DelegatingCommandBus} is responsible for dispatching commands to a registered
 * {@link DelegationCandidateCommandBus}. It tries to find a suitable command bus by consulting
 * {@link DelegationCandidateCommandBus#canHandle(CommandMessage)}.
 *
 * The command can only be dispatched if there is exactly one registered {@link DelegationCandidateCommandBus} which's
 * {@link DelegationCandidateCommandBus#canHandle(CommandMessage) canHandle} method returns true. Otherwise a
 * {@link NoHandlerForCommandException} or {@link MultipleHandlersForCommandException} is thrown.
 *
 * @author Oliver Libutzki
 *
 */
public final class DelegatingCommandBus implements CommandBus {

	private final List<DelegationCandidateCommandBus> delegationCandidates = new CopyOnWriteArrayList<>( );
	private final MessageMonitor<? super CommandMessage<?>> messageMonitor;

	public DelegatingCommandBus( final MessageMonitor<? super CommandMessage<?>> messageMonitor ) {
		this.messageMonitor = messageMonitor;
	}

	@Override
	public <C, R> void dispatch( final CommandMessage<C> command, final CommandCallback<? super C, ? super R> callback ) {
		final MessageMonitor.MonitorCallback monitorCallback = messageMonitor.onMessageIngested( command );
		final List<DelegationCandidateCommandBus> commandBuses = findSuitableCommandBusesFor( command );
		switch ( commandBuses.size( ) ) {
		case 1:
			final DelegationCandidateCommandBus commandBus = commandBuses.iterator( ).next( );
			commandBus.dispatch( command, callback );
			break;
		case 0:
			final NoHandlerForCommandException noHandlerForCommandException = new NoHandlerForCommandException( command );
			monitorCallback.reportFailure( noHandlerForCommandException );
			callback.onResult( command, asCommandResultMessage( noHandlerForCommandException ) );
			break;
		default:
			final MultipleHandlersForCommandException multipleHandlersForCommandException = new MultipleHandlersForCommandException( command );
			monitorCallback.reportFailure( multipleHandlersForCommandException );
			callback.onResult( command, asCommandResultMessage( multipleHandlersForCommandException ) );
			break;
		}
	}

	@Override
	public Registration registerHandlerInterceptor( final MessageHandlerInterceptor<? super CommandMessage<?>> handlerInterceptor ) {
		throw new UnsupportedOperationException( "It's not possible to register a HandlerInterceptor at a " + DelegatingCommandBus.class + ". Please register at of its delegation candidates." );
	}

	@Override
	public Registration registerDispatchInterceptor( final MessageDispatchInterceptor<? super CommandMessage<?>> dispatchInterceptor ) {
		throw new UnsupportedOperationException( "It's not possible to register a DispatchInterceptor at a " + DelegatingCommandBus.class + ". Please register at of its delegation candidates." );
	}

	@Override
	public Registration subscribe( final String commandName, final MessageHandler<? super CommandMessage<?>> handler ) {
		throw new UnsupportedOperationException( "It's not possible to subscribe to a " + DelegatingCommandBus.class + ". Please subscribe to one of its delegation candidates." );
	}

	/**
	 * Registers the given command bus.
	 * 
	 * @param commandBus
	 *                   The command bus to be registered
	 */
	public void registerCommandBus( final DelegationCandidateCommandBus commandBus ) {
		delegationCandidates.add( commandBus );
	}

	private List<DelegationCandidateCommandBus> findSuitableCommandBusesFor( final CommandMessage<?> command ) {
		return delegationCandidates.stream( ).filter( candidate -> candidate.canHandle( command ) ).collect( Collectors.toList( ) );
	}

}
