package de.libutzki.axon.axonhierarchical.module1;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@RequiredArgsConstructor(staticName = "of")
public class SomeCommand {
	private String payload;
}
