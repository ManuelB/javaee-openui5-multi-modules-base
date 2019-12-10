package de.incentergy.base.jms;

import javax.jms.JMSDestinationDefinition;
import javax.jms.JMSDestinationDefinitions;

@JMSDestinationDefinitions({
		@JMSDestinationDefinition(name = "java:/jms/base/entities", interfaceName = "javax.jms.Topic", destinationName = "entities"),
		@JMSDestinationDefinition(name = "java:/jms/base/deployment", interfaceName = "javax.jms.Topic", destinationName = "deployment"),
		@JMSDestinationDefinition(name = "java:/jms/base/client-websocket", interfaceName = "javax.jms.Topic", destinationName = "client-websocket")})
public class Topics {

}
