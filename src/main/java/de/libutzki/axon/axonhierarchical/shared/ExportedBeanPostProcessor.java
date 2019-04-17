package de.libutzki.axon.axonhierarchical.shared;

import java.util.List;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Dieser {@link BeanPostProcessor} scannt die Beans danach, ob sie mit {@link Exported} markiert sind oder ihr Name im
 * <i>exportedbeannames</i> Property enthalten ist. Falls dem so ist, so wird das Bean in den übergeordneten
 * Parent-Context mit übernommen.
 *
 * @see Exported
 */
public class ExportedBeanPostProcessor implements BeanPostProcessor {

	private final ConfigurableListableBeanFactory beanFactory;
	private final List<String> exportedBeanNames;
	private final String moduleName;

	public ExportedBeanPostProcessor( final ConfigurableListableBeanFactory beanFactory, final String moduleName, final List<String> exportedBeanNames ) {
		this.beanFactory = beanFactory;
		this.exportedBeanNames = exportedBeanNames;
		this.moduleName = moduleName;
	}

	@Override
	public Object postProcessBeforeInitialization( final Object bean, final String beanName ) {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization( final Object bean, final String beanName ) {
		if ( exportedBeanNames.contains( beanName ) ) {
			exportBeanToParentApplicationContext( bean, beanName );
		}

		return bean;
	}

	private void exportBeanToParentApplicationContext( final Object bean, final String beanName ) {
		final ConfigurableListableBeanFactory parentBeanFactory = getParentBeanFactory( );
		if ( parentBeanFactory == null ) {
			throw new IllegalStateException( "Das Bean " + beanName + " kann nicht exportiert werden, da kein Parent Application Context vorhanden ist." );
		}

		final String uniqueBeanName = moduleName + "." + beanName;
		parentBeanFactory.registerSingleton( uniqueBeanName, bean );
	}

	private ConfigurableListableBeanFactory getParentBeanFactory( ) {
		final BeanFactory parent = beanFactory.getParentBeanFactory( );
		return parent instanceof ConfigurableListableBeanFactory ? ( ConfigurableListableBeanFactory ) parent : null;
	}
}
