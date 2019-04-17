package de.libutzki.axon.axonhierarchical.module2;

public class Module2Event {
	private final String payload;

	public Module2Event( final String payload ) {
		this.payload = payload;
	}

	public String getPayload( ) {
		return payload;
	}

	@Override
	public String toString( ) {
		return "<" + this.getClass( ).getSimpleName( ) + "> " + payload;
	}
}
