package de.libutzki.axon.axonhierarchical.module1;

public class Module1Command {
	private final String payload;

	public Module1Command( final String payload ) {
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
