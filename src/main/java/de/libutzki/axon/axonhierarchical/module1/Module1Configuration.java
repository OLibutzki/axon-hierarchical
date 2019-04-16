package de.libutzki.axon.axonhierarchical.module1;

import org.axonframework.spring.config.AnnotationDriven;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan
@AnnotationDriven
@Import( HierarchyAwareSpringAxonAutoConfigurer.ImportSelector.class )
public class Module1Configuration {

}
