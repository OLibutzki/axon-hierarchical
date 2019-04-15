package de.libutzki.axon.axonhierarchical.module1;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Module1Runner implements CommandLineRunner {

	private final CommandGateway commandGateway;
	
	@Override
	public void run(String... args) throws Exception {
		System.out.println("Yippie!");
		commandGateway.sendAndWait(SomeCommand.of("Test-Payload"));
	}

}
