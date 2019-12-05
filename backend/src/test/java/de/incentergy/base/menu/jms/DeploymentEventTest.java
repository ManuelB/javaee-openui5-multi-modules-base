package de.incentergy.base.menu.jms;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import de.incentergy.base.menu.Data;
import de.incentergy.base.menu.domain.NavigationListItemList;
import de.incentergy.base.menu.entities.NavigationListItem;

class DeploymentEventTest {

	@Test
	void testDeploymentEvent() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		JaxbAnnotationModule module = new JaxbAnnotationModule();
		// configure as necessary
		objectMapper.registerModule(module);
		NavigationListItem navigationListItem = Data.createNavigationListItem();

		NavigationListItemList list = new NavigationListItemList().addItem(navigationListItem);
		
		DeploymentEvent deploymentEvent = new DeploymentEvent(Lifecycle.PRE_UNDEPLOYMENT, "base-frontend");
		deploymentEvent.setNavigationListItemList(list);

		String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(deploymentEvent);
		System.out.println(json);
	}

}
