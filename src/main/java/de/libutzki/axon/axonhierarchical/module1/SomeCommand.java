package de.libutzki.axon.axonhierarchical.module1;

public class SomeCommand {
	private final String payload;

	public SomeCommand( final String payload ) {
		this.payload = payload;
	}

	public String getPayload( ) {
		return payload;
	}
}
