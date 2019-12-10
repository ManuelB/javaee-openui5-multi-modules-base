package de.incentergy.base.menu.jms;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.json.Json;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import de.incentergy.base.menu.entities.NavigationListItem;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "java:/jms/base/deployment"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "") })
public class CreateMenuOnDeploymentMessageListener implements MessageListener {

	private static final Logger log = Logger.getLogger(CreateMenuOnDeploymentMessageListener.class.getName());

	@Inject
	private JMSContext context;

	@PersistenceContext
	EntityManager em;

	@Resource
	private MessageDrivenContext mesageDrivenContext;

	@Resource(mappedName = "java:/jms/base/client-websocket")
	Topic topic;

	private ObjectMapper objectMapper;

	public CreateMenuOnDeploymentMessageListener() {
		objectMapper = new ObjectMapper();
		JaxbAnnotationModule module = new JaxbAnnotationModule();
		// configure as necessary
		objectMapper.registerModule(module);
	}

	@Override
	public void onMessage(Message message) {
		if (message instanceof TextMessage) {
			try {
				DeploymentEvent deploymentEvent = objectMapper.readValue(((TextMessage) message).getText(),
						DeploymentEvent.class);
				log.log(Level.INFO, "Processing lifecycle event {0} of {1}",
						new Object[] { deploymentEvent.getLifecycle(), deploymentEvent.getApplicationName() });
				// post deployment create the necessary menu entries
				if (deploymentEvent.getLifecycle() == Lifecycle.POST_DEPLOYMENT) {
					deploymentEvent.getNavigationListItemList().getItems().stream().forEach(em::merge);
					// pre undeployment delete the menu entries
				} else if (deploymentEvent.getLifecycle() == Lifecycle.PRE_UNDEPLOYMENT) {
					deploymentEvent.getNavigationListItemList().getItems().stream()
							.map(nli -> em.find(NavigationListItem.class, nli.getId())).filter(Objects::nonNull)
							.forEach(nli -> em.remove(nli));
				}

				context.createProducer().send(topic,
						Json.createObjectBuilder().add("channelId", "server-event").add("eventId", "menu-update")
								.add("appName", deploymentEvent.getApplicationName()).build().toString());

			} catch (JMSException e) {
				log.log(Level.SEVERE, "Exception during createing NavigationListItem: {0}", e.toString());
				mesageDrivenContext.setRollbackOnly();
			} catch (JsonMappingException e) {
				log.log(Level.SEVERE, "Exception during createing NavigationListItem: {0}", e.toString());
				mesageDrivenContext.setRollbackOnly();
			} catch (JsonProcessingException e) {
				log.log(Level.SEVERE, "Exception during createing NavigationListItem: {0}", e.toString());
				mesageDrivenContext.setRollbackOnly();
			}
		} else {
			log.log(Level.WARNING, "Unexpected deployment message type. Expected: TextMessage but was: {0}",
					message.getClass().getName());
		}

	}

}
