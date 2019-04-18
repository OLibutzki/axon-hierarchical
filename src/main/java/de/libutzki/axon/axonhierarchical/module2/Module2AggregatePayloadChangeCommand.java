package de.libutzki.axon.axonhierarchical.module2;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public class Module2AggregatePayloadChangeCommand {

	@TargetAggregateIdentifier
	private final String id;
	private final String payload;

	public Module2AggregatePayloadChangeCommand( final String id, final String payload ) {
		this.id = id;
		this.payload = payload;
	}

	public String getId( ) {
		return id;
	}

	public String getPayload( ) {
		return payload;
	}

	@Override
	public String toString( ) {
		return "<" + this.getClass( ).getSimpleName( ) + "> " + id + ", " + payload;
	}
}