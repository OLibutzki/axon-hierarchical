package de.libutzki.axon.axonhierarchical.module1;

import org.axonframework.commandhandling.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class SomeCommandHandler {

	@CommandHandler
	public void handle(SomeCommand someCommand) {
		System.out.println("The command's payload: " + someCommand.getPayload());
	}
	
}
