package de.incentergy.base.jms;

import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;

@JMSDestinationDefinitions({
		@JMSDestinationDefinition(name = "java:/jms/base/entities", interfaceName = "javax.jms.Topic", destinationName = "entities") })
public class Topics {

}
