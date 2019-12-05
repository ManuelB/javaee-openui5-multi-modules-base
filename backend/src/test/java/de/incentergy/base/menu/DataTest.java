package de.incentergy.base.menu;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import de.incentergy.base.menu.domain.NavigationListItemList;
import de.incentergy.base.menu.entities.NavigationListItem;

class DataTest {

	@Test
	void testCreateNavigationListItemJacksonWithJaxb() throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		JaxbAnnotationModule module = new JaxbAnnotationModule();
		// configure as necessary
		objectMapper.registerModule(module);
		NavigationListItem navigationListItem = Data.createNavigationListItem();

		NavigationListItemList list = new NavigationListItemList().addItem(navigationListItem);

		String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);
		System.out.println(json);

		NavigationListItemList list2 = objectMapper.readValue(json, NavigationListItemList.class);
		System.out.println(list2);
	}

}
